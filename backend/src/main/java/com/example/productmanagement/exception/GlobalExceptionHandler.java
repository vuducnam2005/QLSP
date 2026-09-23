package com.example.productmanagement.exception;

import com.example.productmanagement.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
      MethodArgumentNotValidException exception) {
    Map<String, String> errors = new LinkedHashMap<>();
    for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
      errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
    }
    return build(HttpStatus.BAD_REQUEST, "Validation failed", errors);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(
      ConstraintViolationException exception) {
    Map<String, String> errors = new LinkedHashMap<>();
    exception
        .getConstraintViolations()
        .forEach(
            violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage()));
    return build(HttpStatus.BAD_REQUEST, "Validation failed", errors);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException exception) {
    return build(HttpStatus.NOT_FOUND, exception.getMessage(), null);
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateResourceException exception) {
    return build(HttpStatus.CONFLICT, exception.getMessage(), null);
  }

  @ExceptionHandler({
    InvalidRequestException.class,
    MethodArgumentTypeMismatchException.class,
    HttpMessageNotReadableException.class,
    HttpMediaTypeNotSupportedException.class,
    MissingServletRequestParameterException.class,
    MissingPathVariableException.class
  })
  public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
    String message =
        exception instanceof InvalidRequestException
            ? exception.getMessage()
            : "Request contains invalid data";
    return build(HttpStatus.BAD_REQUEST, message, null);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException exception) {
    return build(
        HttpStatus.METHOD_NOT_ALLOWED, "HTTP method is not supported for this endpoint", null);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
      NoResourceFoundException exception) {
    return build(HttpStatus.NOT_FOUND, "The requested resource was not found", null);
  }

  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotAcceptable(
      HttpMediaTypeNotAcceptableException exception) {
    return build(
        HttpStatus.NOT_ACCEPTABLE, "The requested response format is not acceptable", null);
  }

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  public ResponseEntity<ApiResponse<Void>> handleOptimisticLocking(
      ObjectOptimisticLockingFailureException exception) {
    return build(
        HttpStatus.CONFLICT,
        "The product was changed by another request. Reload it and retry with the latest version",
        null);
  }

  @ExceptionHandler(OptimisticConcurrencyException.class)
  public ResponseEntity<ApiResponse<Void>> handleOptimisticConcurrency(
      OptimisticConcurrencyException exception) {
    return build(HttpStatus.CONFLICT, exception.getMessage(), null);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(
      DataIntegrityViolationException exception) {
    log.warn("Data integrity violation", exception);
    return build(HttpStatus.CONFLICT, "The request conflicts with existing data", null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpected(
      Exception exception, HttpServletRequest request) {
    log.error(
        "Unexpected error while processing {} {}",
        request.getMethod(),
        request.getRequestURI(),
        exception);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred", null);
  }

  private <T> ResponseEntity<ApiResponse<T>> build(HttpStatus status, String message, T data) {
    return ResponseEntity.status(status).body(ApiResponse.error(status.value(), message, data));
  }
}
