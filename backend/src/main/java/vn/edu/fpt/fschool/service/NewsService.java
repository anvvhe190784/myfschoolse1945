package vn.edu.fpt.fschool.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.fschool.dto.NewsDtos.CreateNewsRequest;
import vn.edu.fpt.fschool.dto.NewsDtos.NewsResponse;
import vn.edu.fpt.fschool.event.NotificationEvents.NewsPublishedEvent;
import vn.edu.fpt.fschool.model.News;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.NewsRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

@Service
@Slf4j
public class NewsService {

  public static final String NEWS_VIEWS_KEY = "news:views";

  private final NewsRepository newsRepository;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final StringRedisTemplate stringRedisTemplate;

  private final Map<Long, AtomicLong> inMemoryViews = new ConcurrentHashMap<>();

  public NewsService(
      NewsRepository newsRepository,
      UserRepository userRepository,
      ApplicationEventPublisher eventPublisher,
      @Autowired(required = false) StringRedisTemplate stringRedisTemplate) {
    this.newsRepository = newsRepository;
    this.userRepository = userRepository;
    this.eventPublisher = eventPublisher;
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Transactional(readOnly = true)
  public List<NewsResponse> getAllPublishedNews(Integer limit) {
    return findPublishedNews(limit);
  }

  @Transactional(readOnly = true)
  public List<NewsResponse> getAllPublishedNews() {
    return findPublishedNews(null);
  }

  private List<NewsResponse> findPublishedNews(Integer limit) {
    int safeLimit = (limit != null && limit > 0) ? Math.min(limit, 50) : 20;
    Pageable pageable = PageRequest.of(0, safeLimit);
    List<News> list =
        newsRepository.findByIsPublishedTrueOrderByPublishedAtDesc(pageable).getContent();
    return list.stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public NewsResponse getNewsDetail(Long id) {
    News news =
        newsRepository
            .findById(id)
            .orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy tin tức với ID: " + id));

    long pendingViews;
    if (stringRedisTemplate != null) {
      try {
        pendingViews =
            stringRedisTemplate.opsForHash().increment(NEWS_VIEWS_KEY, id.toString(), 1L);
      } catch (Exception e) {
        log.warn("Không thể tăng views trong Redis cho newsId {}: {}", id, e.getMessage());
        pendingViews = inMemoryViews.computeIfAbsent(id, k -> new AtomicLong(0)).incrementAndGet();
      }
    } else {
      pendingViews = inMemoryViews.computeIfAbsent(id, k -> new AtomicLong(0)).incrementAndGet();
    }

    int totalViews = (news.getViewsCount() != null ? news.getViewsCount() : 0) + (int) pendingViews;
    return toResponseWithViews(news, totalViews);
  }

  public Map<Long, AtomicLong> getInMemoryViews() {
    return inMemoryViews;
  }

  @Transactional
  public NewsResponse createNews(CreateNewsRequest req, String adminEmail) {
    User admin =
        userRepository
            .findByEmailIgnoreCase(adminEmail)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Không tìm thấy tài khoản quản trị: " + adminEmail));

    if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
      throw new IllegalStateException("Chỉ tài khoản ADMIN mới có quyền tạo tin tức");
    }

    News news = new News();
    news.setTitle(req.title().trim());
    news.setSummary(req.summary().trim());
    news.setContent(req.content().trim());
    news.setThumbnailUrl(req.thumbnailUrl());
    news.setAuthor(admin);
    news.setCategory(
        req.category() != null && !req.category().isBlank()
            ? req.category().toUpperCase()
            : "GENERAL");
    news.setViewsCount(0);
    news.setIsPublished(true);
    news.setPublishedAt(OffsetDateTime.now(ZoneOffset.UTC));
    news.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

    News saved = newsRepository.save(news);

    // Phát sự kiện miền: Tin tức mới đã xuất bản (NotificationEventListener xử lý ngầm)
    eventPublisher.publishEvent(
        new NewsPublishedEvent(saved.getNewsId(), saved.getTitle(), saved.getSummary()));

    return toResponse(saved);
  }

  private NewsResponse toResponse(News n) {
    return toResponseWithViews(n, n.getViewsCount() != null ? n.getViewsCount() : 0);
  }

  private NewsResponse toResponseWithViews(News n, int views) {
    String authorName = n.getAuthor() != null ? n.getAuthor().getFullName() : "Ban Truyền Thông";
    return new NewsResponse(
        n.getNewsId(),
        n.getTitle(),
        n.getSummary(),
        n.getContent(),
        n.getThumbnailUrl(),
        authorName,
        n.getCategory(),
        views,
        n.getPublishedAt());
  }
}
