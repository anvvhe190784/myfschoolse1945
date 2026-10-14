package vn.edu.fpt.fschool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.ClassEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {
    Optional<ClassEntity> findByClassCode(String classCode);
    boolean existsByClassCode(String classCode);
    List<ClassEntity> findByLecturer_UserId(Long lecturerId);
}
