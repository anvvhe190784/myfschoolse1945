package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "schedules")
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
    if (createdAt == null) createdAt = OffsetDateTime.now();
    if (status == null) status = "UPCOMING";
    if (sessionNo == null) sessionNo = 1;
  }

  public Long getScheduleId() {
    return scheduleId;
  }

  public void setScheduleId(Long scheduleId) {
    this.scheduleId = scheduleId;
  }

  public ClassEntity getClassEntity() {
    return classEntity;
  }

  public void setClassEntity(ClassEntity classEntity) {
    this.classEntity = classEntity;
  }

  public Integer getSlotNumber() {
    return slotNumber;
  }

  public void setSlotNumber(Integer slotNumber) {
    this.slotNumber = slotNumber;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }

  public String getRoom() {
    return room;
  }

  public void setRoom(String room) {
    this.room = room;
  }

  public Integer getSessionNo() {
    return sessionNo;
  }

  public void setSessionNo(Integer sessionNo) {
    this.sessionNo = sessionNo;
  }

  public User getLecturer() {
    return lecturer;
  }

  public void setLecturer(User lecturer) {
    this.lecturer = lecturer;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
