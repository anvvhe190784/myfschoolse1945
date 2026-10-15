package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "classes",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_class_course_semester",
          columnNames = {"class_code", "course_id", "semester"})
    })
@Getter
@Setter
public class ClassEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "class_id")
  private Long classId;

  @Column(name = "class_code", nullable = false)
  private String classCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lecturer_id")
  private User lecturer;

  @Column(name = "semester", nullable = false)
  private String semester;

  @Column(name = "academic_year", nullable = false)
  private Integer academicYear = 2026;

  @Column(name = "room_default")
  private String roomDefault;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    if (academicYear == null) academicYear = 2026;
  }
}
