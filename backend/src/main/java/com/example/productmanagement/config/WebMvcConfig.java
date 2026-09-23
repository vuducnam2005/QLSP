package com.example.productmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private final String allowedOrigins;

  public WebMvcConfig(
      @Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins(allowedOrigins.split(","))
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("Content-Type", "Accept", "X-XSRF-TOKEN", "X-Requested-With")
        .allowCredentials(true)
        .maxAge(3600);
  }

  @Bean
  public FilterRegistrationBean<RequestIdFilter> requestIdFilter() {
    FilterRegistrationBean<RequestIdFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new RequestIdFilter());
    registration.addUrlPatterns("/api/*", "/actuator/*");
    registration.setOrder(1);
    return registration;
  }
}
