package com.example.productmanagement.dto.request;

import com.example.productmanagement.enums.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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
