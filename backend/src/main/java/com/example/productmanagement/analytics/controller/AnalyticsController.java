package com.example.productmanagement.analytics.controller;

import com.example.productmanagement.analytics.dto.InventoryByStatusResponse;
import com.example.productmanagement.analytics.dto.InventoryOverviewResponse;
import com.example.productmanagement.analytics.dto.InventoryProductResponse;
import com.example.productmanagement.analytics.dto.StockDistributionResponse;
import com.example.productmanagement.analytics.dto.TrendResponse;
import com.example.productmanagement.analytics.service.AnalyticsService;
import com.example.productmanagement.common.ApiConstants;
import com.example.productmanagement.common.ApiResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(ApiConstants.ANALYTICS_BASE_PATH)
public class AnalyticsController {

  private final AnalyticsService analyticsService;

  public AnalyticsController(AnalyticsService analyticsService) {
    this.analyticsService = analyticsService;
  }

  @GetMapping("/overview")
  public ResponseEntity<ApiResponse<InventoryOverviewResponse>> overview() {
    return ok("Inventory overview retrieved successfully", analyticsService.getOverview());
  }

  @GetMapping("/inventory-by-status")
  public ResponseEntity<ApiResponse<List<InventoryByStatusResponse>>> inventoryByStatus() {
    return ok(
        "Inventory by status retrieved successfully", analyticsService.getInventoryByStatus());
  }

  @GetMapping("/top-inventory-value")
  public ResponseEntity<ApiResponse<List<InventoryProductResponse>>> topInventoryValue(
      @RequestParam(defaultValue = "10") int limit) {
    if (limit < 1 || limit > 50) {
      throw new com.example.productmanagement.exception.InvalidRequestException(
          "limit must be between 1 and 50");
    }
    return ok(
        "Top inventory value products retrieved successfully",
        analyticsService.getTopInventoryValue(limit));
  }

  @GetMapping("/low-stock")
  public ResponseEntity<ApiResponse<List<InventoryProductResponse>>> lowStock(
      @RequestParam(defaultValue = "10") int limit) {
    if (limit < 1 || limit > 50) {
      throw new com.example.productmanagement.exception.InvalidRequestException(
          "limit must be between 1 and 50");
    }
    return ok("Low stock products retrieved successfully", analyticsService.getLowStock(limit));
  }

  @GetMapping("/stock-distribution")
  public ResponseEntity<ApiResponse<List<StockDistributionResponse>>> stockDistribution() {
    return ok("Stock distribution retrieved successfully", analyticsService.getStockDistribution());
  }

  @GetMapping("/trends")
  public ResponseEntity<ApiResponse<TrendResponse>> trends(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(defaultValue = "DAY") String granularity) {
    return ok(
        "Inventory trends retrieved successfully",
        analyticsService.getTrends(from, to, granularity));
  }

  private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), message, data));
  }
}
