package com.example.productmanagement.mapper;

import com.example.productmanagement.dto.request.ProductCreateRequest;
import com.example.productmanagement.dto.request.ProductUpdateRequest;
import com.example.productmanagement.dto.response.ProductResponse;
import com.example.productmanagement.entity.Product;
import com.example.productmanagement.enums.ProductStatus;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

  public Product toEntity(ProductCreateRequest request) {
    return toEntity(request, 10);
  }

  public Product toEntity(ProductCreateRequest request, int lowStockThreshold) {
    Product product = new Product();
    product.setProductCode(request.getProductCode().trim());
    product.setName(request.getName().trim());
    product.setDescription(normalizeDescription(request.getDescription()));
    product.setCategory(normalizeDescription(request.getCategory()));
    product.setBrand(normalizeDescription(request.getBrand()));
    product.setSupplier(normalizeDescription(request.getSupplier()));
    product.setUnit(normalizeDescription(request.getUnit()));
    product.setWarehouseLocation(normalizeDescription(request.getWarehouseLocation()));
    product.setWarrantyMonths(request.getWarrantyMonths());
    product.setBarcode(normalizeDescription(request.getBarcode()));
    product.setCostPrice(request.getCostPrice());
    product.setMinimumStock(request.getMinimumStock());
    product.setImageUrl(normalizeDescription(request.getImageUrl()));
    product.setPrice(request.getPrice());
    product.setStockQuantity(request.getStockQuantity());
    product.setStatus(
        ProductStatus.resolve(
            request.getStockQuantity(),
            request.getStatus(),
            request.getMinimumStock(),
            lowStockThreshold));
    product.setDeleted(false);
    return product;
  }

  public void updateEntity(Product product, ProductUpdateRequest request) {
    updateEntity(product, request, 10);
  }

  public void updateEntity(Product product, ProductUpdateRequest request, int lowStockThreshold) {
    product.setName(request.getName().trim());
    product.setDescription(normalizeDescription(request.getDescription()));
    product.setCategory(normalizeDescription(request.getCategory()));
    product.setBrand(normalizeDescription(request.getBrand()));
    product.setSupplier(normalizeDescription(request.getSupplier()));
    product.setUnit(normalizeDescription(request.getUnit()));
    product.setWarehouseLocation(normalizeDescription(request.getWarehouseLocation()));
    product.setWarrantyMonths(request.getWarrantyMonths());
    product.setBarcode(normalizeDescription(request.getBarcode()));
    product.setCostPrice(request.getCostPrice());
    product.setMinimumStock(request.getMinimumStock());
    product.setImageUrl(normalizeDescription(request.getImageUrl()));
    product.setPrice(request.getPrice());
    product.setStockQuantity(request.getStockQuantity());
    product.setStatus(
        ProductStatus.resolve(
            request.getStockQuantity(),
            request.getStatus(),
            request.getMinimumStock(),
            lowStockThreshold));
  }

  public ProductResponse toResponse(Product product) {
    return toResponse(product, 10);
  }

  public ProductResponse toResponse(Product product, int lowStockThreshold) {
    return new ProductResponse(
        product.getId(),
        product.getVersion(),
        product.getProductCode(),
        product.getName(),
        product.getDescription(),
        product.getCategory(),
        product.getBrand(),
        product.getSupplier(),
        product.getUnit(),
        product.getWarehouseLocation(),
        product.getWarrantyMonths(),
        product.getBarcode(),
        product.getCostPrice(),
        product.getMinimumStock(),
        product.getImageUrl(),
        product.getPrice(),
        product.getStockQuantity(),
        ProductStatus.resolve(
            product.getStockQuantity(),
            product.getStatus(),
            product.getMinimumStock(),
            lowStockThreshold),
        product.getCreatedAt(),
        product.getUpdatedAt());
  }

  private String normalizeDescription(String description) {
    return description == null || description.isBlank() ? null : description.trim();
  }
}
