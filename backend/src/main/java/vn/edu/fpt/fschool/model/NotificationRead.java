package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.*;

@Entity
@Table(
    name = "notification_reads",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_notification_user_read",
          columnNames = {"notification_id", "user_id"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRead {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "notification_id", nullable = false)
  private Notification notification;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "read_at", nullable = false, updatable = false)
  private OffsetDateTime readAt;

  @PrePersist
  protected void onCreate() {
    if (readAt == null) {
      readAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
  }
}
