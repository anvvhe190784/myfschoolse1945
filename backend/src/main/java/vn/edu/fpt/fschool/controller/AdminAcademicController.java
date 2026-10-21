package vn.edu.fpt.fschool.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.fschool.dto.AdminDtos.*;
import vn.edu.fpt.fschool.dto.RequestDtos.UpdateRequestStatusDto;
import vn.edu.fpt.fschool.dto.ScheduleDtos.CreateScheduleRequest;
import vn.edu.fpt.fschool.dto.ScheduleDtos.ScheduleResponse;
import vn.edu.fpt.fschool.service.AdminService;

@RestController
@RequestMapping("/api/v1/admin/academic")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAcademicController {

  private final AdminService adminService;

  public AdminAcademicController(AdminService adminService) {
    this.adminService = adminService;
  }

  @PostMapping("/courses")
  public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createCourse(req));
  }

  @GetMapping("/courses")
  public ResponseEntity<List<CourseResponse>> getAllCourses(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    int safeSize = Math.clamp(size, 1, 50);
    return ResponseEntity.ok(
        adminService.getAllCourses(
            org.springframework.data.domain.PageRequest.of(Math.max(0, page), safeSize)));
  }

  @PostMapping("/classes")
  public ResponseEntity<ClassResponse> createClass(@Valid @RequestBody CreateClassRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createClass(req));
  }

  @GetMapping("/classes")
  public ResponseEntity<List<ClassResponse>> getAllClasses(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    int safeSize = Math.clamp(size, 1, 50);
    return ResponseEntity.ok(
        adminService.getAllClasses(
            org.springframework.data.domain.PageRequest.of(Math.max(0, page), safeSize)));
  }

  @GetMapping("/students/{id}/classes")
  public ResponseEntity<List<ClassResponse>> getStudentClasses(
      @PathVariable Long id, @RequestParam(required = false) String semester) {
    return ResponseEntity.ok(adminService.getStudentClasses(id, semester));
  }

  @PostMapping("/schedules")
  public ResponseEntity<ScheduleResponse> createSchedule(
      @Valid @RequestBody CreateScheduleRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createSchedule(req));
  }

  @GetMapping("/requests")
  public ResponseEntity<List<vn.edu.fpt.fschool.dto.RequestDtos.RequestResponse>> getAllRequests(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    int safeSize = Math.clamp(size, 1, 50);
    return ResponseEntity.ok(
        adminService.getAllRequests(
            org.springframework.data.domain.PageRequest.of(Math.max(0, page), safeSize)));
  }

  @PatchMapping("/requests/{id}/process")
  public ResponseEntity<String> processRequest(
      @PathVariable Long id,
      Authentication authentication,
      @Valid @RequestBody UpdateRequestStatusDto dto) {
    String adminEmail = authentication.getName();
    adminService.processRequest(id, adminEmail, dto);
    return ResponseEntity.ok("Xử lý đơn từ thành công!");
  }
}
