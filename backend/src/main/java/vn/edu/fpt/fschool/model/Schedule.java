package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "schedules")
@Getter
@Setter
public class Schedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "schedule_id")
  private Long scheduleId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "class_id", nullable = false)
  private ClassEntity classEntity;

  @Column(name = "slot_number", nullable = false)
  private Integer slotNumber; // 1 to 8

  @Column(name = "date", nullable = false)
  private LocalDate date;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  @Column(name = "room", nullable = false)
  private String room;

  @Column(name = "session_no", nullable = false)
  private Integer sessionNo = 1;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lecturer_id")
  private User lecturer;

  @Column(name = "note")
  private String note;

  @Column(name = "status", nullable = false)
  private String status = "UPCOMING"; // 'UPCOMING', 'PRESENT', 'ABSENT', 'CANCELLED'

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    if (status == null) status = "UPCOMING";
    if (sessionNo == null) sessionNo = 1;
  }
}
