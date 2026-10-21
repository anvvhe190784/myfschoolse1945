package vn.edu.fpt.fschool.repository;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.Attendance;

@Repository
@NullMarked
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

  @EntityGraph(attributePaths = {"student", "recordedBy"})
  @Query("SELECT a FROM Attendance a WHERE a.schedule.scheduleId = :scheduleId")
  List<Attendance> findByScheduleId(@Param("scheduleId") Long scheduleId);

  @EntityGraph(attributePaths = {"student", "recordedBy"})
  @Query(
      "SELECT a FROM Attendance a WHERE a.schedule.scheduleId = :scheduleId AND a.student.userId = :studentId")
  Optional<Attendance> findByScheduleIdAndStudentId(
      @Param("scheduleId") Long scheduleId, @Param("studentId") Long studentId);

  @Query(
      "SELECT COUNT(a) FROM Attendance a WHERE a.schedule.scheduleId = :scheduleId AND a.status = :status")
  long countByScheduleIdAndStatus(
      @Param("scheduleId") Long scheduleId, @Param("status") String status);

  @EntityGraph(attributePaths = {"student", "recordedBy"})
  List<Attendance> findBySchedule_ScheduleId(Long scheduleId);

  @EntityGraph(attributePaths = {"student", "recordedBy"})
  Optional<Attendance> findBySchedule_ScheduleIdAndStudent_UserId(
      Long scheduleId, Long studentId);

  long countBySchedule_ScheduleIdAndStatus(Long scheduleId, String status);
}
