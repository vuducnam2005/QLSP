package com.example.productmanagement.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {

  private final boolean success;
  private final int statusCode;
  private final String message;
  private final T data;
  private final Instant timestamp;

  private ApiResponse(boolean success, int statusCode, String message, T data) {
    this.success = success;
    this.statusCode = statusCode;
    this.message = message;
    this.data = data;
    this.timestamp = Instant.now();
  }

  public static <T> ApiResponse<T> success(int statusCode, String message, T data) {
    return new ApiResponse<>(true, statusCode, message, data);
  }

  public static <T> ApiResponse<T> error(int statusCode, String message, T data) {
    return new ApiResponse<>(false, statusCode, message, data);
  }
}
