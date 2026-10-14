package vn.edu.fpt.fschool.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
  Optional<Course> findByCourseCode(String courseCode);

  boolean existsByCourseCode(String courseCode);
}
