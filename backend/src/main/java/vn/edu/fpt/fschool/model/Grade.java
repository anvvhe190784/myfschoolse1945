package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "grades")
@Getter
@Setter
public class Grade {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "grade_id")
  private Long gradeId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "class_id", nullable = false)
  private ClassEntity classEntity;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private User student;

  @Column(name = "assessment_name", nullable = false)
  private String assessmentName;

  @Column(name = "weight", nullable = false, precision = 5, scale = 2)
  private BigDecimal weight;

  @Column(name = "score", precision = 4, scale = 2)
  private BigDecimal score;

  @Column(name = "feedback")
  private String feedback;

  @Column(name = "status", nullable = false)
  private String status = "PUBLISHED"; // 'DRAFT', 'PUBLISHED'

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @PrePersist
  @PreUpdate
  protected void onUpdate() {
    updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    if (status == null) status = "PUBLISHED";
  }
}
