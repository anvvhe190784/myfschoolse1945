package vn.edu.fpt.fschool.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.ClubMembership;

@Repository
public interface ClubMembershipRepository extends JpaRepository<ClubMembership, Long> {

  boolean existsByClub_ClubIdAndUser_UserId(Long clubId, Long userId);

  Optional<ClubMembership> findByClub_ClubIdAndUser_UserId(Long clubId, Long userId);
}
