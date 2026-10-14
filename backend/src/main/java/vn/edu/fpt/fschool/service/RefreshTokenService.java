package vn.edu.fpt.fschool.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import vn.edu.fpt.fschool.config.JwtUtils;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service quản lý Refresh Token với cơ chế Refresh Token Rotation (RTR).
 * Sử dụng Redis để lưu trữ token với thời gian sống (TTL) và phát hiện hành vi tấn công Replay Token.
 */
@Service
@Slf4j
public class RefreshTokenService {

    private static final String REFRESH_PREFIX = "jwt_refresh:";
    private static final String USER_REFRESH_PREFIX = "user_refresh:";
    private static final String USER_REFRESH_SET_PREFIX = "user_refresh_tokens:";

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtUtils jwtUtils;

    // Fallback bộ nhớ in-memory khi chạy unit test không có service Redis thực tế
    private final Map<String, String> inMemoryTokenStore = new ConcurrentHashMap<>();
    private final Map<String, String> inMemoryUserTokenStore = new ConcurrentHashMap<>();
    private final Map<String, java.util.Set<String>> inMemoryUserTokensSet = new ConcurrentHashMap<>();

    public RefreshTokenService(
            @Autowired(required = false) StringRedisTemplate stringRedisTemplate,
            JwtUtils jwtUtils
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Lưu trữ Refresh Token vào Redis với TTL theo cấu hình (mặc định 7 ngày).
     */
    public void saveRefreshToken(String email, String token, long ttlMs) {
        if (token == null || email == null) {
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (stringRedisTemplate != null) {
            try {
                String tokenKey = REFRESH_PREFIX + token;
                String userKey = USER_REFRESH_PREFIX + normalizedEmail;
                String userSetKey = USER_REFRESH_SET_PREFIX + normalizedEmail;

                stringRedisTemplate.opsForValue().set(tokenKey, normalizedEmail, Duration.ofMillis(ttlMs));
                stringRedisTemplate.opsForValue().set(userKey, token, Duration.ofMillis(ttlMs));
                var setOps = stringRedisTemplate.opsForSet();
                if (setOps != null) {
                    setOps.add(userSetKey, token);
                    stringRedisTemplate.expire(userSetKey, Duration.ofMillis(ttlMs));
                }

                log.debug("Đã lưu Refresh Token vào Redis cho user: {}", normalizedEmail);
            } catch (Exception ex) {
                log.error("Lỗi khi lưu Refresh Token vào Redis: {}", ex.getMessage());
                inMemoryTokenStore.put(token, normalizedEmail);
                inMemoryUserTokenStore.put(normalizedEmail, token);
                inMemoryUserTokensSet.computeIfAbsent(normalizedEmail, k -> ConcurrentHashMap.newKeySet()).add(token);
            }
        } else {
            inMemoryTokenStore.put(token, normalizedEmail);
            inMemoryUserTokenStore.put(normalizedEmail, token);
            inMemoryUserTokensSet.computeIfAbsent(normalizedEmail, k -> ConcurrentHashMap.newKeySet()).add(token);
        }
    }

    /**
     * Xác thực Refresh Token: kiểm tra chữ ký JWT, loại token = REFRESH, và tồn tại trong Redis.
     */
    public boolean isValidRefreshToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        if (!jwtUtils.validateToken(token)) {
            return false;
        }

        String tokenType = jwtUtils.getTokenType(token);
        if (!"REFRESH".equalsIgnoreCase(tokenType)) {
            log.warn("Token được cung cấp không phải là Refresh Token (loại: {})", tokenType);
            return false;
        }

        if (stringRedisTemplate != null) {
            try {
                Boolean exists = stringRedisTemplate.hasKey(REFRESH_PREFIX + token);
                return Boolean.TRUE.equals(exists);
            } catch (Exception ex) {
                log.error("Lỗi khi kiểm tra Refresh Token trên Redis: {}", ex.getMessage());
                return inMemoryTokenStore.containsKey(token);
            }
        }

        return inMemoryTokenStore.containsKey(token);
    }

    /**
     * Hủy Refresh Token khi người dùng logout hoặc khi xoay vòng token.
     */
    public void revokeRefreshToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        if (stringRedisTemplate != null) {
            try {
                String tokenKey = REFRESH_PREFIX + token;
                String email = stringRedisTemplate.opsForValue().get(tokenKey);
                stringRedisTemplate.delete(tokenKey);
                if (email != null) {
                    String normalizedEmail = email.trim().toLowerCase();
                    stringRedisTemplate.opsForSet().remove(USER_REFRESH_SET_PREFIX + normalizedEmail, token);
                    stringRedisTemplate.delete(USER_REFRESH_PREFIX + normalizedEmail);
                }
                log.debug("Đã thu hồi Refresh Token trên Redis");
            } catch (Exception ex) {
                log.error("Lỗi khi xóa Refresh Token trên Redis: {}", ex.getMessage());
                String email = inMemoryTokenStore.remove(token);
                if (email != null) {
                    inMemoryUserTokenStore.remove(email);
                    var set = inMemoryUserTokensSet.get(email);
                    if (set != null) set.remove(token);
                }
            }
        } else {
            String email = inMemoryTokenStore.remove(token);
            if (email != null) {
                inMemoryUserTokenStore.remove(email);
                var set = inMemoryUserTokensSet.get(email);
                if (set != null) set.remove(token);
            }
        }
    }

    /**
     * Thu hồi toàn bộ token của người dùng (Token Family Revocation khi phát hiện Replay Attack hoặc khi đổi mật khẩu).
     */
    public void revokeAllUserTokens(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();
        log.warn("CẢNH BÁO AN NINH: Thu hồi toàn bộ Refresh Token của user {}", normalizedEmail);
        if (stringRedisTemplate != null) {
            try {
                String userSetKey = USER_REFRESH_SET_PREFIX + normalizedEmail;
                java.util.Set<String> tokens = stringRedisTemplate.opsForSet().members(userSetKey);
                if (tokens != null) {
                    for (String t : tokens) {
                        stringRedisTemplate.delete(REFRESH_PREFIX + t);
                    }
                }
                stringRedisTemplate.delete(userSetKey);

                String userKey = USER_REFRESH_PREFIX + normalizedEmail;
                String currentToken = stringRedisTemplate.opsForValue().get(userKey);
                if (currentToken != null) {
                    stringRedisTemplate.delete(REFRESH_PREFIX + currentToken);
                }
                stringRedisTemplate.delete(userKey);
            } catch (Exception ex) {
                log.error("Lỗi khi thu hồi toàn bộ token trên Redis: {}", ex.getMessage());
                String currentToken = inMemoryUserTokenStore.remove(normalizedEmail);
                if (currentToken != null) {
                    inMemoryTokenStore.remove(currentToken);
                }
                var tokens = inMemoryUserTokensSet.remove(normalizedEmail);
                if (tokens != null) {
                    for (String t : tokens) {
                        inMemoryTokenStore.remove(t);
                    }
                }
            }
        } else {
            String currentToken = inMemoryUserTokenStore.remove(normalizedEmail);
            if (currentToken != null) {
                inMemoryTokenStore.remove(currentToken);
            }
            var tokens = inMemoryUserTokensSet.remove(normalizedEmail);
            if (tokens != null) {
                for (String t : tokens) {
                    inMemoryTokenStore.remove(t);
                }
            }
        }
    }
}
