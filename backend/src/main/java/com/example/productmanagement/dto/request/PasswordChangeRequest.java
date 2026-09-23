package com.example.productmanagement.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordChangeRequest {

  private String currentPassword;

  @Size(min = 8, max = 72, message = "newPassword must contain between 8 and 72 characters")
  private String newPassword;

  private String confirmPassword;
}
