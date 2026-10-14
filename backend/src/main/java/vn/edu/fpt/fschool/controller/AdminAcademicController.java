package vn.edu.fpt.fschool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/academic")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "8. Admin: Academic & Requests", description = "Dành riêng cho ADMIN: CRUD Môn học, Lớp học, Xếp lịch và Duyệt đơn từ")
public class AdminAcademicController {

    private final AdminService adminService;

    public AdminAcademicController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/courses")
    @Operation(summary = "Tạo môn học mới (Admin CRUD Course)", description = "Thêm mã môn, tên môn, số tín chỉ vào Database")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createCourse(req));
    }

    @GetMapping("/courses")
    @Operation(summary = "Xem toàn bộ môn học (Admin CRUD Course)", description = "Lấy danh sách tất cả các môn học đã mở")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(adminService.getAllCourses());
    }

    @PostMapping("/classes")
    @Operation(summary = "Mở lớp học mới (Admin CRUD Class)", description = "Mở lớp học mới, gán môn học, giảng viên phụ trách và phòng học")
    public ResponseEntity<ClassResponse> createClass(@Valid @RequestBody CreateClassRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createClass(req));
    }

    @GetMapping("/classes")
    @Operation(summary = "Xem toàn bộ lớp học (Admin CRUD Class)", description = "Lấy danh sách các lớp học hiện có trong học kỳ")
    public ResponseEntity<List<ClassResponse>> getAllClasses() {
        return ResponseEntity.ok(adminService.getAllClasses());
    }

    @PostMapping("/schedules")
    @Operation(summary = "Xếp lịch học mới (Admin CRUD Schedule)", description = "Tạo buổi học mới cho lớp, phân slot 1-8, phòng học và ngày học")
    public ResponseEntity<ScheduleResponse> createSchedule(@Valid @RequestBody CreateScheduleRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createSchedule(req));
    }

    @PatchMapping("/requests/{id}/process")
    @Operation(summary = "Duyệt hoặc Từ chối đơn từ (Admin Review)", description = "Cập nhật trạng thái đơn thành APPROVED hoặc REJECTED kèm ghi chú phản hồi")
    public ResponseEntity<String> processRequest(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody UpdateRequestStatusDto dto
    ) {
        String adminEmail = authentication.getName();
        adminService.processRequest(id, adminEmail, dto);
        return ResponseEntity.ok("Xử lý đơn từ thành công!");
    }
}
