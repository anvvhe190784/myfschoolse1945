package vn.edu.fpt.fschool.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NewsViewSyncScheduler {

  private final NewsService newsService;
  private final StringRedisTemplate stringRedisTemplate;
  private final DistributedLockService distributedLockService;

  public NewsViewSyncScheduler(
      NewsService newsService,
      @Autowired(required = false) StringRedisTemplate stringRedisTemplate) {
    this(newsService, stringRedisTemplate, new DistributedLockService(stringRedisTemplate));
  }

  @Autowired
  public NewsViewSyncScheduler(
      NewsService newsService,
      @Autowired(required = false) StringRedisTemplate stringRedisTemplate,
      DistributedLockService distributedLockService) {
    this.newsService = newsService;
    this.stringRedisTemplate = stringRedisTemplate;
    this.distributedLockService = distributedLockService;
  }

  @Scheduled(fixedDelay = 60000)
  public void syncViewsToDatabase() {
    if (distributedLockService != null) {
      distributedLockService.executeWithLock(
          "NewsViewSyncScheduler",
          Duration.ofSeconds(50),
          () -> {
            syncRedisViews();
            syncInMemoryViews();
          });
    } else {
      syncRedisViews();
      syncInMemoryViews();
    }
  }

  private void syncRedisViews() {
    if (stringRedisTemplate == null) {
      return;
    }
    try {
      Map<Object, Object> entries =
          stringRedisTemplate.opsForHash().entries(NewsService.NEWS_VIEWS_KEY);
      if (entries == null || entries.isEmpty()) {
        return;
      }
      log.info(
          "[SCHEDULED] Bắt đầu đồng bộ {} bài viết có lượt xem mới từ Redis xuống DB",
          entries.size());
      for (Map.Entry<Object, Object> entry : entries.entrySet()) {
        syncSingleRedisEntry(entry);
      }
    } catch (Exception e) {
      log.warn("[SCHEDULED] Không thể kết nối Redis để sync views: {}", e.getMessage());
    }
  }

  private void syncSingleRedisEntry(Map.Entry<Object, Object> entry) {
    try {
      long newsId = Long.parseLong(entry.getKey().toString());
      long count = Long.parseLong(entry.getValue().toString());
      if (count > 0) {
        newsService.updateViewsInDb(newsId, count);
        Long remaining =
            stringRedisTemplate
                .opsForHash()
                .increment(NewsService.NEWS_VIEWS_KEY, entry.getKey().toString(), -count);
        if (remaining == null || remaining <= 0) {
          stringRedisTemplate
              .opsForHash()
              .delete(NewsService.NEWS_VIEWS_KEY, entry.getKey().toString());
        }
      }
    } catch (Exception ex) {
      log.error(
          "[SCHEDULED] Lỗi cập nhật views cho newsId {}: {}", entry.getKey(), ex.getMessage());
    }
  }

  private void syncInMemoryViews() {
    Map<Long, AtomicLong> inMem = newsService.getInMemoryViews();
    if (inMem.isEmpty()) {
      return;
    }
    for (Map.Entry<Long, AtomicLong> entry : inMem.entrySet()) {
      long delta = entry.getValue().getAndSet(0);
      if (delta > 0) {
        newsService.updateViewsInDb(entry.getKey(), delta);
      }
    }
  }
}
