package com.example.productmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "settings")
@EntityListeners(AuditingEntityListener.class)
public class Settings {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String username;

  @Column(name = "low_stock_threshold", nullable = false)
  private Integer lowStockThreshold;

  @Column(name = "product_code_prefix", nullable = false, length = 20)
  private String productCodePrefix;

  @Column(name = "allow_negative_stock", nullable = false)
  private boolean allowNegativeStock;

  @Column(name = "workspace_name", nullable = false, length = 120)
  private String workspaceName;

  @Column(nullable = false, length = 3)
  private String currency;

  @Column(name = "date_format", nullable = false, length = 20)
  private String dateFormat;

  @Column(name = "password_hash", length = 100)
  private String passwordHash;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
