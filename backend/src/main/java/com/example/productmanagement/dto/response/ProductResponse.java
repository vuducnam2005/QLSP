package com.example.productmanagement.dto.response;

import com.example.productmanagement.enums.ProductStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductResponse {

  private Long id;
  private Long version;
  private String productCode;
  private String name;
  private String description;
  private String category;
  private String brand;
  private String supplier;
  private String unit;
  private String warehouseLocation;
  private Integer warrantyMonths;
  private String barcode;
  private BigDecimal costPrice;
  private Integer minimumStock;
  private String imageUrl;
  private BigDecimal price;
  private Integer stockQuantity;
  private ProductStatus status;
  private Instant createdAt;
  private Instant updatedAt;
}
