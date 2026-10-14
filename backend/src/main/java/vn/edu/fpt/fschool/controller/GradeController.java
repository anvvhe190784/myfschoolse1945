package vn.edu.fpt.fschool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.fschool.dto.GradeDtos.GradeResponse;
import vn.edu.fpt.fschool.service.GradeService;

@RestController
@RequestMapping("/api/v1/grades")
@Tag(name = "3. Grades (Bảng điểm)", description = "Tra cứu điểm học kỳ và chi tiết bài thi")
public class GradeController {

  private final GradeService gradeService;

  public GradeController(GradeService gradeService) {
    this.gradeService = gradeService;
  }

  @GetMapping
  @Operation(
      summary = "Danh sách điểm học kỳ (Use Case 5: ListGrade)",
      description = "Xem danh sách các đầu điểm của sinh viên đang đăng nhập")
  public ResponseEntity<List<GradeResponse>> getMyGrades(
      Authentication authentication, @RequestParam(required = false) Long classId) {
    String email = authentication.getName();
    return ResponseEntity.ok(gradeService.getMyGrades(email, classId));
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Chi tiết đầu điểm (Use Case 10: ViewGradeDetail)",
      description = "Xem chi tiết một đầu điểm thi và nhận xét của giảng viên")
  public ResponseEntity<GradeResponse> getGradeDetail(
      Authentication authentication, @PathVariable Long id) {
    String email = authentication != null ? authentication.getName() : null;
    return ResponseEntity.ok(gradeService.getGradeDetail(id, email));
  }
}
