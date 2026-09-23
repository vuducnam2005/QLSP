package com.example.productmanagement.controller;

import com.example.productmanagement.common.ApiConstants;
import com.example.productmanagement.common.ApiResponse;
import com.example.productmanagement.dto.request.SettingsUpdateRequest;
import com.example.productmanagement.dto.response.SettingsResponse;
import com.example.productmanagement.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.SETTINGS_BASE_PATH)
public class SettingsController {

  private final SettingsService settingsService;

  public SettingsController(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<SettingsResponse>> get() {
    return ResponseEntity.ok(
        ApiResponse.success(
            HttpStatus.OK.value(), "Đã tải cài đặt hệ thống", settingsService.getCurrent()));
  }

  @PutMapping
  public ResponseEntity<ApiResponse<SettingsResponse>> update(
      @Valid @RequestBody SettingsUpdateRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            HttpStatus.OK.value(), "Đã lưu cài đặt hệ thống", settingsService.updateCurrent(request)));
  }
}
