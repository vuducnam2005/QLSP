package com.example.productmanagement.controller;

import com.example.productmanagement.common.ApiConstants;
import com.example.productmanagement.common.ApiResponse;
import com.example.productmanagement.common.PageResponse;
import com.example.productmanagement.dto.request.ProductCreateRequest;
import com.example.productmanagement.dto.request.ProductUpdateRequest;
import com.example.productmanagement.dto.response.ProductResponse;
import com.example.productmanagement.enums.ProductStatus;
import com.example.productmanagement.exception.InvalidRequestException;
import com.example.productmanagement.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(ApiConstants.PRODUCTS_BASE_PATH)
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of(
          "id",
          "productCode",
          "name",
          "price",
          "stockQuantity",
          "status",
          "createdAt",
          "updatedAt");

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @PostMapping
  @Operation(summary = "Create a product")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "Product created"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Validation failed"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "Product code already exists")
  })
  public ResponseEntity<ApiResponse<ProductResponse>> create(
      @Valid @RequestBody ProductCreateRequest request) {
    ProductResponse response = productService.create(request);
    return ResponseEntity.created(
            URI.create(ApiConstants.PRODUCTS_BASE_PATH + "/" + response.getId()))
        .body(
            ApiResponse.success(
                HttpStatus.CREATED.value(), "Product created successfully", response));
  }

  @GetMapping
  @Operation(summary = "Search products with pagination and filters")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Products returned"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Invalid pagination or sorting parameters")
  })
  public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAll(
      @Parameter(description = "Zero-based page number", in = ParameterIn.QUERY)
          @RequestParam(defaultValue = "0")
          @Min(0)
          int page,
      @Parameter(description = "Page size from 1 to 100", in = ParameterIn.QUERY)
          @RequestParam(defaultValue = "10")
          @Min(1)
          @Max(ApiConstants.MAX_PAGE_SIZE)
          int size,
      @Parameter(
              description = "Sort in the format field,direction",
              in = ParameterIn.QUERY,
              schema = @Schema(defaultValue = ApiConstants.DEFAULT_SORT))
          @RequestParam(defaultValue = ApiConstants.DEFAULT_SORT)
          String sort,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) ProductStatus status) {
    Pageable pageable = createPageable(page, size, sort);
    Page<ProductResponse> result = productService.getAll(keyword, status, pageable);
    PageResponse<ProductResponse> data =
        new PageResponse<>(
            result.getContent(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages(),
            result.isLast());
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Products retrieved successfully", data));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a product by id")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Product returned"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "Product not found")
  })
  public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
    return ResponseEntity.ok(
        ApiResponse.success(
            HttpStatus.OK.value(), "Product retrieved successfully", productService.getById(id)));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a product")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Product updated"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Validation failed"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "Product not found")
  })
  public ResponseEntity<ApiResponse<ProductResponse>> update(
      @PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            HttpStatus.OK.value(),
            "Product updated successfully",
            productService.update(id, request)));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Soft-delete a product")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Product deleted"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "Product not found")
  })
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    productService.delete(id);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Product deleted successfully", null));
  }

  private Pageable createPageable(int page, int size, String sortParameter) {
    String[] sortParts = sortParameter.split(",", -1);
    if (sortParts.length > 2 || sortParts[0].isBlank()) {
      throw new InvalidRequestException("sort must use the format field,direction");
    }

    String property = sortParts[0].trim();
    if (!ALLOWED_SORT_FIELDS.contains(property)) {
      throw new InvalidRequestException("Unsupported sort field: " + property);
    }

    Sort.Direction direction = Sort.Direction.DESC;
    if (sortParts.length == 2 && !sortParts[1].isBlank()) {
      try {
        direction = Sort.Direction.fromString(sortParts[1].trim());
      } catch (IllegalArgumentException exception) {
        throw new InvalidRequestException("Sort direction must be asc or desc");
      }
    }
    return PageRequest.of(page, size, Sort.by(direction, property));
  }
}
