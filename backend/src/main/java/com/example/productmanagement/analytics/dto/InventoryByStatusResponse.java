package com.example.productmanagement.analytics.dto;

import com.example.productmanagement.enums.ProductStatus;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InventoryByStatusResponse {

  private ProductStatus status;
  private long productCount;
  private long totalStockQuantity;
  private BigDecimal inventoryValue;
}
