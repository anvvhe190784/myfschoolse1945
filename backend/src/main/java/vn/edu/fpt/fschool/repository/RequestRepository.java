package vn.edu.fpt.fschool.repository;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.RequestEntity;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity, Long> {
  @Override
  @EntityGraph(attributePaths = {"student", "requester"})
  List<RequestEntity> findAll();

  @Override
  @EntityGraph(attributePaths = {"student", "requester"})
  org.springframework.data.domain.Page<RequestEntity> findAll(
      org.springframework.data.domain.Pageable pageable);

  @EntityGraph(attributePaths = {"student", "requester"})
  List<RequestEntity> findByStudent_UserId(Long studentId);

  @EntityGraph(attributePaths = {"student", "requester"})
  List<RequestEntity> findByRequester_UserId(Long requesterId);

  List<RequestEntity> findByStatus(String status);

  List<RequestEntity> findByStatusAndCreatedAtBefore(String status, OffsetDateTime cutoff);

  List<RequestEntity> findByStatusInAndCreatedAtBefore(
      List<String> statuses, OffsetDateTime cutoff);
}
