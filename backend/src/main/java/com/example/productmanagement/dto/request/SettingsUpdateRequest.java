package com.example.productmanagement.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SettingsUpdateRequest {

  @NotNull(message = "lowStockThreshold is required")
  @Min(value = 0, message = "lowStockThreshold must be greater than or equal to 0")
  @Max(value = 1000000, message = "lowStockThreshold must not exceed 1000000")
  private Integer lowStockThreshold;

  @NotBlank(message = "productCodePrefix is required")
  @Size(max = 20, message = "productCodePrefix must not exceed 20 characters")
  @Pattern(
      regexp = "^[A-Za-z0-9][A-Za-z0-9-]*$",
      message = "productCodePrefix must contain only letters, numbers and hyphens")
  private String productCodePrefix;

  @NotNull(message = "allowNegativeStock is required")
  private Boolean allowNegativeStock;

  @NotBlank(message = "workspaceName is required")
  @Size(max = 120, message = "workspaceName must not exceed 120 characters")
  private String workspaceName;

  @NotBlank(message = "currency is required")
  @Pattern(regexp = "VND|USD", message = "currency must be VND or USD")
  private String currency;

  @NotBlank(message = "dateFormat is required")
  @Pattern(regexp = "DD/MM/YYYY|MM/DD/YYYY|YYYY-MM-DD", message = "dateFormat is not supported")
  private String dateFormat;

  @Valid private PasswordChangeRequest password;
}
