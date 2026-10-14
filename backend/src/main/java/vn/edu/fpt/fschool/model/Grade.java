package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "grades")
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
    updatedAt = OffsetDateTime.now();
    if (status == null) status = "PUBLISHED";
  }

  public Long getGradeId() {
    return gradeId;
  }

  public void setGradeId(Long gradeId) {
    this.gradeId = gradeId;
  }

  public ClassEntity getClassEntity() {
    return classEntity;
  }

  public void setClassEntity(ClassEntity classEntity) {
    this.classEntity = classEntity;
  }

  public User getStudent() {
    return student;
  }

  public void setStudent(User student) {
    this.student = student;
  }

  public String getAssessmentName() {
    return assessmentName;
  }

  public void setAssessmentName(String assessmentName) {
    this.assessmentName = assessmentName;
  }

  public BigDecimal getWeight() {
    return weight;
  }

  public void setWeight(BigDecimal weight) {
    this.weight = weight;
  }

  public BigDecimal getScore() {
    return score;
  }

  public void setScore(BigDecimal score) {
    this.score = score;
  }

  public String getFeedback() {
    return feedback;
  }

  public void setFeedback(String feedback) {
    this.feedback = feedback;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
