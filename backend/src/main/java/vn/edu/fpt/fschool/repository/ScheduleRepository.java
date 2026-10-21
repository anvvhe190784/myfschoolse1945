package vn.edu.fpt.fschool.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.Schedule;

@Repository
@NullMarked
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

  @Override
  @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "lecturer"})
  Optional<Schedule> findById(Long id);

  @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "lecturer"})
  List<Schedule> findByClassEntity_ClassId(Long classId);

  @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "lecturer"})
  List<Schedule> findByDate(LocalDate date);

  @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "lecturer"})
  @Query(
      "SELECT s FROM Schedule s WHERE s.classEntity.classId IN "
          + "(SELECT ce.classEntity.classId FROM ClassEnrollment ce WHERE ce.student.userId = :studentId AND ce.status = 'ENROLLED') "
          + "AND s.date = :date ORDER BY s.slotNumber ASC")
  List<Schedule> findByStudentIdAndDate(
      @Param("studentId") Long studentId, @Param("date") LocalDate date);

  @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "lecturer"})
  @Query(
      "SELECT s FROM Schedule s WHERE s.classEntity.classId IN "
          + "(SELECT ce.classEntity.classId FROM ClassEnrollment ce WHERE ce.student.userId = :studentId AND ce.status = 'ENROLLED') "
          + "AND s.date BETWEEN :startDate AND :endDate ORDER BY s.date ASC, s.slotNumber ASC")
  List<Schedule> findByStudentIdAndDateBetween(
      @Param("studentId") Long studentId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "lecturer"})
  @Query(
      "SELECT s FROM Schedule s WHERE s.lecturer.userId = :lecturerId AND s.date = :date ORDER BY s.slotNumber ASC")
  List<Schedule> findByLecturerIdAndDate(
      @Param("lecturerId") Long lecturerId, @Param("date") LocalDate date);

  @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "lecturer"})
  @Query(
      "SELECT s FROM Schedule s WHERE s.lecturer.userId = :lecturerId AND s.date BETWEEN :startDate AND :endDate ORDER BY s.date ASC, s.slotNumber ASC")
  List<Schedule> findByLecturerIdAndDateBetween(
      @Param("lecturerId") Long lecturerId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "lecturer"})
  @Query(
      "SELECT s FROM Schedule s WHERE s.date BETWEEN :startDate AND :endDate ORDER BY s.date ASC, s.slotNumber ASC")
  List<Schedule> findByDateBetween(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "lecturer"})
  @Query(
      "SELECT s FROM Schedule s JOIN s.classEntity c WHERE s.date BETWEEN :startDate AND :endDate ORDER BY s.date, s.slotNumber")
  List<Schedule> findSchedulesInRange(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  @Query("SELECT DISTINCT s.classEntity.semester FROM Schedule s WHERE s.classEntity.semester IS NOT NULL ORDER BY s.classEntity.semester DESC")
  List<String> findDistinctSemesters();

  boolean existsByClassEntity_ClassIdAndDateAndSlotNumber(
      Long classId, LocalDate date, Integer slotNumber);

  List<Schedule> findByDateBeforeAndStatus(LocalDate date, String status);
}
