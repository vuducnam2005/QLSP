package com.example.productmanagement.analytics.entity;

import com.example.productmanagement.enums.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "inventory_snapshots",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_inventory_snapshots_product_date",
            columnNames = {"product_id", "snapshot_date"}))
public class InventorySnapshot {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "snapshot_date", nullable = false)
  private LocalDate snapshotDate;

  @Column(name = "stock_quantity", nullable = false)
  private Integer stockQuantity;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal price;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ProductStatus status;

  @Column(name = "inventory_value", nullable = false, precision = 20, scale = 2)
  private BigDecimal inventoryValue;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
