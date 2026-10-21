package vn.edu.fpt.fschool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.fschool.dto.AttendanceDtos.ScheduleAttendanceResponse;
import vn.edu.fpt.fschool.dto.AttendanceDtos.TakeAttendanceRequest;
import vn.edu.fpt.fschool.service.AttendanceService;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class AttendanceController {

  private final AttendanceService attendanceService;

  @GetMapping("/{id}/attendance")
  @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN', 'STUDENT')")
  public ResponseEntity<ScheduleAttendanceResponse> getScheduleAttendance(
      @PathVariable Long id, Authentication authentication) {
    String email = (authentication != null) ? authentication.getName() : null;
    return ResponseEntity.ok(attendanceService.getScheduleAttendance(id, email));
  }

  @PutMapping("/{id}/attendance")
  @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
  public ResponseEntity<ScheduleAttendanceResponse> saveScheduleAttendance(
      @PathVariable Long id,
      @Valid @RequestBody TakeAttendanceRequest request,
      Authentication authentication) {
    String email = (authentication != null) ? authentication.getName() : null;
    return ResponseEntity.ok(attendanceService.saveScheduleAttendance(id, request, email));
  }
}
