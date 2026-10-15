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
  private final vn.edu.fpt.fschool.mapper.AcademicMapper academicMapper;

  public ClubService(
      ClubRepository clubRepository, vn.edu.fpt.fschool.mapper.AcademicMapper academicMapper) {
    this.clubRepository = clubRepository;
    this.academicMapper = academicMapper;
  }

  @Transactional(readOnly = true)
  public List<ClubResponse> getAllActiveClubs() {
    return academicMapper.toClubResponseList(clubRepository.findByIsActiveTrue());
  }

  @Transactional(readOnly = true)
  public ClubResponse getClubDetail(Long id) {
    Club club =
        clubRepository
            .findById(id)
            .orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy câu lạc bộ với ID: " + id));
    return academicMapper.toClubResponse(club);
  }
}
