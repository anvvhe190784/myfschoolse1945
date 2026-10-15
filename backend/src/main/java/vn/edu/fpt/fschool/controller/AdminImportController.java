package vn.edu.fpt.fschool.controller;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.fschool.dto.ImportDtos.ImportResultDto;
import vn.edu.fpt.fschool.service.AdminImportService;

@RestController
@RequestMapping("/api/v1/admin/import")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminImportController {

  private final AdminImportService adminImportService;

  @PostMapping(value = "/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImportResultDto> importUsers(@RequestParam("file") MultipartFile file) {
    return ResponseEntity.ok(adminImportService.importUsers(file));
  }

  @PostMapping(value = "/grades", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImportResultDto> importGrades(@RequestParam("file") MultipartFile file) {
    return ResponseEntity.ok(adminImportService.importGrades(file));
  }

  @PostMapping(value = "/schedules", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImportResultDto> importSchedules(@RequestParam("file") MultipartFile file) {
    return ResponseEntity.ok(adminImportService.importSchedules(file));
  }

  @PostMapping(value = "/enrollments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImportResultDto> importEnrollments(
      @RequestParam("file") MultipartFile file) {
    return ResponseEntity.ok(adminImportService.importEnrollments(file));
  }

  @GetMapping(value = "/templates/{type}", produces = "text/csv")
  public ResponseEntity<String> downloadTemplate(@PathVariable String type) {
    String csv = adminImportService.getTemplateContent(type);
    return ResponseEntity.ok()
        .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + "_template.csv")
        .body(csv);
  }
}
