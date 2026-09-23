package com.example.productmanagement.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiConstants {
  public static final String PRODUCTS_BASE_PATH = "/api/v1/products";
  public static final String ANALYTICS_BASE_PATH = "/api/v1/analytics";
  public static final String SETTINGS_BASE_PATH = "/api/v1/settings";
  public static final String DEFAULT_SORT = "id,desc";
  public static final int DEFAULT_PAGE = 0;
  public static final int DEFAULT_PAGE_SIZE = 10;
  public static final int MAX_PAGE_SIZE = 100;
}
