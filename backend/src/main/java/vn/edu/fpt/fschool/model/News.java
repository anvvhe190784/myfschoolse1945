package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "news")
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
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (publishedAt == null) publishedAt = OffsetDateTime.now();
        if (viewsCount == null) viewsCount = 0;
        if (isPublished == null) isPublished = true;
        if (category == null) category = "GENERAL";
    }

    public Long getNewsId() { return newsId; }
    public void setNewsId(Long newsId) { this.newsId = newsId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getViewsCount() { return viewsCount; }
    public void setViewsCount(Integer viewsCount) { this.viewsCount = viewsCount; }

    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }

    public OffsetDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(OffsetDateTime publishedAt) { this.publishedAt = publishedAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
