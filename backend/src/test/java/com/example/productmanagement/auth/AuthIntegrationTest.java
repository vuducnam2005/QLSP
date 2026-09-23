package com.example.productmanagement.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.example.productmanagement.repository.SettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AuthIntegrationTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("product_management_auth_test")
          .withUsername("test_user")
          .withPassword("test_password");

  @DynamicPropertySource
  static void configureDatasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @Autowired private MockMvc mockMvc;

  @Autowired private SettingsRepository settingsRepository;

  @BeforeEach
  void resetPersistedSettings() {
    settingsRepository.deleteAll();
  }

  @Test
  void protectedEndpointsRejectAnonymousRequests() throws Exception {
    mockMvc
        .perform(get("/api/v1/auth/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false));
    mockMvc
        .perform(get("/api/v1/products"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void correctCredentialsCreateSessionAndLogoutInvalidatesIt() throws Exception {
    MvcResult login =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username").value("admin"))
            .andExpect(jsonPath("$.data.role").value("ADMIN"))
            .andExpect(jsonPath("$.data.password").doesNotExist())
            .andReturn();

    MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

    mockMvc
        .perform(get("/api/v1/auth/me").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.authenticated").value(true));
    mockMvc.perform(get("/api/v1/products").session(session)).andExpect(status().isOk());

    mockMvc
        .perform(post("/api/v1/auth/logout").with(csrf()).session(session))
        .andExpect(status().isOk());
    mockMvc
        .perform(get("/api/v1/products").session(session))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void wrongCredentialsAreRejected() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"incorrect\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Tên đăng nhập hoặc mật khẩu không đúng"));
  }

  @Test
  void authenticatedWritePassesCsrfProtection() throws Exception {
    MvcResult login =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
            .andExpect(status().isOk())
            .andReturn();

    MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
    String updatePayload =
        """
                {
                  "name": "Bàn phím cơ K87",
                  "description": "Bàn phím cơ hot-swap nhỏ gọn với đèn nền RGB.",
                  "price": 2247500,
                  "stockQuantity": 200,
                  "status": "LOW_STOCK",
                  "version": 0
                }
                """;

    mockMvc
        .perform(
            put("/api/v1/products/{id}", 1L)
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  void settingsPasswordChangePersistsAcrossLogin() throws Exception {
    MvcResult login =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
            .andExpect(status().isOk())
            .andReturn();

    MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
    mockMvc
        .perform(
            put("/api/v1/settings")
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "lowStockThreshold": 10,
                      "productCodePrefix": "PRD-",
                      "allowNegativeStock": false,
                      "workspaceName": "Không gian làm việc",
                      "currency": "VND",
                      "dateFormat": "DD/MM/YYYY",
                      "password": {
                        "currentPassword": "admin123",
                        "newPassword": "newadmin123",
                        "confirmPassword": "newadmin123"
                      }
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
        .andExpect(status().isUnauthorized());
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"newadmin123\"}"))
        .andExpect(status().isOk());
  }
}
