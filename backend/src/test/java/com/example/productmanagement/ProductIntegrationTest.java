package com.example.productmanagement;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.productmanagement.analytics.repository.InventorySnapshotRepository;
import com.example.productmanagement.analytics.service.AnalyticsSnapshotService;
import com.example.productmanagement.repository.ProductRepository;
import com.example.productmanagement.repository.SettingsRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers(disabledWithoutDocker = true)
class ProductIntegrationTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("product_management_test")
          .withUsername("test_user")
          .withPassword("test_password");

  @DynamicPropertySource
  static void configureDatasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @Autowired private MockMvc mockMvc;

  @Autowired private ProductRepository productRepository;

  @Autowired private SettingsRepository settingsRepository;

  @Autowired private InventorySnapshotRepository inventorySnapshotRepository;

  @Autowired private AnalyticsSnapshotService analyticsSnapshotService;

  @BeforeEach
  void cleanDatabase() {
    inventorySnapshotRepository.deleteAll();
    settingsRepository.deleteAll();
    productRepository.deleteAll();
  }

  @Test
  void shouldReadAndUpdateSettingsAndUseTheConfiguredThresholdAndPrefix() throws Exception {
    mockMvc
        .perform(get("/api/v1/settings"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.lowStockThreshold").value(10))
        .andExpect(jsonPath("$.data.productCodePrefix").value("PRD-"))
        .andExpect(jsonPath("$.data.password").doesNotExist());

    mockMvc
        .perform(
            put("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "lowStockThreshold": 5,
                      "productCodePrefix": "SKU",
                      "allowNegativeStock": false,
                      "workspaceName": "Kho thử nghiệm",
                      "currency": "USD",
                      "dateFormat": "YYYY-MM-DD"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.lowStockThreshold").value(5))
        .andExpect(jsonPath("$.data.productCodePrefix").value("SKU-"))
        .andExpect(jsonPath("$.data.currency").value("USD"));

    mockMvc
        .perform(
            post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Sản phẩm tự sinh mã",
                      "price": 100,
                      "stockQuantity": 4,
                      "status": "ACTIVE"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.productCode").value("SKU-0001"))
        .andExpect(jsonPath("$.data.status").value("LOW_STOCK"));
  }

  @Test
  void shouldReturnZeroAnalyticsWhenThereAreNoProducts() throws Exception {
    mockMvc
        .perform(get("/api/v1/analytics/overview"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalProducts").value(0))
        .andExpect(jsonPath("$.data.totalInventoryValue").value(0))
        .andExpect(jsonPath("$.data.lowStockRate").value(0));
  }

  @Test
  void shouldCalculateAnalyticsFromLiveProductsAndExcludeSoftDeletedProducts() throws Exception {
    createAnalyticsProduct("PRD-9101", "Sản phẩm hoạt động", 100, 20, "ACTIVE");
    createAnalyticsProduct("PRD-9102", "Sản phẩm tự động", 200, 9, "ACTIVE");
    createAnalyticsProduct("PRD-9103", "Sản phẩm thủ công", 300, 25, "LOW_STOCK");
    createAnalyticsProduct("PRD-9104", "Sản phẩm hết hàng", 400, 0, "ACTIVE");
    createAnalyticsProduct("PRD-9105", "Sản phẩm ngừng", 100, 12, "INACTIVE");
    createAnalyticsProduct("PRD-9106", "Sản phẩm đã xóa", 999, 99, "ACTIVE");

    long deletedId =
        productRepository.findAll().stream()
            .filter(product -> product.getProductCode().equals("PRD-9106"))
            .findFirst()
            .orElseThrow()
            .getId();
    mockMvc.perform(delete("/api/v1/products/{id}", deletedId)).andExpect(status().isOk());

    mockMvc
        .perform(get("/api/v1/analytics/overview"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalProducts").value(5))
        .andExpect(jsonPath("$.data.activeProducts").value(1))
        .andExpect(jsonPath("$.data.lowStockProducts").value(3))
        .andExpect(jsonPath("$.data.outOfStockProducts").value(1))
        .andExpect(jsonPath("$.data.inactiveProducts").value(1))
        .andExpect(jsonPath("$.data.totalStockQuantity").value(66))
        .andExpect(jsonPath("$.data.totalInventoryValue").value(12500.0))
        .andExpect(jsonPath("$.data.averageProductPrice").value(220.0))
        .andExpect(jsonPath("$.data.lowStockRate").value(60.0));

    mockMvc
        .perform(get("/api/v1/analytics/top-inventory-value").param("limit", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].productCode").value("PRD-9103"))
        .andExpect(jsonPath("$.data[0].inventoryValue").value(7500.0));

    mockMvc
        .perform(get("/api/v1/analytics/inventory-by-status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(3))
        .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
        .andExpect(jsonPath("$.data[1].status").value("LOW_STOCK"))
        .andExpect(jsonPath("$.data[1].productCount").value(3));

    mockMvc
        .perform(get("/api/v1/analytics/stock-distribution"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(3))
        .andExpect(jsonPath("$.data[0].bucket").value("OUT_OF_STOCK"))
        .andExpect(jsonPath("$.data[0].label").value("Hết hàng"));

    mockMvc
        .perform(get("/api/v1/analytics/low-stock"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(3))
        .andExpect(jsonPath("$.data[0].stockQuantity").value(0))
        .andExpect(jsonPath("$.data[1].productCode").value("PRD-9102"));

    mockMvc
        .perform(get("/api/v1/analytics/trends"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("INSUFFICIENT_DATA"))
        .andExpect(
            jsonPath("$.data.message").value("Chưa đủ dữ liệu lịch sử để hiển thị xu hướng."))
        .andExpect(jsonPath("$.data.points").isEmpty());

    analyticsSnapshotService.captureSnapshot();
    mockMvc
        .perform(get("/api/v1/analytics/trends"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("INSUFFICIENT_DATA"));
  }

  @Test
  @Transactional
  void shouldReturnTrendPointsWhenTwoRealSnapshotDatesExist() throws Exception {
    createAnalyticsProduct("PRD-9201", "Sản phẩm lịch sử", 100, 12, "ACTIVE");
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate today = LocalDate.now();
    inventorySnapshotRepository.upsertSnapshot(yesterday);
    inventorySnapshotRepository.upsertSnapshot(today);

    mockMvc
        .perform(
            get("/api/v1/analytics/trends")
                .param("from", yesterday.toString())
                .param("to", today.toString())
                .param("granularity", "DAY"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("READY"))
        .andExpect(jsonPath("$.data.points.length()").value(2))
        .andExpect(jsonPath("$.data.points[0].totalInventoryValue").value(1200.0));
  }

  private void createAnalyticsProduct(
      String code, String name, int price, int stockQuantity, String status) throws Exception {
    String payload =
        """
                {
                  "productCode": "%s",
                  "name": "%s",
                  "price": %d,
                  "stockQuantity": %d,
                  "status": "%s"
                }
                """
            .formatted(code, name, price, stockQuantity, status);
    mockMvc
        .perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated());
  }

  @Test
  void shouldCreateListUpdateAndSoftDeleteProduct() throws Exception {
    String createPayload =
        """
                {
                  "productCode": "PRD-9001",
                  "name": "Integration Test Product",
                  "description": "Created by integration test",
                  "category": "Accessories",
                  "brand": "Acme",
                  "supplier": "Acme Distribution",
                  "unit": "piece",
                  "warehouseLocation": "A-01",
                  "warrantyMonths": 12,
                  "barcode": "8938501239001",
                  "costPrice": 15.00,
                  "minimumStock": 12,
                  "imageUrl": "https://example.com/product.png",
                  "price": 25.50,
                  "stockQuantity": 10,
                  "status": "ACTIVE"
                }
                """;

    mockMvc
        .perform(
            post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(createPayload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.productCode").value("PRD-9001"))
        .andExpect(jsonPath("$.data.category").value("Accessories"))
        .andExpect(jsonPath("$.data.brand").value("Acme"))
        .andExpect(jsonPath("$.data.supplier").value("Acme Distribution"))
        .andExpect(jsonPath("$.data.unit").value("piece"))
        .andExpect(jsonPath("$.data.warehouseLocation").value("A-01"))
        .andExpect(jsonPath("$.data.warrantyMonths").value(12))
        .andExpect(jsonPath("$.data.barcode").value("8938501239001"))
        .andExpect(jsonPath("$.data.costPrice").value(15.00))
        .andExpect(jsonPath("$.data.minimumStock").value(12))
        .andExpect(jsonPath("$.data.imageUrl").value("https://example.com/product.png"))
        .andExpect(jsonPath("$.data.version").value(0));

    mockMvc
        .perform(get("/api/v1/products").param("keyword", "integration"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalElements").value(1));

    String updatePayload =
        """
                {
                  "name": "Updated Integration Product",
                  "description": "Updated description",
                  "category": "Updated Accessories",
                  "brand": "Acme Pro",
                  "supplier": "Acme Distribution",
                  "unit": "piece",
                  "warehouseLocation": "A-02",
                  "warrantyMonths": 24,
                  "barcode": "8938501239002",
                  "costPrice": 18.00,
                  "minimumStock": 10,
                  "imageUrl": "https://example.com/updated-product.png",
                  "price": 30.00,
                  "stockQuantity": 8,
                  "status": "ACTIVE",
                  "version": 0
                }
                """;

    long productId = productRepository.findAll().get(0).getId();
    mockMvc
        .perform(
            put("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.name").value("Updated Integration Product"))
        .andExpect(jsonPath("$.data.costPrice").value(18.00))
        .andExpect(jsonPath("$.data.minimumStock").value(10))
        .andExpect(jsonPath("$.data.status").value("LOW_STOCK"))
        .andExpect(jsonPath("$.data.version").value(1));

    mockMvc
        .perform(delete("/api/v1/products/{id}", productId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").value(nullValue()));

    mockMvc
        .perform(get("/api/v1/products/{id}", productId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void shouldPreserveManuallySelectedLowStockStatus() throws Exception {
    String payload =
        """
                {
                  "productCode": "PRD-9004",
                  "name": "Manual Low Stock Product",
                  "price": 20.00,
                  "stockQuantity": 20,
                  "status": "LOW_STOCK"
                }
                """;

    mockMvc
        .perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.status").value("LOW_STOCK"));
  }

  @Test
  void shouldRejectDuplicateProductCode() throws Exception {
    String payload =
        """
                {
                  "productCode": "PRD-9002",
                  "name": "Duplicate Test Product",
                  "price": 10.00,
                  "stockQuantity": 1
                }
                """;

    mockMvc
        .perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated());

    mockMvc
        .perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Product code already exists: PRD-9002"));
  }

  @Test
  void shouldRejectInvalidVersion() throws Exception {
    String createPayload =
        """
                {
                  "productCode": "PRD-9003",
                  "name": "Version Test Product",
                  "price": 15.00,
                  "stockQuantity": 2
                }
                """;
    mockMvc
        .perform(
            post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(createPayload))
        .andExpect(status().isCreated());

    long productId = productRepository.findAll().get(0).getId();
    String stalePayload =
        """
                {
                  "name": "Stale Update",
                  "price": 16.00,
                  "stockQuantity": 2,
                  "status": "ACTIVE",
                  "version": 99
                }
                """;

    mockMvc
        .perform(
            put("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(stalePayload))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false));
  }
}
