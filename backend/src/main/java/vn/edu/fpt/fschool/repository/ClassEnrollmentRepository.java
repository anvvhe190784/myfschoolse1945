package vn.edu.fpt.fschool.repository;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.ClassEnrollment;

@Repository
@NullMarked
public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, Long> {

  List<ClassEnrollment> findByStudent_UserIdAndStatus(Long studentId, String status);

  @EntityGraph(attributePaths = {"student"})
  List<ClassEnrollment> findByClassEntity_ClassIdAndStatus(Long classId, String status);

  List<ClassEnrollment> findByStudent_UserIdAndClassEntity_Semester(
      Long studentId, String semester);

  Optional<ClassEnrollment> findByClassEntity_ClassIdAndStudent_UserId(
      Long classId, Long studentId);

  boolean existsByClassEntity_ClassIdAndStudent_UserIdAndStatus(
      Long classId, Long studentId, String status);

  @Query(
      "SELECT COUNT(e) > 0 FROM ClassEnrollment e, ParentStudent ps "
          + "WHERE e.classEntity.classId = :classId AND e.status = 'ENROLLED' "
          + "AND ps.student.userId = e.student.userId AND ps.parent.userId = :parentId")
  boolean existsChildEnrolled(@Param("classId") Long classId, @Param("parentId") Long parentId);

  boolean existsByStudent_UserIdAndClassEntity_Lecturer_UserIdAndStatus(
      Long studentId, Long lecturerId, String status);
}
