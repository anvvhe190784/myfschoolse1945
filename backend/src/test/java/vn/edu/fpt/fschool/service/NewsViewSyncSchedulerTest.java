package vn.edu.fpt.fschool.service;

import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class NewsViewSyncSchedulerTest {

  @Mock private NewsService newsService;
  @Mock private StringRedisTemplate stringRedisTemplate;
  @Mock private HashOperations<String, Object, Object> hashOperations;

  @InjectMocks private NewsViewSyncScheduler scheduler;

  @Test
  @DisplayName("Đồng bộ lượt xem từ Redis và In-Memory xuống DB thông qua newsService.updateViewsInDb")
  void syncViewsToDatabase_shouldCallNewsService() {
    when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
    when(hashOperations.entries(NewsService.NEWS_VIEWS_KEY)).thenReturn(Map.of("10", "5"));

    Map<Long, AtomicLong> inMem = new ConcurrentHashMap<>();
    inMem.put(20L, new AtomicLong(3));
    when(newsService.getInMemoryViews()).thenReturn(inMem);

    scheduler.syncViewsToDatabase();

    verify(newsService, times(1)).updateViewsInDb(10L, 5L);
    verify(hashOperations, times(1)).delete(NewsService.NEWS_VIEWS_KEY, "10");
    verify(newsService, times(1)).updateViewsInDb(20L, 3L);
  }

  @Test
  @DisplayName("Bỏ qua đồng bộ an toàn khi Redis rỗng và In-Memory rỗng")
  void syncViewsToDatabase_shouldDoNothing_whenEmpty() {
    when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
    when(hashOperations.entries(NewsService.NEWS_VIEWS_KEY)).thenReturn(Collections.emptyMap());
    when(newsService.getInMemoryViews()).thenReturn(Collections.emptyMap());

    scheduler.syncViewsToDatabase();

    verify(newsService, never()).updateViewsInDb(anyLong(), anyLong());
  }
}
