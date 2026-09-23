package com.example.productmanagement.analytics.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventorySnapshotRepository
    extends JpaRepository<com.example.productmanagement.analytics.entity.InventorySnapshot, Long> {

  @Modifying
  @Query(
      value =
          """
          INSERT INTO inventory_snapshots
            (product_id, snapshot_date, stock_quantity, price, status, inventory_value)
          SELECT
            id, :snapshotDate, stock_quantity, price, status, price * stock_quantity
          FROM products
          WHERE is_deleted = FALSE
          ON CONFLICT (product_id, snapshot_date) DO UPDATE SET
            stock_quantity = EXCLUDED.stock_quantity,
            price = EXCLUDED.price,
            status = EXCLUDED.status,
            inventory_value = EXCLUDED.inventory_value
          """,
      nativeQuery = true)
  int upsertSnapshot(@Param("snapshotDate") LocalDate snapshotDate);
}
