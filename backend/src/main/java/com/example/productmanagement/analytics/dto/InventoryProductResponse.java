package com.example.productmanagement.analytics.dto;

import com.example.productmanagement.enums.ProductStatus;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InventoryProductResponse {

  private Long productId;
  private String productCode;
  private String name;
  private BigDecimal price;
  private int stockQuantity;
  private ProductStatus status;
  private BigDecimal inventoryValue;
}
