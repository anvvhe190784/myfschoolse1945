package vn.edu.fpt.fschool.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.fschool.dto.AuthDtos.*;
import vn.edu.fpt.fschool.service.AuthRateLimiter;
import vn.edu.fpt.fschool.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;
  private final AuthRateLimiter authRateLimiter;
  private final HttpServletRequest httpServletRequest;

  public AuthController(
      AuthService authService,
      AuthRateLimiter authRateLimiter,
      HttpServletRequest httpServletRequest) {
    this.authService = authService;
    this.authRateLimiter = authRateLimiter;
    this.httpServletRequest = httpServletRequest;
  }

  private String getClientIp() {
    if (httpServletRequest == null) {
      return "unknown";
    }
    String remoteAddr = httpServletRequest.getRemoteAddr();
    return (remoteAddr != null && !remoteAddr.isBlank()) ? remoteAddr : "unknown";
  }

  @PostMapping("/login")
  @RateLimiter(name = "default")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    String ip = getClientIp();
    authRateLimiter.checkRateLimit(
        "rate:auth:login:" + ip + ":" + request.identifier().trim().toLowerCase(),
        10,
        Duration.ofMinutes(1));
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<MessageResponse> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {
    String ip = getClientIp();
    authRateLimiter.checkRateLimit("rate:auth:forgot-pwd:" + ip, 5, Duration.ofMinutes(1));
    return ResponseEntity.ok(authService.forgotPassword(request));
  }

  @PostMapping("/verify-otp")
  public ResponseEntity<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
    String ip = getClientIp();
    authRateLimiter.checkRateLimit("rate:auth:verify-otp:" + ip, 10, Duration.ofMinutes(1));
    return ResponseEntity.ok(authService.verifyOtp(request));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<MessageResponse> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    String ip = getClientIp();
    authRateLimiter.checkRateLimit("rate:auth:reset-pwd:" + ip, 5, Duration.ofMinutes(1));
    return ResponseEntity.ok(authService.resetPassword(request));
  }

  @PostMapping("/change-password")
  public ResponseEntity<MessageResponse> changePassword(
      @Valid @RequestBody ChangePasswordRequest request,
      org.springframework.security.core.Authentication authentication) {
    if (authentication == null || authentication.getName() == null) {
      throw new IllegalArgumentException("Chưa xác thực người dùng");
    }
    return ResponseEntity.ok(authService.changePassword(request, authentication.getName()));
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<TokenRefreshResponse> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request) {
    String ip = getClientIp();
    authRateLimiter.checkRateLimit("rate:auth:refresh:" + ip, 20, Duration.ofMinutes(1));
    return ResponseEntity.ok(authService.refreshToken(request));
  }

  @PostMapping("/logout")
  public ResponseEntity<MessageResponse> logout(
      @RequestHeader(value = org.springframework.http.HttpHeaders.AUTHORIZATION, required = false)
          String authHeader,
      @RequestBody(required = false) LogoutRequest logoutRequest) {
    String accessToken =
        (authHeader != null && authHeader.startsWith("Bearer "))
            ? authHeader.substring(7).trim()
            : null;
    String refreshToken = (logoutRequest != null) ? logoutRequest.refreshToken() : null;

    if (accessToken == null && refreshToken == null) {
      return ResponseEntity.ok(new MessageResponse("Không có phiên đăng nhập cần hủy", true));
    }
    return ResponseEntity.ok(authService.logout(accessToken, refreshToken));
  }

  @GetMapping("/me")
  public ResponseEntity<UserMeResponse> getCurrentUser(
      org.springframework.security.core.Authentication authentication) {
    if (authentication == null || authentication.getName() == null) {
      throw new IllegalArgumentException("Chưa xác thực người dùng");
    }
    return ResponseEntity.ok(authService.getCurrentUserProfile(authentication.getName()));
  }
}
