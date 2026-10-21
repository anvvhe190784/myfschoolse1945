package vn.edu.fpt.fschool.service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

/**
 * Service quản lý Distributed Lock bằng Redis (SETNX + Lua script release)
 * để đảm bảo an toàn cho các tác vụ phân tán đa instance (Scheduler, Batch).
 */
@Service
@Slf4j
public class DistributedLockService {

  private static final String LOCK_PREFIX = "dist_lock:";
  private static final String UNLOCK_LUA_SCRIPT =
      "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

  private final StringRedisTemplate stringRedisTemplate;
  private final DefaultRedisScript<Long> unlockScript;
  private final ConcurrentHashMap<String, ReentrantLock> localLocks = new ConcurrentHashMap<>();

  public DistributedLockService(
      @Autowired(required = false) StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
    this.unlockScript = new DefaultRedisScript<>(UNLOCK_LUA_SCRIPT, Long.class);
  }

  /**
   * Thực thi tác vụ với khóa phân tán. Trả về true nếu lấy được khóa và thực thi thành công,
   * trả về false nếu không lấy được khóa (đang có instance khác chạy).
   */
  public boolean executeWithLock(String lockName, Duration timeout, Runnable task) {
    String lockKey = LOCK_PREFIX + lockName;
    String lockToken = UUID.randomUUID().toString();

    boolean acquired = acquireLock(lockKey, lockToken, timeout);
    if (!acquired) {
      log.info("[DIST-LOCK] Bỏ qua tác vụ {}: Đang có instance khác nắm giữ khóa.", lockName);
      return false;
    }

    try {
      task.run();
      return true;
    } finally {
      releaseLock(lockKey, lockToken);
    }
  }

  private boolean acquireLock(String lockKey, String lockToken, Duration timeout) {
    if (stringRedisTemplate != null) {
      try {
        Boolean success =
            stringRedisTemplate.opsForValue().setIfAbsent(lockKey, lockToken, timeout);
        return Boolean.TRUE.equals(success);
      } catch (Exception ex) {
        log.warn(
            "[DIST-LOCK] Không thể kết nối Redis để lấy khóa {}, chuyển sang local lock: {}",
            lockKey,
            ex.getMessage());
      }
    }

    // Fallback: Local locking for unit tests / standalone
    ReentrantLock localLock = localLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());
    return localLock.tryLock();
  }

  private void releaseLock(String lockKey, String lockToken) {
    if (stringRedisTemplate != null) {
      try {
        stringRedisTemplate.execute(
            unlockScript, Collections.singletonList(lockKey), lockToken);
        return;
      } catch (Exception ex) {
        log.error("[DIST-LOCK] Lỗi khi giải phóng khóa Redis {}: {}", lockKey, ex.getMessage());
      }
    }

    ReentrantLock localLock = localLocks.get(lockKey);
    if (localLock != null && localLock.isHeldByCurrentThread()) {
      localLock.unlock();
    }
  }
}
