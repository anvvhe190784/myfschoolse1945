package vn.edu.fpt.fschool.controller;

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
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<Page<NotificationResponse>> getNotifications(
      Authentication authentication,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "false") boolean unreadOnly) {
    String email = authentication.getName();
    Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
    return ResponseEntity.ok(notificationService.getNotifications(email, pageable, unreadOnly));
  }

  @GetMapping("/unread-count")
  public ResponseEntity<UnreadCountResponse> getUnreadCount(Authentication authentication) {
    String email = authentication.getName();
    return ResponseEntity.ok(notificationService.getUnreadCount(email));
  }

  @PatchMapping("/{id}/read")
  public ResponseEntity<NotificationResponse> markAsRead(
      Authentication authentication, @PathVariable Long id) {
    String email = authentication.getName();
    return ResponseEntity.ok(notificationService.markAsRead(id, email));
  }

  @PatchMapping("/read-all")
  public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
    String email = authentication.getName();
    int updatedCount = notificationService.markAllAsRead(email);
    return ResponseEntity.ok(
        Map.of(
            "message",
            "Đã đánh dấu đã đọc tất cả thông báo",
            "updatedCount",
            updatedCount,
            "success",
            true));
  }
}
