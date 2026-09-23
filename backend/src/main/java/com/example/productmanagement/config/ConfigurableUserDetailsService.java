package com.example.productmanagement.config;

import com.example.productmanagement.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ConfigurableUserDetailsService implements UserDetailsService {

  private final SettingsRepository settingsRepository;
  private final PasswordEncoder passwordEncoder;
  private final String configuredUsername;
  private final String configuredPassword;

  public ConfigurableUserDetailsService(
      SettingsRepository settingsRepository,
      PasswordEncoder passwordEncoder,
      @Value("${app.security.username}") String configuredUsername,
      @Value("${app.security.password}") String configuredPassword) {
    this.settingsRepository = settingsRepository;
    this.passwordEncoder = passwordEncoder;
    this.configuredUsername = configuredUsername;
    this.configuredPassword = configuredPassword;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    if (!configuredUsername.equals(username)) {
      throw new UsernameNotFoundException("User not found");
    }

    String password =
        settingsRepository
            .findByUsername(username)
            .map(settings -> settings.getPasswordHash())
            .filter(hash -> hash != null && !hash.isBlank())
            .orElseGet(() -> passwordEncoder.encode(configuredPassword));
    return User.withUsername(username).password(password).roles("ADMIN").build();
  }
}
