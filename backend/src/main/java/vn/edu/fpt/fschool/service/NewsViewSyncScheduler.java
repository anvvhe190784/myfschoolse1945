package vn.edu.fpt.fschool.service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.fschool.repository.NewsRepository;

@Component
@Slf4j
public class NewsViewSyncScheduler {

  private final NewsRepository newsRepository;
  private final NewsService newsService;
  private final StringRedisTemplate stringRedisTemplate;

  public NewsViewSyncScheduler(
      NewsRepository newsRepository,
      NewsService newsService,
      @Autowired(required = false) StringRedisTemplate stringRedisTemplate) {
    this.newsRepository = newsRepository;
    this.newsService = newsService;
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Scheduled(fixedDelay = 60000)
  @Transactional
  public void syncViewsToDatabase() {
    syncRedisViews();
    syncInMemoryViews();
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
        newsRepository.incrementViewsCount(newsId, count);
        stringRedisTemplate
            .opsForHash()
            .delete(NewsService.NEWS_VIEWS_KEY, entry.getKey().toString());
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
        newsRepository.incrementViewsCount(entry.getKey(), delta);
      }
    }
  }
}
