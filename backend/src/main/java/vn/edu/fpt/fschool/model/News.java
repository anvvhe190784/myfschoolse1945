package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "news")
@Getter
@Setter
public class News {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "news_id")
  private Long newsId;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
  private String summary;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "thumbnail_url")
  private String thumbnailUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id")
  private User author;

  @Column(name = "category", nullable = false)
  private String category = "GENERAL"; // 'GENERAL', 'ACADEMIC', 'EVENT', 'ACTIVITY', 'SCHOLARSHIP'

  @Column(name = "views_count", nullable = false)
  private Integer viewsCount = 0;

  @Column(name = "is_published", nullable = false)
  private Boolean isPublished = true;

  @Column(name = "published_at", nullable = false)
  private OffsetDateTime publishedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    if (publishedAt == null) publishedAt = OffsetDateTime.now(ZoneOffset.UTC);
    if (viewsCount == null) viewsCount = 0;
    if (isPublished == null) isPublished = true;
    if (category == null) category = "GENERAL";
  }
}
