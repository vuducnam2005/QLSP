package com.example.productmanagement.config;

import com.example.productmanagement.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRepository;

@Configuration
public class SecurityConfig {

  private static final String[] PUBLIC_PATHS = {
    "/api/v1/auth/login",
    "/api/v1/auth/me",
    "/api/v1/auth/csrf",
    "/api/v1/auth/logout",
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/v3/api-docs/**",
    "/actuator/health/**"
  };

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, ObjectMapper objectMapper, CsrfTokenRepository csrfTokenRepository)
      throws Exception {
    return http
        .cors(Customizer.withDefaults())
        .csrf(
            csrf ->
                csrf.csrfTokenRepository(csrfTokenRepository)
                    // SPA clients send the raw token returned by /auth/csrf.
                    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                    .ignoringRequestMatchers("/api/v1/auth/login", "/api/v1/auth/logout"))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .securityContext(
            securityContext -> securityContext.securityContextRepository(securityContextRepository()))
        .authorizeHttpRequests(
            authorize ->
                authorize.requestMatchers(PUBLIC_PATHS).permitAll().anyRequest().authenticated())
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(apiAuthenticationEntryPoint(objectMapper))
                    .accessDeniedHandler(apiAccessDeniedHandler(objectMapper)))
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable())
        .logout(logout -> logout.disable())
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider authenticationProvider(
      UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public SecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  @Bean
  public CsrfTokenRepository csrfTokenRepository() {
    return CookieCsrfTokenRepository.withHttpOnlyFalse();
  }

  private AuthenticationEntryPoint apiAuthenticationEntryPoint(ObjectMapper objectMapper) {
    return (request, response, exception) ->
        writeSecurityError(response, objectMapper, 401, "Vui lòng đăng nhập để tiếp tục");
  }

  private AccessDeniedHandler apiAccessDeniedHandler(ObjectMapper objectMapper) {
    return (request, response, exception) ->
        writeSecurityError(response, objectMapper, 403, "Bạn không có quyền truy cập tài nguyên này");
  }

  private void writeSecurityError(
      jakarta.servlet.http.HttpServletResponse response,
      ObjectMapper objectMapper,
      int statusCode,
      String message)
      throws IOException {
    response.setStatus(statusCode);
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    objectMapper.writeValue(response.getWriter(), ApiResponse.error(statusCode, message, null));
  }
}
