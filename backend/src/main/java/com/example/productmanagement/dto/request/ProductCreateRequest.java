package com.example.productmanagement.dto.request;

import com.example.productmanagement.enums.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductCreateRequest {

  @Size(max = 50, message = "productCode must not exceed 50 characters")
  private String productCode;

  @NotBlank(message = "name is required")
  @Size(max = 255, message = "name must not exceed 255 characters")
  private String name;

  @Size(max = 5000, message = "description must not exceed 5000 characters")
  private String description;

  @Size(max = 100, message = "category must not exceed 100 characters")
  private String category;

  @Size(max = 100, message = "brand must not exceed 100 characters")
  private String brand;

  @Size(max = 255, message = "supplier must not exceed 255 characters")
  private String supplier;

  @Size(max = 50, message = "unit must not exceed 50 characters")
  private String unit;

  @Size(max = 100, message = "warehouseLocation must not exceed 100 characters")
  private String warehouseLocation;

  @Min(value = 0, message = "warrantyMonths must be greater than or equal to 0")
  private Integer warrantyMonths;

  @Size(max = 50, message = "barcode must not exceed 50 characters")
  private String barcode;

  @DecimalMin(value = "0.00", message = "costPrice must be greater than or equal to 0")
  @Digits(
      integer = 13,
      fraction = 2,
      message = "costPrice must have at most 13 integer digits and 2 decimal places")
  private BigDecimal costPrice;

  @Min(value = 0, message = "minimumStock must be greater than or equal to 0")
  private Integer minimumStock;

  @Size(max = 1000, message = "imageUrl must not exceed 1000 characters")
  private String imageUrl;

  @NotNull(message = "price is required")
  @DecimalMin(value = "0.01", message = "price must be greater than 0")
  @Digits(
      integer = 13,
      fraction = 2,
      message = "price must have at most 13 integer digits and 2 decimal places")
  private BigDecimal price;

  @NotNull(message = "stockQuantity is required")
  private Integer stockQuantity;

  private ProductStatus status = ProductStatus.ACTIVE;
}
