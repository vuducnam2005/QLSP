package com.example.productmanagement.analytics.repository;

import com.example.productmanagement.entity.Product;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface AnalyticsRepository extends Repository<Product, Long> {

  @Query(
      value =
          """
          SELECT
            COUNT(*) AS "totalProducts",
            COUNT(*) FILTER (WHERE status = 'ACTIVE') AS "activeProducts",
            COUNT(*) FILTER (WHERE status = 'LOW_STOCK' OR stock_quantity < :threshold) AS "lowStockProducts",
            COUNT(*) FILTER (WHERE stock_quantity = 0) AS "outOfStockProducts",
            COUNT(*) FILTER (WHERE status = 'INACTIVE') AS "inactiveProducts",
            COALESCE(SUM(stock_quantity), 0) AS "totalStockQuantity",
            COALESCE(SUM(price * stock_quantity), 0) AS "totalInventoryValue",
            COALESCE(AVG(price), 0) AS "averageProductPrice"
          FROM products
          WHERE is_deleted = FALSE
          """,
      nativeQuery = true)
  AnalyticsProjection.Overview getOverview(@Param("threshold") int threshold);

  default AnalyticsProjection.Overview getOverview() {
    return getOverview(10);
  }

  @Query(
      value =
          """
          SELECT
            status AS "status",
            COUNT(*) AS "productCount",
            COALESCE(SUM(stock_quantity), 0) AS "totalStockQuantity",
            COALESCE(SUM(price * stock_quantity), 0) AS "inventoryValue"
          FROM products
          WHERE is_deleted = FALSE
          GROUP BY status
          ORDER BY CASE status WHEN 'ACTIVE' THEN 1 WHEN 'LOW_STOCK' THEN 2 ELSE 3 END
          """,
      nativeQuery = true)
  List<AnalyticsProjection.StatusSummary> getInventoryByStatus();

  @Query(
      value =
          """
          SELECT
            inventory.status AS "status",
            COUNT(*) AS "productCount",
            COALESCE(SUM(inventory.stock_quantity), 0) AS "totalStockQuantity",
            COALESCE(SUM(inventory.price * inventory.stock_quantity), 0) AS "inventoryValue"
          FROM (
            SELECT
              stock_quantity,
              price,
              CASE WHEN status = 'INACTIVE' THEN 'INACTIVE'
                   WHEN status = 'LOW_STOCK' OR stock_quantity < :threshold THEN 'LOW_STOCK'
                   ELSE 'ACTIVE' END AS status
            FROM products
            WHERE is_deleted = FALSE
          ) inventory
          GROUP BY 1
          ORDER BY CASE inventory.status WHEN 'ACTIVE' THEN 1 WHEN 'LOW_STOCK' THEN 2 ELSE 3 END
          """,
      nativeQuery = true)
  List<AnalyticsProjection.StatusSummary> getInventoryByStatus(@Param("threshold") int threshold);

  @Query(
      value =
          """
          SELECT
            id AS "productId",
            product_code AS "productCode",
            name AS "name",
            price AS "price",
            stock_quantity AS "stockQuantity",
            status AS "status",
            (price * stock_quantity) AS "inventoryValue"
          FROM products
          WHERE is_deleted = FALSE
          ORDER BY (price * stock_quantity) DESC, id ASC
          LIMIT :limit
          """,
      nativeQuery = true)
  List<AnalyticsProjection.InventoryProduct> getTopInventoryValue(@Param("limit") int limit);

  @Query(
      value =
          """
          SELECT id AS "productId", product_code AS "productCode", name AS "name", price AS "price",
                 stock_quantity AS "stockQuantity",
                 CASE WHEN status = 'INACTIVE' THEN 'INACTIVE'
                      WHEN status = 'LOW_STOCK' OR stock_quantity < :threshold THEN 'LOW_STOCK'
                      ELSE 'ACTIVE' END AS "status",
                 (price * stock_quantity) AS "inventoryValue"
          FROM products
          WHERE is_deleted = FALSE
          ORDER BY (price * stock_quantity) DESC, id ASC
          LIMIT :limit
          """,
      nativeQuery = true)
  List<AnalyticsProjection.InventoryProduct> getTopInventoryValue(
      @Param("limit") int limit, @Param("threshold") int threshold);

  @Query(
      value =
          """
          SELECT
            id AS "productId",
            product_code AS "productCode",
            name AS "name",
            price AS "price",
            stock_quantity AS "stockQuantity",
            status AS "status",
            (price * stock_quantity) AS "inventoryValue"
          FROM products
          WHERE is_deleted = FALSE
            AND (status = 'LOW_STOCK' OR stock_quantity < :threshold)
          ORDER BY stock_quantity ASC, (price * stock_quantity) ASC, id ASC
          LIMIT :limit
          """,
      nativeQuery = true)
  List<AnalyticsProjection.InventoryProduct> getLowStock(
      @Param("limit") int limit, @Param("threshold") int threshold);

  default List<AnalyticsProjection.InventoryProduct> getLowStock(int limit) {
    return getLowStock(limit, 10);
  }

  @Query(
      value =
          """
          SELECT
            bucket AS "bucket",
            COUNT(*) AS "productCount",
            COALESCE(SUM(stock_quantity), 0) AS "totalStockQuantity",
            COALESCE(SUM(price * stock_quantity), 0) AS "inventoryValue"
          FROM (
            SELECT
              price,
              stock_quantity,
              CASE
                WHEN stock_quantity = 0 THEN 'OUT_OF_STOCK'
                WHEN stock_quantity < :threshold THEN 'LOW_STOCK'
                WHEN stock_quantity < 50 THEN '10_49'
                WHEN stock_quantity < 100 THEN '50_99'
                ELSE '100_PLUS'
              END AS bucket
            FROM products
            WHERE is_deleted = FALSE
          ) inventory
          GROUP BY bucket
          ORDER BY CASE bucket
            WHEN 'OUT_OF_STOCK' THEN 1
            WHEN 'LOW_STOCK' THEN 2
            WHEN '1_9' THEN 2
            WHEN '10_49' THEN 3
            WHEN '50_99' THEN 4
            ELSE 5
          END
          """,
      nativeQuery = true)
  List<AnalyticsProjection.Distribution> getStockDistribution(@Param("threshold") int threshold);

  default List<AnalyticsProjection.Distribution> getStockDistribution() {
    return getStockDistribution(10);
  }

  @Query(
      value =
          """
          SELECT COUNT(DISTINCT snap.snapshot_date)
          FROM inventory_snapshots snap
          JOIN products ON products.id = snap.product_id
          WHERE snap.snapshot_date BETWEEN :fromDate AND :toDate
            AND products.is_deleted = FALSE
          """,
      nativeQuery = true)
  long countSnapshotDates(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

  @Query(
      value =
          """
          SELECT
            DATE_TRUNC(:granularity, snap.snapshot_date::timestamp)::date AS "period",
            COUNT(DISTINCT snap.product_id) AS "productCount",
            COALESCE(SUM(snap.stock_quantity), 0) AS "totalStockQuantity",
            COALESCE(SUM(snap.inventory_value), 0) AS "totalInventoryValue"
          FROM inventory_snapshots snap
          JOIN products ON products.id = snap.product_id
          WHERE snap.snapshot_date BETWEEN :fromDate AND :toDate
            AND products.is_deleted = FALSE
          GROUP BY 1
          ORDER BY 1
          """,
      nativeQuery = true)
  List<AnalyticsProjection.TrendPoint> getTrends(
      @Param("fromDate") LocalDate fromDate,
      @Param("toDate") LocalDate toDate,
      @Param("granularity") String granularity);
}
