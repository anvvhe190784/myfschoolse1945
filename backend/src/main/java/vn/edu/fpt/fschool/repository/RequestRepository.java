package vn.edu.fpt.fschool.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.RequestEntity;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity, Long> {
  List<RequestEntity> findByStudent_UserId(Long studentId);

  List<RequestEntity> findByRequester_UserId(Long requesterId);

  List<RequestEntity> findByStatus(String status);
}
