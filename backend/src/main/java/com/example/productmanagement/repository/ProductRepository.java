package com.example.productmanagement.repository;

import com.example.productmanagement.entity.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository
    extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

  boolean existsByProductCode(String productCode);

  Optional<Product> findByIdAndDeletedFalse(Long id);

}
