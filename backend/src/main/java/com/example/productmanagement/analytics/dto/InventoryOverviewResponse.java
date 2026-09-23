package com.example.productmanagement.analytics.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InventoryOverviewResponse {

  private long totalProducts;
  private long activeProducts;
  private long lowStockProducts;
  private long outOfStockProducts;
  private long inactiveProducts;
  private long totalStockQuantity;
  private BigDecimal totalInventoryValue;
  private BigDecimal averageProductPrice;
  private BigDecimal lowStockRate;
}
