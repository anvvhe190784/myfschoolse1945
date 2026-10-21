package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.*;

@Entity
@Table(
    name = "attendances",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_schedule_student_attendance",
          columnNames = {"schedule_id", "student_id"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SuppressWarnings("JpaDataSourceORMInspection")
public class Attendance {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "attendance_id")
  private Long attendanceId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "schedule_id", nullable = false)
  private Schedule schedule;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private User student;

  @Column(name = "status", nullable = false)
  @Builder.Default
  private String status = "PRESENT"; // 'PRESENT', 'ABSENT', 'LATE'

  @Column(name = "note")
  private String note;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recorded_by")
  private User recordedBy;

  @Column(name = "recorded_at", nullable = false)
  private OffsetDateTime recordedAt;

  @PrePersist
  protected void onCreate() {
    if (recordedAt == null) {
      recordedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
    if (status == null) {
      status = "PRESENT";
    }
  }

  @PreUpdate
  protected void onUpdate() {
    recordedAt = OffsetDateTime.now(ZoneOffset.UTC);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Attendance other)) return false;
    return attendanceId != null && attendanceId.equals(other.getAttendanceId());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
