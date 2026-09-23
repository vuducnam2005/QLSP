package com.example.productmanagement.analytics.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class AnalyticsProjection {

  private AnalyticsProjection() {}

  public interface Overview {
    Long getTotalProducts();

    Long getActiveProducts();

    Long getLowStockProducts();

    Long getOutOfStockProducts();

    Long getInactiveProducts();

    Long getTotalStockQuantity();

    BigDecimal getTotalInventoryValue();

    BigDecimal getAverageProductPrice();
  }

  public interface StatusSummary {
    String getStatus();

    Long getProductCount();

    Long getTotalStockQuantity();

    BigDecimal getInventoryValue();
  }

  public interface InventoryProduct {
    Long getProductId();

    String getProductCode();

    String getName();

    BigDecimal getPrice();

    Integer getStockQuantity();

    String getStatus();

    BigDecimal getInventoryValue();
  }

  public interface Distribution {
    String getBucket();

    Long getProductCount();

    Long getTotalStockQuantity();

    BigDecimal getInventoryValue();
  }

  public interface TrendPoint {
    LocalDate getPeriod();

    Long getProductCount();

    Long getTotalStockQuantity();

    BigDecimal getTotalInventoryValue();
  }
}
