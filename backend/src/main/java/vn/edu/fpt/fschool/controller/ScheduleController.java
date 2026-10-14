package vn.edu.fpt.fschool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.fschool.dto.ScheduleDtos.ScheduleResponse;
import vn.edu.fpt.fschool.service.ScheduleService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@Tag(name = "2. Schedules (Lịch học)", description = "Quản lý và tra cứu lịch học cho Sinh viên / Giảng viên")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    @Operation(summary = "Danh sách lịch học (Use Case 4: ListSchedule)", description = "Xem danh sách lịch học theo ngày hoặc toàn bộ")
    public ResponseEntity<List<ScheduleResponse>> getSchedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(scheduleService.getSchedules(date));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết lịch học (Use Case 9: ViewScheduleDetail)", description = "Xem chi tiết một buổi học theo ID")
    public ResponseEntity<ScheduleResponse> getScheduleDetail(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getScheduleDetail(id));
    }
}
