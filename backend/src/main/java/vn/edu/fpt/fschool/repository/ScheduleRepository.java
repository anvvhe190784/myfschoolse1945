package vn.edu.fpt.fschool.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
  @Override
  @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "lecturer"})
  Optional<Schedule> findById(Long id);

  @Override
  @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "lecturer"})
  List<Schedule> findAll();

  @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "lecturer"})
  List<Schedule> findByClassEntity_ClassId(Long classId);

  @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "lecturer"})
  List<Schedule> findByDate(LocalDate date);

  @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "lecturer"})
  @Query(
      "SELECT s FROM Schedule s JOIN s.classEntity c WHERE s.date BETWEEN :startDate AND :endDate ORDER BY s.date, s.slotNumber")
  List<Schedule> findSchedulesInRange(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
