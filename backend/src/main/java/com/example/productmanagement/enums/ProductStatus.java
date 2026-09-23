package com.example.productmanagement.enums;

public enum ProductStatus {
  ACTIVE,
  LOW_STOCK,
  INACTIVE;

  public static ProductStatus resolve(Integer stockQuantity, ProductStatus requested) {
    return resolve(stockQuantity, requested, 10);
  }

  public static ProductStatus resolve(
      Integer stockQuantity, ProductStatus requested, int lowStockThreshold) {
    if (stockQuantity != null && stockQuantity < lowStockThreshold) {
      return LOW_STOCK;
    }
    return requested == null ? ACTIVE : requested;
  }
}
