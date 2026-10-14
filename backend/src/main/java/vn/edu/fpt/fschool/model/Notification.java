package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "notification_id")
  private Long notificationId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "type", nullable = false)
  private String type; // 'NEWS', 'SECURITY', 'ACADEMIC', 'REQUEST', 'SYSTEM'

  @Column(name = "target_id")
  private Long targetId;

  @Column(name = "is_read", nullable = false)
  @Builder.Default
  private Boolean isRead = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = OffsetDateTime.now();
    }
    if (isRead == null) {
      isRead = false;
    }
  }
}
