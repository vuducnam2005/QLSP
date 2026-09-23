package com.example.productmanagement.repository;

import com.example.productmanagement.entity.Product;
import com.example.productmanagement.enums.ProductStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {

  private ProductSpecifications() {}

  public static Specification<Product> filter(String keyword, ProductStatus status) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(criteriaBuilder.isFalse(root.get("deleted")));

      if (keyword != null && !keyword.isBlank()) {
        String search = "%" + keyword.trim().toLowerCase() + "%";
        predicates.add(
            criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), search),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("productCode")), search)));
      }

      if (status != null) {
        predicates.add(criteriaBuilder.equal(root.get("status"), status));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
