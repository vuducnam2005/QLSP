package com.example.productmanagement.config;

import com.example.productmanagement.common.ApiConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI productManagementOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Product Management API")
                .version("v1")
                .description("RESTful API for product management")
                .contact(new Contact().name("Product Management Team")));
  }

  @Bean
  public OpenApiCustomizer productResponseExamples() {
    return openAPI -> openAPI.getPaths().forEach((path, pathItem) -> customizePath(path, pathItem));
  }

  private void customizePath(String path, PathItem pathItem) {
    if (!path.startsWith(ApiConstants.PRODUCTS_BASE_PATH)) {
      return;
    }

    boolean itemPath = path.endsWith("/{id}");
    customizeOperation(pathItem.getGet(), "get", itemPath);
    customizeOperation(pathItem.getPost(), "post", itemPath);
    customizeOperation(pathItem.getPut(), "put", itemPath);
    customizeOperation(pathItem.getDelete(), "delete", itemPath);
  }

  private void customizeOperation(Operation operation, String method, boolean itemPath) {
    if (operation == null) {
      return;
    }

    if ("get".equals(method) && itemPath) {
      addExample(operation, "200", productExample("Product retrieved successfully", 200));
      addExample(operation, "404", errorExample("Product not found with id: 999999999", 404));
    } else if ("get".equals(method)) {
      addExample(operation, "200", listExample());
      addExample(operation, "400", validationExample());
    } else if ("post".equals(method)) {
      addExample(operation, "201", productExample("Product created successfully", 201));
      addExample(operation, "400", validationExample());
      addExample(operation, "409", errorExample("Product code already exists: PRD-0001", 409));
    } else if ("put".equals(method) && itemPath) {
      addExample(operation, "200", productExample("Product updated successfully", 200));
      addExample(operation, "400", validationExample());
      addExample(operation, "404", errorExample("Product not found with id: 999999999", 404));
      addExample(
          operation,
          "409",
          errorExample(
              "The product version is stale. Reload the product and retry with the latest version",
              409));
    } else if ("delete".equals(method) && itemPath) {
      addExample(operation, "200", errorExample("Product deleted successfully", 200, true));
      addExample(operation, "404", errorExample("Product not found with id: 999999999", 404));
    }
  }

  private void addExample(Operation operation, String statusCode, String example) {
    ApiResponse response = operation.getResponses().get(statusCode);
    if (response == null) {
      response = new ApiResponse().description("Response " + statusCode);
      operation.getResponses().addApiResponse(statusCode, response);
    }

    Content content = response.getContent();
    if (content == null) {
      content = new Content();
      response.setContent(content);
    }

    String mediaTypeName = content.containsKey("*/*") ? "*/*" : "application/json";
    MediaType mediaType = content.get(mediaTypeName);
    if (mediaType == null) {
      mediaType = new MediaType();
      content.addMediaType(mediaTypeName, mediaType);
    }
    mediaType.setExample(example);
  }

  private String listExample() {
    return """
        {
          "success": true,
          "statusCode": 200,
          "message": "Products retrieved successfully",
          "data": {
            "items": [],
            "pageNo": 0,
            "pageSize": 10,
            "totalElements": 0,
            "totalPages": 0,
            "last": true
          },
          "timestamp": "2026-01-01T00:00:00Z"
        }
        """;
  }

  private String productExample(String message, int statusCode) {
    return """
        {
          "success": true,
          "statusCode": %d,
          "message": "%s",
          "data": {
            "id": 1,
            "version": 0,
            "productCode": "PRD-0001",
            "name": "Mechanical Keyboard K87",
            "description": "Compact mechanical keyboard",
            "price": 89.90,
            "stockQuantity": 120,
            "status": "ACTIVE",
            "createdAt": "2026-01-01T00:00:00Z",
            "updatedAt": "2026-01-01T00:00:00Z"
          },
          "timestamp": "2026-01-01T00:00:00Z"
        }
        """
        .formatted(statusCode, message);
  }

  private String validationExample() {
    return """
        {
          "success": false,
          "statusCode": 400,
          "message": "Validation failed",
          "data": {
            "productCode": "productCode must follow the format PRD-xxxx"
          },
          "timestamp": "2026-01-01T00:00:00Z"
        }
        """;
  }

  private String errorExample(String message, int statusCode) {
    return errorExample(message, statusCode, false);
  }

  private String errorExample(String message, int statusCode, boolean success) {
    return """
        {
          "success": %s,
          "statusCode": %d,
          "message": "%s",
          "data": null,
          "timestamp": "2026-01-01T00:00:00Z"
        }
        """
        .formatted(success, statusCode, message);
  }
}
