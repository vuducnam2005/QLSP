package com.example.productmanagement.common;

import java.util.List;
import lombok.Getter;

@Getter
public class PageResponse<T> {

  private final List<T> items;
  private final int pageNo;
  private final int pageSize;
  private final long totalElements;
  private final int totalPages;
  private final boolean last;

  public PageResponse(
      List<T> items, int pageNo, int pageSize, long totalElements, int totalPages, boolean last) {
    this.items = items;
    this.pageNo = pageNo;
    this.pageSize = pageSize;
    this.totalElements = totalElements;
    this.totalPages = totalPages;
    this.last = last;
  }
}
