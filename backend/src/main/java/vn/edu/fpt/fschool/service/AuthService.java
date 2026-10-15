package vn.edu.fpt.fschool.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
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

  public static final String OTP_PREFIX = "otp:reset:";
  public static final String OTP_COOLDOWN_PREFIX = "otp:cooldown:";
  public static final String OTP_ATTEMPTS_PREFIX = "otp:attempts:";
  public static final String RESET_TOKEN_PREFIX = "otp:token:";
  public static final int MAX_OTP_ATTEMPTS = 5;
  public static final long OTP_EXPIRATION_MINUTES = 5;
  public static final long OTP_COOLDOWN_SECONDS = 60;
  private static final String ERR_USER_NOT_FOUND_EMAIL = "Không tìm thấy người dùng với email: ";
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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

    tokenVersionService.updateUserTokenVersion(user.getEmail(), user.getTokenVersion());

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
        // Token is completely unparseable or malformed, safe to ignore
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
    java.util.Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);

    // Chống dò quét email (Email Enumeration Mitigation): Trả về thông báo trung lập nếu user không
    // tồn tại
    if (userOpt.isEmpty()) {
      return new MessageResponse(
          "Nếu email tồn tại trong hệ thống, Mã xác thực OTP đã được gửi đến hòm thư của bạn.",
          true);
    }

    User user = userOpt.get();
    String emailKey = user.getEmail().toLowerCase();
    String cooldownKey = OTP_COOLDOWN_PREFIX + emailKey;

    Boolean hasCooldown = stringRedisTemplate.hasKey(cooldownKey);
    if (Boolean.TRUE.equals(hasCooldown)) {
      Long expireSeconds = stringRedisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
      long waitSeconds =
          (expireSeconds != null && expireSeconds > 0) ? expireSeconds : OTP_COOLDOWN_SECONDS;
      throw new IllegalStateException(
          "Mã xác thực đã được gửi. Vui lòng đợi "
              + waitSeconds
              + " giây trước khi yêu cầu mã mới.");
    }

    // Sinh mã OTP 6 chữ số ngẫu nhiên an toàn bằng SecureRandom
    String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    stringRedisTemplate
        .opsForValue()
        .set(OTP_PREFIX + emailKey, otp, Duration.ofMinutes(OTP_EXPIRATION_MINUTES));

    // Reset attempts counter khi tạo OTP mới
    stringRedisTemplate.delete(OTP_ATTEMPTS_PREFIX + emailKey);

    // Đặt cooldown 60 giây chống spam gửi mã liên tục
    stringRedisTemplate
        .opsForValue()
        .set(cooldownKey, "1", Duration.ofSeconds(OTP_COOLDOWN_SECONDS));

    // Gửi email OTP ngầm (bất đồng bộ)
    emailService.sendOtpEmail(user.getEmail(), otp);

    return new MessageResponse(
        "Nếu email tồn tại trong hệ thống, Mã xác thực OTP đã được gửi đến hòm thư của bạn.", true);
  }

  public VerifyOtpResponse verifyOtp(VerifyOtpRequest req) {
    String email = req.email().trim().toLowerCase();
    String otp = req.otp().trim();

    String savedOtp = stringRedisTemplate.opsForValue().get(OTP_PREFIX + email);
    if (savedOtp == null) {
      throw new IllegalArgumentException(
          "Mã xác thực OTP đã hết hạn hoặc chưa được tạo. Vui lòng yêu cầu mã mới");
    }

    // Anti brute-force protection (tối đa 5 lần thử)
    Long attempts = stringRedisTemplate.opsForValue().increment(OTP_ATTEMPTS_PREFIX + email);
    if (attempts != null && attempts == 1) {
      stringRedisTemplate.expire(
          OTP_ATTEMPTS_PREFIX + email, Duration.ofMinutes(OTP_EXPIRATION_MINUTES));
    }

    if (attempts != null && attempts > MAX_OTP_ATTEMPTS) {
      stringRedisTemplate.delete(OTP_PREFIX + email);
      stringRedisTemplate.delete(OTP_ATTEMPTS_PREFIX + email);
      throw new IllegalArgumentException(
          "Bạn đã nhập sai mã OTP quá 5 lần. Mã xác thực đã bị vô hiệu hóa vì lý do an toàn, vui lòng yêu cầu mã mới.");
    }

    if (!savedOtp.equals(otp)) {
      long remaining = MAX_OTP_ATTEMPTS - (attempts != null ? attempts : 0);
      throw new IllegalArgumentException(
          "Mã xác thực OTP không chính xác. Bạn còn " + remaining + " lần thử.");
    }

    // Thành công: Xóa OTP, xóa attempts counter và cấp One-Time Reset Token (5 phút)
    stringRedisTemplate.delete(OTP_PREFIX + email);
    stringRedisTemplate.delete(OTP_ATTEMPTS_PREFIX + email);

    String resetToken = java.util.UUID.randomUUID().toString();
    stringRedisTemplate
        .opsForValue()
        .set(RESET_TOKEN_PREFIX + resetToken, email, Duration.ofMinutes(5));

    return new VerifyOtpResponse("Xác thực OTP thành công!", true, resetToken);
  }

  @Transactional
  public MessageResponse resetPassword(ResetPasswordRequest req) {
    String targetEmail = null;

    if (req.resetToken() != null && !req.resetToken().isBlank()) {
      // Xác thực bằng One-Time Reset Token (atomic getAndDelete)
      targetEmail =
          stringRedisTemplate
              .opsForValue()
              .getAndDelete(RESET_TOKEN_PREFIX + req.resetToken().trim());
      if (targetEmail == null) {
        throw new IllegalArgumentException("Token đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
      }
    } else if (req.email() != null && req.otp() != null) {
      // Hỗ trợ luồng cũ (email + otp) để backward compatibility
      String email = req.email().trim().toLowerCase();
      String otp = req.otp().trim();
      String savedOtp = stringRedisTemplate.opsForValue().get(OTP_PREFIX + email);
      if (savedOtp == null) {
        throw new IllegalArgumentException(
            "Mã xác thực OTP đã hết hạn hoặc chưa được tạo. Vui lòng yêu cầu mã mới");
      }
      if (!savedOtp.equals(otp)) {
        throw new IllegalArgumentException(
            "Mã xác thực OTP không chính xác hoặc chưa được xác thực");
      }
      stringRedisTemplate.delete(OTP_PREFIX + email);
      targetEmail = email;
    } else {
      throw new IllegalArgumentException("Vui lòng cung cấp mã xác thực hoặc reset token hợp lệ");
    }

    final String resolvedEmail = targetEmail;
    User user =
        userRepository
            .findByEmailIgnoreCase(resolvedEmail)
            .orElseThrow(
                () -> new IllegalArgumentException(ERR_USER_NOT_FOUND_EMAIL + resolvedEmail));

    user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
    user.setTokenVersion(user.getTokenVersion() != null ? user.getTokenVersion() + 1 : 2);
    userRepository.save(user);

    // Hủy khóa cooldown sau khi reset mật khẩu thành công
    stringRedisTemplate.delete(OTP_COOLDOWN_PREFIX + resolvedEmail);

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
                () -> new IllegalArgumentException(ERR_USER_NOT_FOUND_EMAIL + authenticatedEmail));

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

  public void logout(String token) {
    logout(token, null);
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
