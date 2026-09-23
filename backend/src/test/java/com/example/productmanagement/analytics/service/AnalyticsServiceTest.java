package com.example.productmanagement.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.productmanagement.analytics.dto.InventoryOverviewResponse;
import com.example.productmanagement.analytics.repository.AnalyticsProjection;
import com.example.productmanagement.analytics.repository.AnalyticsRepository;
import com.example.productmanagement.analytics.service.impl.AnalyticsServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnalyticsServiceTest {

  private final AnalyticsRepository repository = mock(AnalyticsRepository.class);
  private final AnalyticsService service = new AnalyticsServiceImpl(repository);

  @Test
  void overviewReturnsZeroesWhenThereAreNoProducts() {
    AnalyticsProjection.Overview projection = overview(0, 0, 0, 0, 0, 0, "0", "0");
    when(repository.getOverview()).thenReturn(projection);

    InventoryOverviewResponse result = service.getOverview();

    assertThat(result.getTotalProducts()).isZero();
    assertThat(result.getTotalInventoryValue()).isEqualByComparingTo("0");
    assertThat(result.getLowStockRate()).isEqualByComparingTo("0.00");
  }

  @Test
  void overviewCalculatesInventoryValueAndLowStockRatePrecisely() {
    AnalyticsProjection.Overview projection =
        overview(5, 2, 3, 1, 1, 42, "1234567.50", "350000.00");
    when(repository.getOverview()).thenReturn(projection);

    InventoryOverviewResponse result = service.getOverview();

    assertThat(result.getTotalInventoryValue()).isEqualByComparingTo("1234567.50");
    assertThat(result.getAverageProductPrice()).isEqualByComparingTo("350000.00");
    assertThat(result.getLowStockProducts()).isEqualTo(3);
    assertThat(result.getOutOfStockProducts()).isEqualTo(1);
    assertThat(result.getLowStockRate()).isEqualByComparingTo("60.00");
  }

  @Test
  void lowStockEndpointUsesBothAutomaticAndManualProducts() {
    AnalyticsProjection.InventoryProduct automatic =
        product(1L, "PRD-0001", "Tự động", 4, "LOW_STOCK");
    AnalyticsProjection.InventoryProduct manual =
        product(2L, "PRD-0002", "Thủ công", 20, "LOW_STOCK");
    when(repository.getLowStock(10)).thenReturn(List.of(automatic, manual));

    var result = service.getLowStock(10);

    assertThat(result).hasSize(2);
    assertThat(result)
        .extracting("status")
        .containsOnly(com.example.productmanagement.enums.ProductStatus.LOW_STOCK);
  }

  private AnalyticsProjection.Overview overview(
      long total,
      long active,
      long lowStock,
      long outOfStock,
      long inactive,
      long totalStock,
      String inventoryValue,
      String averagePrice) {
    AnalyticsProjection.Overview projection = mock(AnalyticsProjection.Overview.class);
    when(projection.getTotalProducts()).thenReturn(total);
    when(projection.getActiveProducts()).thenReturn(active);
    when(projection.getLowStockProducts()).thenReturn(lowStock);
    when(projection.getOutOfStockProducts()).thenReturn(outOfStock);
    when(projection.getInactiveProducts()).thenReturn(inactive);
    when(projection.getTotalStockQuantity()).thenReturn(totalStock);
    when(projection.getTotalInventoryValue()).thenReturn(new BigDecimal(inventoryValue));
    when(projection.getAverageProductPrice()).thenReturn(new BigDecimal(averagePrice));
    return projection;
  }

  private AnalyticsProjection.InventoryProduct product(
      Long id, String code, String name, int stock, String status) {
    AnalyticsProjection.InventoryProduct projection =
        mock(AnalyticsProjection.InventoryProduct.class);
    when(projection.getProductId()).thenReturn(id);
    when(projection.getProductCode()).thenReturn(code);
    when(projection.getName()).thenReturn(name);
    when(projection.getPrice()).thenReturn(new BigDecimal("100000"));
    when(projection.getStockQuantity()).thenReturn(stock);
    when(projection.getStatus()).thenReturn(status);
    when(projection.getInventoryValue()).thenReturn(new BigDecimal("1000000"));
    return projection;
  }
}
