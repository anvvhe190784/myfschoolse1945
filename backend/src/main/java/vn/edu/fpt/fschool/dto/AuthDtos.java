package vn.edu.fpt.fschool.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthDtos {
  public record LoginRequest(
      @NotBlank(message = "Email không được để trống") String identifier,
      @NotBlank(message = "Mật khẩu không được để trống") String password) {}

  public record LoginResponse(
      String accessToken,
      String refreshToken,
      String tokenType,
      Long expiresIn,
      Long userId,
      String rollNumber,
      String email,
      String fullName,
      String role,
      Boolean mustChangePassword) {
    // Backwards compatibility method for code expecting .token()
    public String token() {
      return accessToken;
    }
  }

  public record RefreshTokenRequest(
      @NotBlank(message = "Refresh token không được để trống") String refreshToken) {}

  public record TokenRefreshResponse(
      String accessToken, String refreshToken, String tokenType, Long expiresIn) {}

  public record LogoutRequest(String refreshToken) {}

  public record ResetPasswordRequest(
      @NotBlank(message = "Reset token không được để trống") String resetToken,
      @NotBlank(message = "Mật khẩu mới không được để trống")
          @Size(min = 6, max = 50, message = "Mật khẩu mới phải có độ dài từ 6 đến 50 ký tự")
          String newPassword) {}

  public record ChangePasswordRequest(
      @NotBlank(message = "Mật khẩu hiện tại không được để trống") String oldPassword,
      @NotBlank(message = "Mật khẩu mới không được để trống")
          @Size(min = 8, max = 12, message = "Mật khẩu mới phải có độ dài từ 8 đến 12 ký tự")
          @Pattern(
              regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,12}$",
              message = "Mật khẩu mới phải bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt")
          String newPassword) {}

  public record ForgotPasswordRequest(
      @NotBlank @Email(message = "Email không hợp lệ") String email) {}

  public record VerifyOtpRequest(
      @NotBlank @Email(message = "Email không hợp lệ") String email,
      @NotBlank(message = "Mã xác thực không được để trống") String otp) {}

  public record VerifyOtpResponse(String message, boolean success, String resetToken) {}

  public record UserMeResponse(
      Long userId,
      String rollNumber,
      String email,
      String fullName,
      String phoneNumber,
      String avatarUrl,
      String role,
      String status,
      Boolean mustChangePassword) {}

  public record MessageResponse(String message, boolean success) {}
}
