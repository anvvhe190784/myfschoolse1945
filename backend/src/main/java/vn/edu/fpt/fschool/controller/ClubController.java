package vn.edu.fpt.fschool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.fschool.dto.ClubDtos.ClubResponse;
import vn.edu.fpt.fschool.service.ClubService;

@RestController
@RequestMapping("/api/v1/clubs")
@Tag(
    name = "5. Clubs (Câu lạc bộ)",
    description = "Khám phá danh sách và thông tin chi tiết các câu lạc bộ")
public class ClubController {

  private final ClubService clubService;

  public ClubController(ClubService clubService) {
    this.clubService = clubService;
  }

  @GetMapping
  @Operation(
      summary = "Danh sách câu lạc bộ (Use Case 7: ListClub)",
      description = "Xem danh sách toàn bộ các câu lạc bộ đang hoạt động")
  public ResponseEntity<List<ClubResponse>> getAllClubs() {
    return ResponseEntity.ok(clubService.getAllActiveClubs());
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Chi tiết câu lạc bộ (Use Case 12: ViewClubDetail)",
      description = "Xem thông tin chi tiết, chủ nhiệm, số lượng thành viên của CLB")
  public ResponseEntity<ClubResponse> getClubDetail(@PathVariable Long id) {
    return ResponseEntity.ok(clubService.getClubDetail(id));
  }
}
