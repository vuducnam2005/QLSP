package com.example.productmanagement.analytics.service.impl;

import com.example.productmanagement.analytics.dto.InventoryByStatusResponse;
import com.example.productmanagement.analytics.dto.InventoryOverviewResponse;
import com.example.productmanagement.analytics.dto.InventoryProductResponse;
import com.example.productmanagement.analytics.dto.StockDistributionResponse;
import com.example.productmanagement.analytics.dto.TrendPointResponse;
import com.example.productmanagement.analytics.dto.TrendResponse;
import com.example.productmanagement.analytics.repository.AnalyticsProjection;
import com.example.productmanagement.analytics.repository.AnalyticsRepository;
import com.example.productmanagement.analytics.service.AnalyticsService;
import com.example.productmanagement.enums.ProductStatus;
import com.example.productmanagement.exception.InvalidRequestException;
import com.example.productmanagement.service.SettingsService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

  private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

  private final AnalyticsRepository analyticsRepository;
  private final SettingsService settingsService;

  public AnalyticsServiceImpl(AnalyticsRepository analyticsRepository) {
    this(analyticsRepository, null);
  }

  @Autowired
  public AnalyticsServiceImpl(
      AnalyticsRepository analyticsRepository, SettingsService settingsService) {
    this.analyticsRepository = analyticsRepository;
    this.settingsService = settingsService;
  }

  @Override
  @Transactional(readOnly = true)
  public InventoryOverviewResponse getOverview() {
    AnalyticsProjection.Overview overview = analyticsRepository.getOverview(lowStockThreshold());
    if (overview == null) overview = analyticsRepository.getOverview();
    long totalProducts = valueOrZero(overview.getTotalProducts());
    long lowStockProducts = valueOrZero(overview.getLowStockProducts());
    BigDecimal lowStockRate =
        totalProducts == 0
            ? BigDecimal.ZERO.setScale(2)
            : BigDecimal.valueOf(lowStockProducts)
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(totalProducts), 2, RoundingMode.HALF_UP);
    return new InventoryOverviewResponse(
        totalProducts,
        valueOrZero(overview.getActiveProducts()),
        lowStockProducts,
        valueOrZero(overview.getOutOfStockProducts()),
        valueOrZero(overview.getInactiveProducts()),
        valueOrZero(overview.getTotalStockQuantity()),
        decimalOrZero(overview.getTotalInventoryValue()),
        decimalOrZero(overview.getAverageProductPrice()),
        lowStockRate);
  }

  @Override
  @Transactional(readOnly = true)
  public List<InventoryByStatusResponse> getInventoryByStatus() {
    var statusSummary =
        settingsService == null
            ? analyticsRepository.getInventoryByStatus()
            : analyticsRepository.getInventoryByStatus(lowStockThreshold());
    if (statusSummary == null) statusSummary = java.util.List.of();
    return statusSummary.stream()
        .map(
            item ->
                new InventoryByStatusResponse(
                    ProductStatus.valueOf(item.getStatus()),
                    valueOrZero(item.getProductCount()),
                    valueOrZero(item.getTotalStockQuantity()),
                    decimalOrZero(item.getInventoryValue())))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<InventoryProductResponse> getTopInventoryValue(int limit) {
    var topProducts =
        settingsService == null
            ? analyticsRepository.getTopInventoryValue(limit)
            : analyticsRepository.getTopInventoryValue(limit, lowStockThreshold());
    if (topProducts == null) topProducts = java.util.List.of();
    return topProducts.stream()
        .map(this::toProductResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<InventoryProductResponse> getLowStock(int limit) {
    var lowStock =
        settingsService == null
            ? analyticsRepository.getLowStock(limit)
            : analyticsRepository.getLowStock(limit, lowStockThreshold());
    if (lowStock == null) lowStock = java.util.List.of();
    return lowStock.stream()
        .map(this::toProductResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<StockDistributionResponse> getStockDistribution() {
    int threshold = lowStockThreshold();
    var distribution =
        settingsService == null
            ? analyticsRepository.getStockDistribution()
            : analyticsRepository.getStockDistribution(threshold);
    if (distribution == null) distribution = java.util.List.of();
    return distribution.stream()
        .map(
            item ->
                new StockDistributionResponse(
                    item.getBucket(),
                    distributionLabel(item.getBucket(), threshold),
                    valueOrZero(item.getProductCount()),
                    valueOrZero(item.getTotalStockQuantity()),
                    decimalOrZero(item.getInventoryValue())))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public TrendResponse getTrends(LocalDate from, LocalDate to, String granularity) {
    LocalDate actualTo = to == null ? LocalDate.now() : to;
    LocalDate actualFrom = from == null ? actualTo.minusDays(30) : from;
    if (actualFrom.isAfter(actualTo)) {
      throw new InvalidRequestException("from must be before or equal to to");
    }

    String actualGranularity = normalizeGranularity(granularity);
    if (analyticsRepository.countSnapshotDates(actualFrom, actualTo) < 2) {
      return new TrendResponse(
          "INSUFFICIENT_DATA",
          "Chưa đủ dữ liệu lịch sử để hiển thị xu hướng.",
          actualGranularity,
          List.of());
    }

    List<TrendPointResponse> points =
        analyticsRepository
            .getTrends(actualFrom, actualTo, actualGranularity.toLowerCase(Locale.ROOT))
            .stream()
            .map(
                point ->
                    new TrendPointResponse(
                        point.getPeriod(),
                        valueOrZero(point.getProductCount()),
                        valueOrZero(point.getTotalStockQuantity()),
                        decimalOrZero(point.getTotalInventoryValue())))
            .toList();
    return new TrendResponse(
        "READY", "Đã có đủ dữ liệu lịch sử để hiển thị xu hướng.", actualGranularity, points);
  }

  private InventoryProductResponse toProductResponse(AnalyticsProjection.InventoryProduct item) {
    return new InventoryProductResponse(
        item.getProductId(),
        item.getProductCode(),
        item.getName(),
        decimalOrZero(item.getPrice()),
        item.getStockQuantity() == null ? 0 : item.getStockQuantity(),
        ProductStatus.valueOf(item.getStatus()),
        decimalOrZero(item.getInventoryValue()));
  }

  private String normalizeGranularity(String granularity) {
    String normalized = granularity == null ? "DAY" : granularity.trim().toUpperCase(Locale.ROOT);
    if (!normalized.equals("DAY") && !normalized.equals("WEEK") && !normalized.equals("MONTH")) {
      throw new InvalidRequestException("granularity must be DAY, WEEK or MONTH");
    }
    return normalized;
  }

  private String distributionLabel(String bucket, int threshold) {
    return switch (bucket) {
      case "OUT_OF_STOCK" -> "Hết hàng";
      case "LOW_STOCK" -> "Dưới " + threshold + " sản phẩm";
      case "10_49" -> "10 - 49 sản phẩm";
      case "50_99" -> "50 - 99 sản phẩm";
      case "100_PLUS" -> "Từ 100 sản phẩm";
      default -> bucket;
    };
  }

  private int lowStockThreshold() {
    return settingsService == null ? 10 : settingsService.getCurrent().lowStockThreshold();
  }

  private long valueOrZero(Long value) {
    return value == null ? 0 : value;
  }

  private BigDecimal decimalOrZero(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }
}
