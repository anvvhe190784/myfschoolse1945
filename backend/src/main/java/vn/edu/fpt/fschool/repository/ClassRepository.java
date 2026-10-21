package vn.edu.fpt.fschool.repository;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.ClassEntity;

@Repository
@NullMarked
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {
  @Override
  @EntityGraph(attributePaths = {"course", "lecturer"})
  List<ClassEntity> findAll();

  @Override
  @EntityGraph(attributePaths = {"course", "lecturer"})
  org.springframework.data.domain.Page<ClassEntity> findAll(
      org.springframework.data.domain.Pageable pageable);

  List<ClassEntity> findByClassCode(String classCode);

  @EntityGraph(attributePaths = {"course", "lecturer"})
  Optional<ClassEntity> findByClassCodeAndCourse_CourseCode(String classCode, String courseCode);

  boolean existsByClassCode(String classCode);

  boolean existsByClassCodeAndCourse_CourseIdAndSemester(
      String classCode, Long courseId, String semester);

  List<ClassEntity> findByLecturer_UserId(Long lecturerId);
}
