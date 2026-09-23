package com.example.productmanagement.analytics.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrendResponse {

  private String status;
  private String message;
  private String granularity;
  private List<TrendPointResponse> points;
}
