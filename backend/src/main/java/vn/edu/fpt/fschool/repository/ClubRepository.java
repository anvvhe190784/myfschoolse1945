package vn.edu.fpt.fschool.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.Club;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {
    @Override
    @EntityGraph(attributePaths = {"leader"})
    Optional<Club> findById(Long id);

    @EntityGraph(attributePaths = {"leader"})
    List<Club> findByIsActiveTrue();

    @EntityGraph(attributePaths = {"leader"})
    List<Club> findByCategory(String category);
}
