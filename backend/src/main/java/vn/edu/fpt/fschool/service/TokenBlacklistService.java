package vn.edu.fpt.fschool.service;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import vn.edu.fpt.fschool.config.JwtUtils;

@Service
@Slf4j
public class TokenBlacklistService {

  private static final String BLACKLIST_PREFIX = "jwt_blacklist:";

  private final StringRedisTemplate stringRedisTemplate;
  private final JwtUtils jwtUtils;

  public TokenBlacklistService(
      @Autowired(required = false) StringRedisTemplate stringRedisTemplate, JwtUtils jwtUtils) {
    this.stringRedisTemplate = stringRedisTemplate;
    this.jwtUtils = jwtUtils;
  }

  /**
   * Đưa token JWT vào danh sách đen (Redis Blacklist) với thời gian sống (TTL) đúng bằng thời gian
   * còn lại trước khi token tự hết hạn.
   */
  public void blacklistToken(String token) {
    if (token == null || token.isBlank()) {
      return;
    }
    if (token.startsWith("Bearer ")) {
      token = token.substring(7).trim();
    }

    long remainingTtlMs = jwtUtils.getRemainingTtlMs(token);
    if (remainingTtlMs <= 0) {
      log.info("Token đã hết hạn hoặc không hợp lệ, không cần đưa vào blacklist");
      return;
    }

    if (stringRedisTemplate != null) {
      try {
        String key = BLACKLIST_PREFIX + token;
        stringRedisTemplate.opsForValue().set(key, "revoked", Duration.ofMillis(remainingTtlMs));
        log.info("Token đã được đưa vào Redis Blacklist với TTL: {} ms", remainingTtlMs);
      } catch (Exception ex) {
        log.error("Lỗi khi kết nối Redis để blacklist token: {}", ex.getMessage());
      }
    } else {
      log.warn("StringRedisTemplate không khả dụng, bỏ qua lưu Redis blacklist");
    }
  }

  /** Kiểm tra xem token JWT có nằm trong danh sách đen đã thu hồi hay không. */
  public boolean isBlacklisted(String token) {
    if (token == null || token.isBlank()) {
      return false;
    }
    if (token.startsWith("Bearer ")) {
      token = token.substring(7).trim();
    }

    if (stringRedisTemplate != null) {
      try {
        String key = BLACKLIST_PREFIX + token;
        Boolean hasKey = stringRedisTemplate.hasKey(key);
        return Boolean.TRUE.equals(hasKey);
      } catch (Exception ex) {
        log.error(
            "Lỗi kiểm tra Redis blacklist (Fail-closed: từ chối token): {}", ex.getMessage());
        return true;
      }
    }
    log.error("StringRedisTemplate không khả dụng (Fail-closed: từ chối token)");
    return true;
  }
}
