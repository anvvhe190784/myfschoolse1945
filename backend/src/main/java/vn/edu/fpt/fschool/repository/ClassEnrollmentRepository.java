package vn.edu.fpt.fschool.repository;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.ClassEnrollment;

@Repository
@NullMarked
public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, Long> {

  List<ClassEnrollment> findByStudent_UserIdAndStatus(Long studentId, String status);

  List<ClassEnrollment> findByStudent_UserIdAndClassEntity_Semester(
      Long studentId, String semester);

  Optional<ClassEnrollment> findByClassEntity_ClassIdAndStudent_UserId(
      Long classId, Long studentId);

  boolean existsByClassEntity_ClassIdAndStudent_UserIdAndStatus(
      Long classId, Long studentId, String status);
}
