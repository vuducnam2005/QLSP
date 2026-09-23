package com.example.productmanagement.service.impl;

import com.example.productmanagement.dto.request.PasswordChangeRequest;
import com.example.productmanagement.dto.request.SettingsUpdateRequest;
import com.example.productmanagement.dto.response.SettingsResponse;
import com.example.productmanagement.entity.Settings;
import com.example.productmanagement.exception.InvalidRequestException;
import com.example.productmanagement.repository.SettingsRepository;
import com.example.productmanagement.service.SettingsService;
import java.util.Locale;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsServiceImpl implements SettingsService {

  private static final String DEFAULT_USERNAME = "admin";

  private final SettingsRepository settingsRepository;
  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;

  public SettingsServiceImpl(
      SettingsRepository settingsRepository,
      UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder) {
    this.settingsRepository = settingsRepository;
    this.userDetailsService = userDetailsService;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public SettingsResponse getCurrent() {
    String username = currentUsername();
    Settings settings =
        settingsRepository.findByUsername(username).orElseGet(() -> defaultSettings(username));
    return toResponse(settings);
  }

  @Override
  @Transactional
  public SettingsResponse updateCurrent(SettingsUpdateRequest request) {
    String username = currentUsername();
    Settings settings =
        settingsRepository.findByUsername(username).orElseGet(() -> createDefaults(username));

    settings.setLowStockThreshold(request.getLowStockThreshold());
    String prefix = request.getProductCodePrefix().trim().toUpperCase(Locale.ROOT);
    if (!prefix.endsWith("-")) prefix += "-";
    if (prefix.length() > 20) {
      throw new InvalidRequestException("productCodePrefix must not exceed 20 characters");
    }
    settings.setProductCodePrefix(prefix);
    settings.setAllowNegativeStock(request.getAllowNegativeStock());
    settings.setWorkspaceName(request.getWorkspaceName().trim());
    settings.setCurrency(request.getCurrency().trim().toUpperCase(Locale.ROOT));
    settings.setDateFormat(request.getDateFormat().trim().toUpperCase(Locale.ROOT));

    updatePasswordIfRequested(request.getPassword(), username, settings);
    return toResponse(settingsRepository.save(settings));
  }

  private Settings createDefaults(String username) {
    return settingsRepository.save(defaultSettings(username));
  }

  private Settings defaultSettings(String username) {
    Settings settings = new Settings();
    settings.setUsername(username);
    settings.setLowStockThreshold(10);
    settings.setProductCodePrefix("PRD-");
    settings.setAllowNegativeStock(false);
    settings.setWorkspaceName("Không gian làm việc");
    settings.setCurrency("VND");
    settings.setDateFormat("DD/MM/YYYY");
    return settings;
  }

  private void updatePasswordIfRequested(
      PasswordChangeRequest request, String username, Settings settings) {
    if (request == null) return;
    if (isBlank(request.getCurrentPassword())
        || isBlank(request.getNewPassword())
        || isBlank(request.getConfirmPassword())) {
      throw new InvalidRequestException("Vui lòng nhập đầy đủ thông tin đổi mật khẩu");
    }
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new InvalidRequestException("Mật khẩu mới và xác nhận mật khẩu không khớp");
    }

    var currentUser = userDetailsService.loadUserByUsername(username);
    if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
      throw new InvalidRequestException("Mật khẩu hiện tại không đúng");
    }

    settings.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
  }

  private SettingsResponse toResponse(Settings settings) {
    var user = userDetailsService.loadUserByUsername(settings.getUsername());
    String role =
        user.getAuthorities().stream()
            .findFirst()
            .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
            .orElse("ADMIN");
    return new SettingsResponse(
        settings.getId() == null ? 0L : settings.getId(),
        settings.getUsername(),
        settings.getLowStockThreshold(),
        settings.getProductCodePrefix(),
        settings.isAllowNegativeStock(),
        settings.getWorkspaceName(),
        settings.getCurrency(),
        settings.getDateFormat(),
        new SettingsResponse.AccountResponse(settings.getUsername(), role));
  }

  private String currentUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getName())) {
      return DEFAULT_USERNAME;
    }
    return authentication.getName();
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
