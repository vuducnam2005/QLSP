package com.example.productmanagement.service;

import com.example.productmanagement.dto.request.ProductCreateRequest;
import com.example.productmanagement.dto.request.ProductUpdateRequest;
import com.example.productmanagement.dto.response.ProductResponse;
import com.example.productmanagement.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

  ProductResponse create(ProductCreateRequest request);

  Page<ProductResponse> getAll(String keyword, ProductStatus status, Pageable pageable);

  ProductResponse getById(Long id);

  ProductResponse update(Long id, ProductUpdateRequest request);

  void delete(Long id);
}
