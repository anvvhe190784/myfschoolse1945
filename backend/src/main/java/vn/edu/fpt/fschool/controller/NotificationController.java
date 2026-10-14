package vn.edu.fpt.fschool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.fschool.dto.NotificationDtos.NotificationResponse;
import vn.edu.fpt.fschool.dto.NotificationDtos.UnreadCountResponse;
import vn.edu.fpt.fschool.service.NotificationService;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "12. Notifications (Thông báo)", description = "Hệ thống thông báo tin tức, bảo mật và học vụ")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  @Operation(summary = "Lấy danh sách thông báo của người dùng hiện tại", description = "Lấy danh sách thông báo sắp xếp theo thời gian mới nhất")
  public ResponseEntity<List<NotificationResponse>> getNotifications(Authentication authentication) {
    String email = authentication.getName();
    return ResponseEntity.ok(notificationService.getNotifications(email));
  }

  @GetMapping("/unread-count")
  @Operation(summary = "Lấy số lượng thông báo chưa đọc", description = "Đếm số lượng thông báo chưa đọc để hiển thị badge")
  public ResponseEntity<UnreadCountResponse> getUnreadCount(Authentication authentication) {
    String email = authentication.getName();
    return ResponseEntity.ok(notificationService.getUnreadCount(email));
  }

  @PatchMapping("/{id}/read")
  @Operation(summary = "Đánh dấu đã đọc một thông báo", description = "Chuyển trạng thái is_read = true cho một thông báo cụ thể")
  public ResponseEntity<NotificationResponse> markAsRead(
      Authentication authentication,
      @PathVariable Long id
  ) {
    String email = authentication.getName();
    return ResponseEntity.ok(notificationService.markAsRead(id, email));
  }

  @PatchMapping("/read-all")
  @Operation(summary = "Đánh dấu đã đọc tất cả", description = "Chuyển trạng thái is_read = true cho tất cả thông báo của người dùng")
  public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
    String email = authentication.getName();
    int updatedCount = notificationService.markAllAsRead(email);
    return ResponseEntity.ok(Map.of(
        "message", "Đã đánh dấu đã đọc tất cả thông báo",
        "updatedCount", updatedCount,
        "success", true
    ));
  }
}
