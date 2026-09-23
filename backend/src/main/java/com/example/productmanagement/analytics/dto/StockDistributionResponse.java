package com.example.productmanagement.analytics.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockDistributionResponse {

  private String bucket;
  private String label;
  private long productCount;
  private long totalStockQuantity;
  private BigDecimal inventoryValue;
}
