package com.example.productmanagement.service;

import com.example.productmanagement.dto.request.SettingsUpdateRequest;
import com.example.productmanagement.dto.response.SettingsResponse;

public interface SettingsService {

  SettingsResponse getCurrent();

  SettingsResponse updateCurrent(SettingsUpdateRequest request);
}
