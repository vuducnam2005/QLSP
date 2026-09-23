package com.example.productmanagement.analytics.service.impl;

import com.example.productmanagement.analytics.repository.InventorySnapshotRepository;
import com.example.productmanagement.analytics.service.AnalyticsSnapshotService;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsSnapshotServiceImpl implements AnalyticsSnapshotService {

  private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  private final InventorySnapshotRepository inventorySnapshotRepository;

  public AnalyticsSnapshotServiceImpl(InventorySnapshotRepository inventorySnapshotRepository) {
    this.inventorySnapshotRepository = inventorySnapshotRepository;
  }

  @Override
  @Transactional
  @Scheduled(
      cron = "${app.analytics.snapshot-cron:0 5 0 * * *}",
      zone = "${app.analytics.snapshot-zone:Asia/Ho_Chi_Minh}")
  public int captureSnapshot() {
    return inventorySnapshotRepository.upsertSnapshot(LocalDate.now(BUSINESS_ZONE));
  }
}
