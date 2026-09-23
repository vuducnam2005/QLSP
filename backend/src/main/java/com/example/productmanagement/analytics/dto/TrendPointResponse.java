package com.example.productmanagement.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrendPointResponse {

  private LocalDate period;
  private long productCount;
  private long totalStockQuantity;
  private BigDecimal totalInventoryValue;
}
