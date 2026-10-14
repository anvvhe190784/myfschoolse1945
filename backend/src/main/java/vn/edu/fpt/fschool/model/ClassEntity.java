package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "classes", uniqueConstraints = {
    @UniqueConstraint(name = "uq_class_course_semester", columnNames = {"class_code", "course_id", "semester"})
})
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
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (academicYear == null) academicYear = 2026;
    }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public User getLecturer() { return lecturer; }
    public void setLecturer(User lecturer) { this.lecturer = lecturer; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public Integer getAcademicYear() { return academicYear; }
    public void setAcademicYear(Integer academicYear) { this.academicYear = academicYear; }

    public String getRoomDefault() { return roomDefault; }
    public void setRoomDefault(String roomDefault) { this.roomDefault = roomDefault; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
