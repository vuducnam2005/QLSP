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
public class ProductUpdateRequest {

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

  @NotNull(message = "status is required")
  private ProductStatus status;

  @NotNull(message = "version is required for optimistic locking")
  @Min(value = 0, message = "version must be greater than or equal to 0")
  private Long version;
}
