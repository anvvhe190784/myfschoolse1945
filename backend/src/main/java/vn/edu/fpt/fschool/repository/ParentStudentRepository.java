package vn.edu.fpt.fschool.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.ParentStudent;

@Repository
public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {

  boolean existsByParent_UserIdAndStudent_UserId(Long parentId, Long studentId);

  @EntityGraph(attributePaths = {"student"})
  List<ParentStudent> findByParent_UserId(Long parentId);

  @EntityGraph(attributePaths = {"parent"})
  List<ParentStudent> findByStudent_UserId(Long studentId);
}
