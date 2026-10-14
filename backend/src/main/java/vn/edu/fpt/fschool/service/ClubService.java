package vn.edu.fpt.fschool.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.fschool.dto.ClubDtos.ClubResponse;
import vn.edu.fpt.fschool.model.Club;
import vn.edu.fpt.fschool.repository.ClubRepository;

@Service
public class ClubService {

  private final ClubRepository clubRepository;

  public ClubService(ClubRepository clubRepository) {
    this.clubRepository = clubRepository;
  }

  @Transactional(readOnly = true)
  public List<ClubResponse> getAllActiveClubs() {
    return clubRepository.findByIsActiveTrue().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public ClubResponse getClubDetail(Long id) {
    Club club =
        clubRepository
            .findById(id)
            .orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy câu lạc bộ với ID: " + id));
    return toResponse(club);
  }

  private ClubResponse toResponse(Club c) {
    String leaderName = c.getLeader() != null ? c.getLeader().getFullName() : "N/A";
    return new ClubResponse(
        c.getClubId(),
        c.getClubName(),
        c.getDescription(),
        c.getCategory(),
        c.getAvatarUrl(),
        c.getBannerUrl(),
        leaderName,
        c.getContactEmail(),
        c.getMemberCount(),
        c.getIsActive());
  }
}
