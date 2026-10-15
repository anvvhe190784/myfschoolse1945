package vn.edu.fpt.fschool.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.fschool.dto.GradeDtos.GradeResponse;
import vn.edu.fpt.fschool.service.GradeService;

@RestController
@RequestMapping("/api/v1/grades")
public class GradeController {

  private final GradeService gradeService;

  public GradeController(GradeService gradeService) {
    this.gradeService = gradeService;
  }

  @GetMapping
  public ResponseEntity<List<GradeResponse>> getMyGrades(
      Authentication authentication,
      @RequestParam(required = false) Long classId,
      @RequestParam(required = false) Long studentId) {
    String email = authentication.getName();
    return ResponseEntity.ok(gradeService.getMyGrades(email, classId, studentId));
  }

  @GetMapping("/{id}")
  public ResponseEntity<GradeResponse> getGradeDetail(
      Authentication authentication, @PathVariable Long id) {
    String email = authentication != null ? authentication.getName() : null;
    return ResponseEntity.ok(gradeService.getGradeDetail(id, email));
  }
}
