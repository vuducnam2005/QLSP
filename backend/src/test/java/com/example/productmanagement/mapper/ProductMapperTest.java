package com.example.productmanagement.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.productmanagement.dto.request.ProductCreateRequest;
import com.example.productmanagement.dto.request.ProductUpdateRequest;
import com.example.productmanagement.entity.Product;
import com.example.productmanagement.enums.ProductStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductMapperTest {

  private final ProductMapper mapper = new ProductMapper();

  @Test
  void toEntityAppliesLowStockRuleAndNormalizesText() {
    ProductCreateRequest request = new ProductCreateRequest();
    request.setProductCode(" PRD-0009 ");
    request.setName("  Test product  ");
    request.setDescription("  Description  ");
    request.setPrice(new BigDecimal("10.00"));
    request.setStockQuantity(4);
    request.setStatus(null);

    Product product = mapper.toEntity(request);

    assertThat(product.getProductCode()).isEqualTo("PRD-0009");
    assertThat(product.getName()).isEqualTo("Test product");
    assertThat(product.getDescription()).isEqualTo("Description");
    assertThat(product.getStatus()).isEqualTo(ProductStatus.LOW_STOCK);
    assertThat(product.isDeleted()).isFalse();
  }

  @Test
  void toEntityPreservesManuallySelectedLowStockAtOrAboveThreshold() {
    ProductCreateRequest request = new ProductCreateRequest();
    request.setProductCode("PRD-0010");
    request.setName("Manual low stock");
    request.setPrice(new BigDecimal("10.00"));
    request.setStockQuantity(20);
    request.setStatus(ProductStatus.LOW_STOCK);

    Product product = mapper.toEntity(request);

    assertThat(product.getStatus()).isEqualTo(ProductStatus.LOW_STOCK);
  }

  @Test
  void updateEntityForcesLowStockWhenQuantityFallsBelowThreshold() {
    Product product = new Product();
    ProductUpdateRequest request = new ProductUpdateRequest();
    request.setName("Updated product");
    request.setPrice(new BigDecimal("10.00"));
    request.setStockQuantity(9);
    request.setStatus(ProductStatus.INACTIVE);

    mapper.updateEntity(product, request);

    assertThat(product.getStatus()).isEqualTo(ProductStatus.LOW_STOCK);
  }
}
