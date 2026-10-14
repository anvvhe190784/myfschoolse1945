package vn.edu.fpt.fschool.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.fschool.dto.AuthDtos.*;
import vn.edu.fpt.fschool.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "1. Authentication (Login & ResetPass)", description = "Xác thực tài khoản và đặt lại mật khẩu")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @RateLimiter(name = "default")
    @Operation(summary = "Đăng nhập hệ thống (Use Case 1)", description = "Đăng nhập bằng email/roll_number và password. Trả về Bearer JWT Token.")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Yêu cầu mã xác thực quên mật khẩu", description = "Gửi email chứa mã OTP 6 số đến địa chỉ email đã đăng ký")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Xác thực mã OTP", description = "Kiểm tra mã OTP 6 số người dùng đã nhận qua email")
    public ResponseEntity<MessageResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Đặt lại mật khẩu (Use Case 2)", description = "Đặt lại mật khẩu mới cho tài khoản qua email xác nhận")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu tài khoản", description = "Đổi mật khẩu cho người dùng đang đăng nhập, tự động hủy toàn bộ phiên làm việc cũ")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            org.springframework.security.core.Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Chưa xác thực người dùng");
        }
        return ResponseEntity.ok(authService.changePassword(request, authentication.getName()));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Làm mới Access Token", description = "Sử dụng Refresh Token để cấp cặp Access Token và Refresh Token mới (RTR)")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất tài khoản & Thu hồi Token JWT", description = "Đưa Access Token vào Redis Blacklist và hủy Refresh Token phía Server")
    public ResponseEntity<MessageResponse> logout(
            @RequestHeader(value = org.springframework.http.HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody(required = false) LogoutRequest logoutRequest
    ) {
        String accessToken = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7).trim()
                : null;
        String refreshToken = (logoutRequest != null) ? logoutRequest.refreshToken() : null;

        if (accessToken == null && refreshToken == null) {
            return ResponseEntity.ok(new MessageResponse("Không có phiên đăng nhập cần hủy", true));
        }
        return ResponseEntity.ok(authService.logout(accessToken, refreshToken));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin cá nhân của phiên đăng nhập hiện tại", description = "Truy vấn hồ sơ cá nhân của người dùng đang được xác thực qua JWT")
    public ResponseEntity<UserMeResponse> getCurrentUser(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Chưa xác thực người dùng");
        }
        return ResponseEntity.ok(authService.getCurrentUserProfile(authentication.getName()));
    }
}
