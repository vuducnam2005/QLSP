package com.example.productmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProductManagementApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProductManagementApplication.class, args);
  }
}
