package vn.edu.fpt.fschool.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.fschool.dto.ScheduleDtos.ScheduleResponse;
import vn.edu.fpt.fschool.service.ScheduleService;

@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

  private final ScheduleService scheduleService;

  public ScheduleController(ScheduleService scheduleService) {
    this.scheduleService = scheduleService;
  }

  @GetMapping
  public ResponseEntity<List<ScheduleResponse>> getSchedules(
      Authentication authentication,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required = false) Long studentId) {
    String email = (authentication != null) ? authentication.getName() : null;
    if (email != null) {
      return ResponseEntity.ok(scheduleService.getSchedules(email, date, studentId));
    }
    return ResponseEntity.ok(scheduleService.getSchedules(date));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ScheduleResponse> getScheduleDetail(
      Authentication authentication, @PathVariable Long id) {
    String email = (authentication != null) ? authentication.getName() : null;
    return ResponseEntity.ok(scheduleService.getScheduleDetail(id, email));
  }
}
