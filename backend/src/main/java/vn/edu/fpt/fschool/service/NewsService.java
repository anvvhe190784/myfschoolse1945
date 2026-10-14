package vn.edu.fpt.fschool.service;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
@RequiredArgsConstructor
public class NewsService {

  private final NewsRepository newsRepository;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public List<NewsResponse> getAllPublishedNews(Integer limit) {
    List<News> list = newsRepository.findByIsPublishedTrueOrderByPublishedAtDesc();
    if (limit != null && limit > 0) {
      list = list.stream().limit(limit).toList();
    }
    return list.stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<NewsResponse> getAllPublishedNews() {
    return getAllPublishedNews(null);
  }

  @Transactional
  public NewsResponse getNewsDetail(Long id) {
    News news = newsRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tin tức với ID: " + id));

    // Increment views count
    news.setViewsCount(news.getViewsCount() + 1);
    newsRepository.save(news);

    return toResponse(news);
  }

  @Transactional
  public NewsResponse createNews(CreateNewsRequest req, String adminEmail) {
    User admin = userRepository.findByEmailIgnoreCase(adminEmail)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản quản trị: " + adminEmail));

    if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
      throw new IllegalStateException("Chỉ tài khoản ADMIN mới có quyền tạo tin tức");
    }

    News news = new News();
    news.setTitle(req.title().trim());
    news.setSummary(req.summary().trim());
    news.setContent(req.content().trim());
    news.setThumbnailUrl(req.thumbnailUrl());
    news.setAuthor(admin);
    news.setCategory(req.category() != null && !req.category().isBlank() ? req.category().toUpperCase() : "GENERAL");
    news.setViewsCount(0);
    news.setIsPublished(true);
    news.setPublishedAt(OffsetDateTime.now());
    news.setCreatedAt(OffsetDateTime.now());

    News saved = newsRepository.save(news);

    // Phát sự kiện miền: Tin tức mới đã xuất bản (NotificationEventListener xử lý ngầm)
    eventPublisher.publishEvent(new NewsPublishedEvent(saved.getNewsId(), saved.getTitle(), saved.getSummary()));

    return toResponse(saved);
  }

  private NewsResponse toResponse(News n) {
    String authorName = n.getAuthor() != null ? n.getAuthor().getFullName() : "Ban Truyền Thông";
    return new NewsResponse(
        n.getNewsId(),
        n.getTitle(),
        n.getSummary(),
        n.getContent(),
        n.getThumbnailUrl(),
        authorName,
        n.getCategory(),
        n.getViewsCount(),
        n.getPublishedAt()
    );
  }
}
