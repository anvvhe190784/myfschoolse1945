package vn.edu.fpt.fschool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.fschool.dto.AdminDtos.*;
import vn.edu.fpt.fschool.service.AdminService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "7. Admin: User Management", description = "Dành riêng cho ADMIN: Thao tác CRUD người dùng trực tiếp trên Database")
public class AdminUserController {

    private final AdminService adminService;

    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    @Operation(summary = "Tạo tài khoản người dùng mới (Admin CRUD)", description = "Khởi tạo tài khoản cho Sinh viên, Giảng viên, Phụ huynh hoặc Admin mới. Mật khẩu được mã hóa BCrypt.")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(req));
    }

    @GetMapping
    @Operation(summary = "Xem toàn bộ danh sách người dùng (Admin CRUD)", description = "Lấy danh sách tất cả tài khoản trong hệ thống kèm vai trò và trạng thái")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Khóa hoặc Mở khóa tài khoản (Admin CRUD)", description = "Cập nhật trạng thái người dùng thành ACTIVE, LOCKED, hoặc INACTIVE")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest req
    ) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, req));
    }
}
