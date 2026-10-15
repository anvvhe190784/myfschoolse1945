package vn.edu.fpt.fschool.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.fschool.dto.ClubDtos.ClubResponse;
import vn.edu.fpt.fschool.service.ClubService;

@RestController
@RequestMapping("/api/v1/clubs")
public class ClubController {

  private final ClubService clubService;

  public ClubController(ClubService clubService) {
    this.clubService = clubService;
  }

  @GetMapping
  public ResponseEntity<List<ClubResponse>> getAllClubs() {
    return ResponseEntity.ok(clubService.getAllActiveClubs());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ClubResponse> getClubDetail(@PathVariable Long id) {
    return ResponseEntity.ok(clubService.getClubDetail(id));
  }
}
