package com.example.productmanagement.dto.response;

public record SettingsResponse(
    Long id,
    String username,
    Integer lowStockThreshold,
    String productCodePrefix,
    boolean allowNegativeStock,
    String workspaceName,
    String currency,
    String dateFormat,
    AccountResponse account) {

  public record AccountResponse(String username, String role) {}
}
