package vn.edu.fpt.fschool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
  @Operation(summary = "Lấy danh sách thông báo phân trang của người dùng hiện tại", description = "Lấy danh sách thông báo sắp xếp theo thời gian mới nhất có hỗ trợ phân trang")
  public ResponseEntity<Page<NotificationResponse>> getNotifications(
      Authentication authentication,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "false") boolean unreadOnly
  ) {
    String email = authentication.getName();
    Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
    return ResponseEntity.ok(notificationService.getNotifications(email, pageable, unreadOnly));
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
