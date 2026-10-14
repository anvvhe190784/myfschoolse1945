package vn.edu.fpt.fschool.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.fpt.fschool.config.JwtUtils;
import vn.edu.fpt.fschool.dto.AuthDtos.ChangePasswordRequest;
import vn.edu.fpt.fschool.dto.AuthDtos.ResetPasswordRequest;
import vn.edu.fpt.fschool.event.NotificationEvents.PasswordChangedEvent;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthSecurityTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private EmailService emailService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private TokenBlacklistService tokenBlacklistService;
    private RefreshTokenService refreshTokenService;
    private TokenVersionService tokenVersionService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService(stringRedisTemplate, jwtUtils);
        refreshTokenService = new RefreshTokenService(stringRedisTemplate, jwtUtils);
        tokenVersionService = new TokenVersionService(stringRedisTemplate, userRepository);
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtUtils,
                emailService,
                tokenBlacklistService,
                refreshTokenService,
                tokenVersionService,
                stringRedisTemplate,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Bảo mật: Từ chối đặt lại mật khẩu nếu mã OTP không khớp (Chống Account Takeover)")
    void resetPassword_shouldReject_whenOtpDoesNotMatch() {
        User user = User.builder()
                .userId(1L)
                .email("victim@fpt.edu.vn")
                .build();

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:reset:victim@fpt.edu.vn")).thenReturn("654321");
        when(userRepository.findByEmailIgnoreCase("victim@fpt.edu.vn")).thenReturn(Optional.of(user));

        ResetPasswordRequest request = new ResetPasswordRequest(
                "victim@fpt.edu.vn",
                "999999", // Sai OTP
                "NewPassword123!"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(request));
        assertTrue(ex.getMessage().contains("Mã xác thực OTP không chính xác"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Bảo mật: Từ chối đặt lại mật khẩu nếu mã OTP đã hết hạn")
    void resetPassword_shouldReject_whenOtpIsExpired() {
        User user = User.builder()
                .userId(1L)
                .email("victim@fpt.edu.vn")
                .build();

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:reset:victim@fpt.edu.vn")).thenReturn(null); // Đã hết hạn trong Redis
        when(userRepository.findByEmailIgnoreCase("victim@fpt.edu.vn")).thenReturn(Optional.of(user));

        ResetPasswordRequest request = new ResetPasswordRequest(
                "victim@fpt.edu.vn",
                "123456",
                "NewPassword123!"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(request));
        assertTrue(ex.getMessage().contains("Mã xác thực OTP đã hết hạn"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Thành công: Đặt lại mật khẩu thành công khi OTP đúng và còn hiệu lực")
    void resetPassword_shouldSucceed_whenOtpIsValid() {
        User user = User.builder()
                .userId(1L)
                .email("user@fpt.edu.vn")
                .build();

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:reset:user@fpt.edu.vn")).thenReturn("123456");
        when(userRepository.findByEmailIgnoreCase("user@fpt.edu.vn")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("hashedPassword");

        ResetPasswordRequest request = new ResetPasswordRequest(
                "user@fpt.edu.vn",
                "123456",
                "NewPassword123!"
        );

        var response = authService.resetPassword(request);
        assertTrue(response.success());
        assertEquals("hashedPassword", user.getPasswordHash());
        assertEquals(2, user.getTokenVersion());
        verify(userRepository, times(1)).save(user);
        verify(stringRedisTemplate, times(1)).delete("otp:reset:user@fpt.edu.vn");
        verify(eventPublisher, times(1)).publishEvent(any(PasswordChangedEvent.class));
    }

    @Test
    @DisplayName("Đổi mật khẩu: Đổi mật khẩu thành công tăng tokenVersion và phát sự kiện bảo mật")
    void changePassword_shouldSucceed_andIncrementTokenVersion() {
        User user = User.builder()
                .userId(2L)
                .email("student@fpt.edu.vn")
                .passwordHash("oldHashedPassword")
                .tokenVersion(1)
                .build();

        when(userRepository.findByEmailIgnoreCase("student@fpt.edu.vn")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPassword123!", "oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword456!")).thenReturn("newHashedPassword");

        ChangePasswordRequest request = new ChangePasswordRequest("OldPassword123!", "NewPassword456!");
        var response = authService.changePassword(request, "student@fpt.edu.vn");

        assertTrue(response.success());
        assertEquals("newHashedPassword", user.getPasswordHash());
        assertEquals(2, user.getTokenVersion());
        verify(userRepository, times(1)).save(user);
        verify(eventPublisher, times(1)).publishEvent(any(PasswordChangedEvent.class));
    }

    @Test
    @DisplayName("Bảo mật: Từ chối đổi mật khẩu nếu mật khẩu cũ không chính xác")
    void changePassword_shouldReject_whenOldPasswordDoesNotMatch() {
        User user = User.builder()
                .userId(2L)
                .email("student@fpt.edu.vn")
                .passwordHash("oldHashedPassword")
                .tokenVersion(1)
                .build();

        when(userRepository.findByEmailIgnoreCase("student@fpt.edu.vn")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongOldPassword", "oldHashedPassword")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest("WrongOldPassword", "NewPassword456!");

        assertThrows(IllegalArgumentException.class, () -> authService.changePassword(request, "student@fpt.edu.vn"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Bảo mật: Từ chối đổi mật khẩu nếu mật khẩu mới trùng mật khẩu cũ")
    void changePassword_shouldReject_whenNewPasswordIsSameAsOld() {
        User user = User.builder()
                .userId(2L)
                .email("student@fpt.edu.vn")
                .passwordHash("oldHashedPassword")
                .tokenVersion(1)
                .build();

        when(userRepository.findByEmailIgnoreCase("student@fpt.edu.vn")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SamePassword123!", "oldHashedPassword")).thenReturn(true);

        ChangePasswordRequest request = new ChangePasswordRequest("SamePassword123!", "SamePassword123!");

        assertThrows(IllegalArgumentException.class, () -> authService.changePassword(request, "student@fpt.edu.vn"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Bảo mật Redis Blacklist: Khi logout, token được ghi vào Redis với TTL còn lại")
    void logout_shouldStoreTokenInRedisBlacklist() {
        when(jwtUtils.getRemainingTtlMs("valid-jwt-token")).thenReturn(3600000L);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        authService.logout("Bearer valid-jwt-token");

        verify(valueOperations, times(1)).set(
                eq("jwt_blacklist:valid-jwt-token"),
                eq("revoked"),
                any(java.time.Duration.class)
        );
    }

    @Test
    @DisplayName("Bảo mật: Token nằm trong Redis Blacklist được phát hiện chính xác")
    void isBlacklisted_shouldReturnTrue_whenKeyExistsInRedis() {
        when(stringRedisTemplate.hasKey("jwt_blacklist:revoked-token")).thenReturn(true);

        boolean blacklisted = tokenBlacklistService.isBlacklisted("Bearer revoked-token");
        assertTrue(blacklisted);
    }

    @Test
    @DisplayName("Dual Token: Login cấp cả Access Token và Refresh Token")
    void login_shouldReturnBothTokens() {
        User user = User.builder()
                .userId(1L)
                .email("student@fpt.edu.vn")
                .rollNumber("SE1945")
                .fullName("Sinh viên F-School")
                .role("STUDENT")
                .passwordHash("hashedPass")
                .status("ACTIVE")
                .build();

        when(userRepository.findByEmailIgnoreCase("student@fpt.edu.vn")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashedPass")).thenReturn(true);
        when(jwtUtils.generateAccessToken(eq(1L), eq("student@fpt.edu.vn"), eq("STUDENT"), any())).thenReturn("mock-access-token");
        when(jwtUtils.generateRefreshToken(1L, "student@fpt.edu.vn")).thenReturn("mock-refresh-token");
        when(jwtUtils.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(jwtUtils.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        var response = authService.login(new vn.edu.fpt.fschool.dto.AuthDtos.LoginRequest("student@fpt.edu.vn", "Password123!"));

        assertNotNull(response);
        assertEquals("mock-access-token", response.accessToken());
        assertEquals("mock-refresh-token", response.refreshToken());
        assertEquals("mock-access-token", response.token()); // Backwards compat
        assertEquals("Bearer", response.tokenType());
    }

    @Test
    @DisplayName("RTR: Refresh Token xoay vòng token thành công")
    void refreshToken_shouldRotateTokens() {
        String oldRefresh = "old-refresh-token";
        User user = User.builder()
                .userId(1L)
                .email("student@fpt.edu.vn")
                .role("STUDENT")
                .status("ACTIVE")
                .build();

        when(jwtUtils.validateToken(oldRefresh)).thenReturn(true);
        when(jwtUtils.getTokenType(oldRefresh)).thenReturn("REFRESH");
        when(stringRedisTemplate.hasKey("jwt_refresh:" + oldRefresh)).thenReturn(true);
        when(jwtUtils.getEmailFromToken(oldRefresh)).thenReturn("student@fpt.edu.vn");
        when(userRepository.findByEmailIgnoreCase("student@fpt.edu.vn")).thenReturn(Optional.of(user));
        when(jwtUtils.generateAccessToken(eq(1L), eq("student@fpt.edu.vn"), eq("STUDENT"), any())).thenReturn("new-access-token");
        when(jwtUtils.generateRefreshToken(1L, "student@fpt.edu.vn")).thenReturn("new-refresh-token");
        when(jwtUtils.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(jwtUtils.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        var response = authService.refreshToken(new vn.edu.fpt.fschool.dto.AuthDtos.RefreshTokenRequest(oldRefresh));

        assertNotNull(response);
        assertEquals("new-access-token", response.accessToken());
        assertEquals("new-refresh-token", response.refreshToken());
        verify(stringRedisTemplate, atLeastOnce()).delete("jwt_refresh:" + oldRefresh);
    }

    @Test
    @DisplayName("Bảo mật Logout: Thu hồi cả Access Token và Refresh Token")
    void logout_shouldRevokeBothTokens() {
        when(jwtUtils.getRemainingTtlMs("valid-access-token")).thenReturn(300000L);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        authService.logout("Bearer valid-access-token", "valid-refresh-token");

        verify(valueOperations, times(1)).set(
                eq("jwt_blacklist:valid-access-token"),
                eq("revoked"),
                any(java.time.Duration.class)
        );
        verify(stringRedisTemplate, atLeastOnce()).delete("jwt_refresh:valid-refresh-token");
    }
}
