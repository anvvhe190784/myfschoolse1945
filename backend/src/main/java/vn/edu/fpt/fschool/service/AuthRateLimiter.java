package vn.edu.fpt.fschool.service;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthRateLimiter {

  private final StringRedisTemplate stringRedisTemplate;

  public AuthRateLimiter(
      @org.springframework.beans.factory.annotation.Autowired(required = false)
          StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  public void checkRateLimit(String key, int maxRequests, Duration window) {
    if (stringRedisTemplate == null) {
      return;
    }
    try {
      Long count = stringRedisTemplate.opsForValue().increment(key);
      if (count != null && count == 1L) {
        stringRedisTemplate.expire(key, window);
      } else if (count != null && count > 1) {
        Long expire = stringRedisTemplate.getExpire(key);
        if (expire != null && expire == -1L) {
          stringRedisTemplate.expire(key, window);
        }
      }
      if (count != null && count > maxRequests) {
        log.warn("Rate limit exceeded for key: {} (count={}, max={})", key, count, maxRequests);
        RateLimiter limiter = RateLimiter.ofDefaults(key);
        throw RequestNotPermitted.createRequestNotPermitted(limiter);
      }
    } catch (RequestNotPermitted rnp) {
      throw rnp;
    } catch (Exception ex) {
      log.error("Lỗi khi kiểm tra rate limit cho key {}: {}", key, ex.getMessage());
    }
  }
}
