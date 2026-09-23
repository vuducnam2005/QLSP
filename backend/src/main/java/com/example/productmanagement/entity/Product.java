package com.example.productmanagement.entity;

import com.example.productmanagement.enums.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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
@Table(
    name = "products",
    indexes = {
      @Index(name = "idx_products_name", columnList = "name"),
      @Index(name = "idx_products_status_deleted", columnList = "status, is_deleted")
    })
@EntityListeners(AuditingEntityListener.class)
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @jakarta.persistence.Version
  @Column(nullable = false)
  private Long version;

  @NotNull
  @Column(name = "product_code", nullable = false, unique = true, length = 50)
  private String productCode;

  @NotNull
  @Column(nullable = false, length = 255)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @NotNull
  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal price;

  @NotNull
  @Column(name = "stock_quantity", nullable = false)
  private Integer stockQuantity;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ProductStatus status;

  @NotNull
  @Column(name = "is_deleted", nullable = false)
  private boolean deleted;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
