package vn.edu.fpt.fschool.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AuthSecurityMvcTest {

  @Autowired private WebApplicationContext context;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  @DisplayName("Bảo mật: /api/v1/auth/refresh-token là public endpoint (không bị chặn 401/403)")
  void refreshToken_shouldBePermitAll_notUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Bảo mật: /api/v1/auth/logout là public endpoint")
  void logout_shouldBePermitAll() throws Exception {
    mockMvc.perform(post("/api/v1/auth/logout")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("Bảo mật: /api/v1/auth/me yêu cầu Bearer token (chặn 403 khi chưa đăng nhập)")
  void me_shouldRequireAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Bảo mật: /api/v1/admin/users yêu cầu quyền ADMIN")
  void adminUsers_shouldRequireAdminRole() throws Exception {
    mockMvc.perform(get("/api/v1/admin/users")).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Bảo mật: /api/v1/notifications yêu cầu Bearer token (chặn 403 khi chưa đăng nhập)")
  void notifications_shouldRequireAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/notifications")).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Bảo mật: POST /api/v1/news yêu cầu quyền ADMIN (chặn 403 khi chưa đăng nhập)")
  void createNews_shouldRequireAdmin() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/news")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test\",\"summary\":\"Sum\",\"content\":\"Cont\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Bảo mật: POST /api/v1/news từ chối STUDENT (403 Forbidden)")
  @org.springframework.security.test.context.support.WithMockUser(
      username = "student@fpt.edu.vn",
      roles = {"STUDENT"})
  void createNews_shouldRejectStudentRole() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/news")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test\",\"summary\":\"Sum\",\"content\":\"Cont\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName(
      "Bảo mật: POST /api/v1/auth/change-password yêu cầu Bearer token (chặn 403 khi chưa đăng nhập)")
  void changePassword_shouldRequireAuthentication() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"oldPassword\":\"Old123!\",\"newPassword\":\"New456!\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Bảo mật: Access Token mang token_version cũ bị từ chối 403 khi truy cập API bảo vệ")
  void outdatedTokenVersion_shouldBeRejected() throws Exception {
    vn.edu.fpt.fschool.config.JwtUtils jwtUtils =
        context.getBean(vn.edu.fpt.fschool.config.JwtUtils.class);
    vn.edu.fpt.fschool.service.TokenVersionService tokenVersionService =
        context.getBean(vn.edu.fpt.fschool.service.TokenVersionService.class);

    String testEmail = "anvvhe194500@fpt.edu.vn";
    // Giả lập người dùng đã đổi mật khẩu và token_version tăng lên 5
    tokenVersionService.updateUserTokenVersion(testEmail, 5);

    // Sinh token cũ với tokenVersion = 1
    String outdatedToken = jwtUtils.generateAccessToken(1L, testEmail, "STUDENT", 1);

    // Gọi API /api/v1/auth/me với token cũ -> Phải bị chặn 403
    mockMvc
        .perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + outdatedToken))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName(
      "Bảo mật: /admin, /admin/ và /admin/index.html là public web page (không bị chặn 403)")
  void adminPortal_shouldBeAccessibleWithoutAuth() throws Exception {
    mockMvc.perform(get("/admin")).andExpect(status().isOk());
    mockMvc.perform(get("/admin/")).andExpect(status().isOk());
    mockMvc.perform(get("/admin/index.html")).andExpect(status().isOk());
  }
}
