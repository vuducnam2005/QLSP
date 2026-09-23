package com.example.productmanagement.service.impl;

import com.example.productmanagement.dto.request.ProductCreateRequest;
import com.example.productmanagement.dto.request.ProductUpdateRequest;
import com.example.productmanagement.dto.response.ProductResponse;
import com.example.productmanagement.dto.response.SettingsResponse;
import com.example.productmanagement.entity.Product;
import com.example.productmanagement.enums.ProductStatus;
import com.example.productmanagement.exception.DuplicateResourceException;
import com.example.productmanagement.exception.OptimisticConcurrencyException;
import com.example.productmanagement.exception.ResourceNotFoundException;
import com.example.productmanagement.mapper.ProductMapper;
import com.example.productmanagement.repository.ProductRepository;
import com.example.productmanagement.repository.ProductSpecifications;
import com.example.productmanagement.service.ProductService;
import com.example.productmanagement.service.SettingsService;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;
  private final SettingsService settingsService;

  public ProductServiceImpl(
      ProductRepository productRepository,
      ProductMapper productMapper,
      SettingsService settingsService) {
    this.productRepository = productRepository;
    this.productMapper = productMapper;
    this.settingsService = settingsService;
  }

  @Override
  @Transactional
  public ProductResponse create(ProductCreateRequest request) {
    SettingsResponse settings = settingsService.getCurrent();
    validateStock(request.getStockQuantity(), settings);
    request.setProductCode(resolveProductCode(request.getProductCode(), settings));
    if (productRepository.existsByProductCode(request.getProductCode())) {
      throw new DuplicateResourceException(
          "Product code already exists: " + request.getProductCode());
    }

    Product product =
        productRepository.save(productMapper.toEntity(request, settings.lowStockThreshold()));
    return productMapper.toResponse(product);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ProductResponse> getAll(String keyword, ProductStatus status, Pageable pageable) {
    int threshold = settingsService.getCurrent().lowStockThreshold();
    return productRepository
        .findAll(ProductSpecifications.filter(keyword, status), pageable)
        .map(product -> productMapper.toResponse(product, threshold));
  }

  @Override
  @Transactional(readOnly = true)
  public ProductResponse getById(Long id) {
    return productMapper.toResponse(
        findActiveProduct(id), settingsService.getCurrent().lowStockThreshold());
  }

  @Override
  @Transactional
  public ProductResponse update(Long id, ProductUpdateRequest request) {
    SettingsResponse settings = settingsService.getCurrent();
    validateStock(request.getStockQuantity(), settings);
    Product product = findActiveProduct(id);
    if (!product.getVersion().equals(request.getVersion())) {
      throw new OptimisticConcurrencyException(
          "The product version is stale. Reload the product and retry with the latest version");
    }
    productMapper.updateEntity(product, request, settings.lowStockThreshold());
    return productMapper.toResponse(productRepository.saveAndFlush(product));
  }

  @Override
  @Transactional
  public void delete(Long id) {
    Product product = findActiveProduct(id);
    product.setDeleted(true);
    productRepository.save(product);
  }

  private Product findActiveProduct(Long id) {
    return productRepository
        .findByIdAndDeletedFalse(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
  }

  private void validateStock(Integer stockQuantity, SettingsResponse settings) {
    if (stockQuantity != null && !settings.allowNegativeStock() && stockQuantity < 0) {
      throw new com.example.productmanagement.exception.InvalidRequestException(
          "stockQuantity must be greater than or equal to 0");
    }
  }

  private String resolveProductCode(String requestedCode, SettingsResponse settings) {
    String prefix = settings.productCodePrefix();
    if (requestedCode == null || requestedCode.isBlank()) {
      for (int sequence = 1; sequence <= 9999; sequence++) {
        String candidate = prefix + String.format("%04d", sequence);
        if (!productRepository.existsByProductCode(candidate)) return candidate;
      }
      throw new com.example.productmanagement.exception.InvalidRequestException(
          "Không thể tự sinh thêm mã sản phẩm cho prefix hiện tại");
    }

    String normalized = requestedCode.trim();
    if (!Pattern.matches(Pattern.quote(prefix) + "\\d{4}", normalized)) {
      throw new com.example.productmanagement.exception.InvalidRequestException(
          "productCode must follow the configured product prefix and end with four digits");
    }
    return normalized;
  }
}
