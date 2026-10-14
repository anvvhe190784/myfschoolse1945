package vn.edu.fpt.fschool.service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.fschool.config.JwtUtils;
import vn.edu.fpt.fschool.dto.AuthDtos.*;
import vn.edu.fpt.fschool.event.NotificationEvents.PasswordChangedEvent;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.UserRepository;

@Service
public class AuthService {

  private static final String OTP_PREFIX = "otp:reset:";
  private static final long OTP_EXPIRATION_MINUTES = 5;

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;
  private final EmailService emailService;
  private final TokenBlacklistService tokenBlacklistService;
  private final RefreshTokenService refreshTokenService;
  private final TokenVersionService tokenVersionService;
  private final StringRedisTemplate stringRedisTemplate;
  private final ApplicationEventPublisher eventPublisher;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtUtils jwtUtils,
      EmailService emailService,
      TokenBlacklistService tokenBlacklistService,
      RefreshTokenService refreshTokenService,
      TokenVersionService tokenVersionService,
      StringRedisTemplate stringRedisTemplate,
      ApplicationEventPublisher eventPublisher) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtils = jwtUtils;
    this.emailService = emailService;
    this.tokenBlacklistService = tokenBlacklistService;
    this.refreshTokenService = refreshTokenService;
    this.tokenVersionService = tokenVersionService;
    this.stringRedisTemplate = stringRedisTemplate;
    this.eventPublisher = eventPublisher;
  }

  @Transactional(readOnly = true)
  public LoginResponse login(LoginRequest req) {
    String identifier = req.identifier().trim();
    User user =
        userRepository
            .findByEmailIgnoreCase(identifier)
            .or(() -> userRepository.findByRollNumber(identifier))
            .orElseThrow(
                () -> new IllegalArgumentException("Tài khoản hoặc mật khẩu không chính xác"));

    if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
      throw new IllegalArgumentException("Tài khoản hoặc mật khẩu không chính xác");
    }

    if ("LOCKED".equalsIgnoreCase(user.getStatus())
        || "INACTIVE".equalsIgnoreCase(user.getStatus())) {
      throw new IllegalStateException("Tài khoản của bạn đã bị khóa hoặc chưa được kích hoạt");
    }

    String accessToken =
        jwtUtils.generateAccessToken(
            user.getUserId(), user.getEmail(), user.getRole(), user.getTokenVersion());
    String refreshToken = jwtUtils.generateRefreshToken(user.getUserId(), user.getEmail());

    refreshTokenService.saveRefreshToken(
        user.getEmail(), refreshToken, jwtUtils.getRefreshTokenExpirationMs());

    return new LoginResponse(
        accessToken,
        refreshToken,
        "Bearer",
        jwtUtils.getAccessTokenExpirationMs() / 1000,
        user.getUserId(),
        user.getRollNumber(),
        user.getEmail(),
        user.getFullName(),
        user.getRole());
  }

  @Transactional(readOnly = true)
  public TokenRefreshResponse refreshToken(RefreshTokenRequest req) {
    String oldRefreshToken = req.refreshToken().trim();

    if (!refreshTokenService.isValidRefreshToken(oldRefreshToken)) {
      // Kiểm tra nếu là token giả mạo hoặc token đã bị thu hồi/tái sử dụng
      try {
        if (jwtUtils.validateToken(oldRefreshToken)) {
          String compromisedEmail = jwtUtils.getEmailFromToken(oldRefreshToken);
          refreshTokenService.revokeAllUserTokens(compromisedEmail);
        }
      } catch (Exception ignored) {
      }
      throw new IllegalArgumentException("Refresh token không hợp lệ hoặc đã hết hạn");
    }

    String email = jwtUtils.getEmailFromToken(oldRefreshToken);
    User user =
        userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

    if ("LOCKED".equalsIgnoreCase(user.getStatus())
        || "INACTIVE".equalsIgnoreCase(user.getStatus())) {
      refreshTokenService.revokeAllUserTokens(email);
      throw new IllegalStateException("Tài khoản của bạn đã bị khóa hoặc chưa được kích hoạt");
    }

    // Xoay vòng Refresh Token (Refresh Token Rotation - RTR)
    refreshTokenService.revokeRefreshToken(oldRefreshToken);

    String newAccessToken =
        jwtUtils.generateAccessToken(
            user.getUserId(), user.getEmail(), user.getRole(), user.getTokenVersion());
    String newRefreshToken = jwtUtils.generateRefreshToken(user.getUserId(), user.getEmail());
    refreshTokenService.saveRefreshToken(
        user.getEmail(), newRefreshToken, jwtUtils.getRefreshTokenExpirationMs());

    return new TokenRefreshResponse(
        newAccessToken, newRefreshToken, "Bearer", jwtUtils.getAccessTokenExpirationMs() / 1000);
  }

  public MessageResponse forgotPassword(ForgotPasswordRequest req) {
    String email = req.email().trim();
    User user =
        userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Không tìm thấy người dùng với email: " + email));

    // Sinh mã OTP 6 chữ số ngẫu nhiên
    String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
    stringRedisTemplate
        .opsForValue()
        .set(
            OTP_PREFIX + user.getEmail().toLowerCase(),
            otp,
            Duration.ofMinutes(OTP_EXPIRATION_MINUTES));

    // Gửi email OTP ngoài transaction để tránh nghẽn database connection pool
    emailService.sendOtpEmail(user.getEmail(), otp);

    return new MessageResponse("Mã xác thực OTP đã được gửi đến email của bạn.", true);
  }

  public MessageResponse verifyOtp(VerifyOtpRequest req) {
    String email = req.email().trim().toLowerCase();
    String otp = req.otp().trim();

    if (!userRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("Không tìm thấy người dùng với email: " + email);
    }

    String savedOtp = stringRedisTemplate.opsForValue().get(OTP_PREFIX + email);
    if (savedOtp == null) {
      throw new IllegalArgumentException(
          "Mã xác thực OTP đã hết hạn hoặc chưa được tạo. Vui lòng yêu cầu mã mới");
    }

    if (!savedOtp.equals(otp)) {
      throw new IllegalArgumentException("Mã xác thực OTP không chính xác");
    }

    return new MessageResponse("Xác thực OTP thành công!", true);
  }

  @Transactional
  public MessageResponse resetPassword(ResetPasswordRequest req) {
    String email = req.email().trim().toLowerCase();
    String otp = req.otp().trim();

    User user =
        userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Không tìm thấy người dùng với email: " + req.email()));

    String savedOtp = stringRedisTemplate.opsForValue().get(OTP_PREFIX + email);
    if (savedOtp == null) {
      throw new IllegalArgumentException(
          "Mã xác thực OTP đã hết hạn hoặc chưa được tạo. Vui lòng yêu cầu mã mới");
    }

    if (!savedOtp.equals(otp)) {
      throw new IllegalArgumentException("Mã xác thực OTP không chính xác hoặc chưa được xác thực");
    }

    user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
    user.setTokenVersion(user.getTokenVersion() != null ? user.getTokenVersion() + 1 : 2);
    userRepository.save(user);

    // Hủy mã OTP sau khi reset mật khẩu thành công
    stringRedisTemplate.delete(OTP_PREFIX + email);

    // Cập nhật token_version vào Redis để chặn ngay Access Token cũ
    tokenVersionService.updateUserTokenVersion(user.getEmail(), user.getTokenVersion());

    // Thu hồi toàn bộ Refresh Token của user trên mọi thiết bị
    refreshTokenService.revokeAllUserTokens(user.getEmail());

    // Phát sự kiện miền: Mật khẩu đã thay đổi (NotificationEventListener xử lý ngầm)
    eventPublisher.publishEvent(new PasswordChangedEvent(user.getUserId(), user.getEmail()));

    return new MessageResponse("Đặt lại mật khẩu thành công!", true);
  }

  @Transactional
  public MessageResponse changePassword(ChangePasswordRequest req, String authenticatedEmail) {
    String email = authenticatedEmail.trim().toLowerCase();
    User user =
        userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Không tìm thấy người dùng với email: " + authenticatedEmail));

    if (!passwordEncoder.matches(req.oldPassword(), user.getPasswordHash())) {
      throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác");
    }

    if (req.oldPassword().equals(req.newPassword())) {
      throw new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu cũ");
    }

    user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
    user.setTokenVersion(user.getTokenVersion() != null ? user.getTokenVersion() + 1 : 2);
    userRepository.save(user);

    // Cập nhật token_version vào Redis để chặn ngay Access Token cũ
    tokenVersionService.updateUserTokenVersion(user.getEmail(), user.getTokenVersion());

    // Thu hồi toàn bộ Refresh Token của user trên mọi thiết bị
    refreshTokenService.revokeAllUserTokens(user.getEmail());

    // Phát sự kiện miền: Mật khẩu đã thay đổi (NotificationEventListener xử lý ngầm)
    eventPublisher.publishEvent(new PasswordChangedEvent(user.getUserId(), user.getEmail()));

    return new MessageResponse("Đổi mật khẩu thành công!", true);
  }

  public MessageResponse logout(String accessToken, String refreshToken) {
    if (accessToken != null && !accessToken.isBlank()) {
      tokenBlacklistService.blacklistToken(accessToken);
    }
    if (refreshToken != null && !refreshToken.isBlank()) {
      refreshTokenService.revokeRefreshToken(refreshToken);
    }
    return new MessageResponse("Đăng xuất thành công!", true);
  }

  public MessageResponse logout(String token) {
    return logout(token, null);
  }

  @Transactional(readOnly = true)
  public UserMeResponse getCurrentUserProfile(String email) {
    User user =
        userRepository
            .findByEmailIgnoreCase(email.trim())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

    return new UserMeResponse(
        user.getUserId(),
        user.getRollNumber(),
        user.getEmail(),
        user.getFullName(),
        user.getPhoneNumber(),
        user.getAvatarUrl(),
        user.getRole(),
        user.getStatus());
  }
}
