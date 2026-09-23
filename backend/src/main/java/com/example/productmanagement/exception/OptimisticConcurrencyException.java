package com.example.productmanagement.exception;

public class OptimisticConcurrencyException extends RuntimeException {

  public OptimisticConcurrencyException(String message) {
    super(message);
  }
}
