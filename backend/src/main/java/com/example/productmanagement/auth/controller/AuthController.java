package com.example.productmanagement.auth.controller;

import com.example.productmanagement.auth.dto.AuthUserResponse;
import com.example.productmanagement.auth.dto.LoginRequest;
import com.example.productmanagement.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final SecurityContextRepository securityContextRepository;
  private final CsrfTokenRepository csrfTokenRepository;

  public AuthController(
      AuthenticationManager authenticationManager,
      SecurityContextRepository securityContextRepository,
      CsrfTokenRepository csrfTokenRepository) {
    this.authenticationManager = authenticationManager;
    this.securityContextRepository = securityContextRepository;
    this.csrfTokenRepository = csrfTokenRepository;
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthUserResponse>> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) {
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.username(), request.password()));

      SecurityContext context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(authentication);
      SecurityContextHolder.setContext(context);
      securityContextRepository.saveContext(context, httpRequest, httpResponse);

      return ResponseEntity.ok(
          ApiResponse.success(
              HttpStatus.OK.value(), "Đăng nhập thành công", toUserResponse(authentication)));
    } catch (AuthenticationException exception) {
      SecurityContextHolder.clearContext();
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.error(401, "Tên đăng nhập hoặc mật khẩu không đúng", null));
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    new SecurityContextLogoutHandler().logout(request, response, authentication);
    return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Đã đăng xuất", null));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<AuthUserResponse>> currentUser(Authentication authentication) {
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication.getName().equals("anonymousUser")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.error(401, "Vui lòng đăng nhập để tiếp tục", null));
    }
    return ResponseEntity.ok(
        ApiResponse.success(
            HttpStatus.OK.value(), "Đã tải thông tin tài khoản", toUserResponse(authentication)));
  }

  @GetMapping("/csrf")
  public ResponseEntity<ApiResponse<String>> csrf(
      HttpServletRequest request, HttpServletResponse response) {
    CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
    csrfTokenRepository.saveToken(csrfToken, request, response);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "CSRF token", csrfToken.getToken()));
  }

  private AuthUserResponse toUserResponse(Authentication authentication) {
    String role =
        authentication.getAuthorities().stream()
            .findFirst()
            .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
            .orElse("ADMIN");
    return new AuthUserResponse(authentication.getName(), role, true);
  }
}
