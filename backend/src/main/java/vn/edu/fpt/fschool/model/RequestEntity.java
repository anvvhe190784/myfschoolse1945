package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "requests")
@Getter
@Setter
@SuppressWarnings("JpaDataSourceORMInspection")
public class RequestEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "request_id")
  private Long requestId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private User student;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "requester_id", nullable = false)
  private User requester;

  @Column(name = "request_type", nullable = false)
  private String requestType; // 'ATTENDANCE_EXEMPTION', 'COURSE_WITHDRAWAL', 'LEAVE_OF_ABSENCE',

  // 'GRADE_REVIEW', 'CERTIFICATE_REQUEST', 'OTHER'

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "attachment_url")
  private String attachmentUrl;

  @Column(name = "status", nullable = false)
  private String status = "PENDING"; // 'PENDING', 'PROCESSING', 'APPROVED', 'REJECTED'

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "handler_id")
  private User handler;

  @Column(name = "response_note", columnDefinition = "TEXT")
  private String responseNote;

  @Version
  @Column(name = "version", nullable = false)
  private Long version = 0L;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    if (updatedAt == null) updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    if (status == null) status = "PENDING";
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
  }
}
