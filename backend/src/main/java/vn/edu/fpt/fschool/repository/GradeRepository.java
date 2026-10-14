package vn.edu.fpt.fschool.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.Grade;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    @Override
    @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "student"})
    Optional<Grade> findById(Long id);

    @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "student"})
    List<Grade> findByStudent_UserId(Long studentId);

    @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "student"})
    List<Grade> findByClassEntity_ClassId(Long classId);

    @EntityGraph(attributePaths = {"classEntity", "classEntity.course", "student"})
    List<Grade> findByStudent_UserIdAndClassEntity_ClassId(Long studentId, Long classId);
}
