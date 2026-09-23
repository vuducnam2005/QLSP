package com.example.productmanagement.analytics.service;

import com.example.productmanagement.analytics.dto.InventoryByStatusResponse;
import com.example.productmanagement.analytics.dto.InventoryOverviewResponse;
import com.example.productmanagement.analytics.dto.InventoryProductResponse;
import com.example.productmanagement.analytics.dto.StockDistributionResponse;
import com.example.productmanagement.analytics.dto.TrendResponse;
import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {

  InventoryOverviewResponse getOverview();

  List<InventoryByStatusResponse> getInventoryByStatus();

  List<InventoryProductResponse> getTopInventoryValue(int limit);

  List<InventoryProductResponse> getLowStock(int limit);

  List<StockDistributionResponse> getStockDistribution();

  TrendResponse getTrends(LocalDate from, LocalDate to, String granularity);
}
