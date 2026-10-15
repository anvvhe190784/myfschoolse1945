package vn.edu.fpt.fschool.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.fschool.dto.AdminDtos.*;
import vn.edu.fpt.fschool.service.AdminService;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final AdminService adminService;

  public AdminUserController(AdminService adminService) {
    this.adminService = adminService;
  }

  @PostMapping
  public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(req));
  }

  @GetMapping
  public ResponseEntity<List<UserResponse>> getAllUsers(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    int safeSize = Math.clamp(size, 1, 50);
    return ResponseEntity.ok(
        adminService.getAllUsers(
            org.springframework.data.domain.PageRequest.of(Math.max(0, page), safeSize)));
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<UserResponse> updateUserStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest req) {
    return ResponseEntity.ok(adminService.updateUserStatus(id, req));
  }
}
