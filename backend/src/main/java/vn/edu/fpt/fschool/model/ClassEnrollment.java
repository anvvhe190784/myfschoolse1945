package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.*;

@Entity
@Table(
    name = "class_enrollments",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_class_student",
          columnNames = {"class_id", "student_id"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassEnrollment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "enrollment_id")
  private Long enrollmentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "class_id", nullable = false)
  private ClassEntity classEntity;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private User student;

  @Column(name = "status", nullable = false)
  @Builder.Default
  private String status = "ENROLLED"; // 'ENROLLED', 'DROPPED', 'COMPLETED'

  @Column(name = "enrolled_at", nullable = false, updatable = false)
  private OffsetDateTime enrolledAt;

  @PrePersist
  protected void onCreate() {
    if (enrolledAt == null) {
      enrolledAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
    if (status == null) {
      status = "ENROLLED";
    }
  }
}
