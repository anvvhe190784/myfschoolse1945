package vn.edu.fpt.fschool.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.UserRepository;

/**
 * Service quản lý và xác thực token_version của người dùng.
 * Sử dụng Redis để kiểm tra nhanh (< 1ms) với fallback in-memory và database query khi cache miss.
 */
@Service
@Slf4j
public class TokenVersionService {

  private static final String TOKEN_VERSION_PREFIX = "user:token_version:";

  private final StringRedisTemplate stringRedisTemplate;
  private final UserRepository userRepository;

  // Fallback in-memory cho môi trường test không có Redis thực tế
  private final Map<String, Integer> inMemoryTokenVersionStore = new ConcurrentHashMap<>();

  public TokenVersionService(
      @Autowired(required = false) StringRedisTemplate stringRedisTemplate,
      UserRepository userRepository) {
    this.stringRedisTemplate = stringRedisTemplate;
    this.userRepository = userRepository;
  }

  /**
   * Kiểm tra xem tokenVersion trong JWT có hợp lệ không.
   * Nếu nhỏ hơn currentVersion (mật khẩu đã bị đổi), trả về false để từ chối ngay.
   */
  public boolean isTokenVersionValid(String email, Integer tokenVersionInJwt) {
    if (email == null || tokenVersionInJwt == null) {
      return false;
    }
    int currentVersion = getCurrentTokenVersion(email);
    return tokenVersionInJwt >= currentVersion;
  }

  /**
   * Lấy token_version hiện tại của người dùng (ưu tiên Redis, cache miss thì query DB).
   */
  public int getCurrentTokenVersion(String email) {
    String normalizedEmail = email.trim().toLowerCase();
    String key = TOKEN_VERSION_PREFIX + normalizedEmail;

    if (stringRedisTemplate != null) {
      try {
        String val = stringRedisTemplate.opsForValue().get(key);
        if (val != null) {
          return Integer.parseInt(val);
        }
      } catch (Exception e) {
        log.error("Lỗi khi đọc token_version từ Redis: {}", e.getMessage());
      }
    }

    Integer memVal = inMemoryTokenVersionStore.get(normalizedEmail);
    if (memVal != null) {
      return memVal;
    }

    // Cache miss -> Query DB và nạp lại vào Redis
    int dbVersion = userRepository.findByEmailIgnoreCase(normalizedEmail)
        .map(User::getTokenVersion)
        .orElse(1);

    updateUserTokenVersion(normalizedEmail, dbVersion);
    return dbVersion;
  }

  /**
   * Cập nhật token_version mới vào Redis khi người dùng đổi mật khẩu.
   */
  public void updateUserTokenVersion(String email, int newVersion) {
    if (email == null) return;
    String normalizedEmail = email.trim().toLowerCase();
    String key = TOKEN_VERSION_PREFIX + normalizedEmail;

    if (stringRedisTemplate != null) {
      try {
        stringRedisTemplate.opsForValue().set(key, String.valueOf(newVersion), Duration.ofDays(7));
        log.debug("Đã cập nhật token_version={} cho user={} vào Redis", newVersion, normalizedEmail);
      } catch (Exception e) {
        log.error("Lỗi khi cập nhật token_version vào Redis: {}", e.getMessage());
        inMemoryTokenVersionStore.put(normalizedEmail, newVersion);
      }
    } else {
      inMemoryTokenVersionStore.put(normalizedEmail, newVersion);
    }
  }

  /**
   * Xóa cache token_version (khi cần reset)
   */
  public void evictTokenVersion(String email) {
    if (email == null) return;
    String normalizedEmail = email.trim().toLowerCase();
    if (stringRedisTemplate != null) {
      try {
        stringRedisTemplate.delete(TOKEN_VERSION_PREFIX + normalizedEmail);
      } catch (Exception ignored) {}
    }
    inMemoryTokenVersionStore.remove(normalizedEmail);
  }
}
