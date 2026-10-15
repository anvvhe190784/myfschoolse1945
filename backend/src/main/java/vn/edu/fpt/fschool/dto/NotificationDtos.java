package vn.edu.fpt.fschool.dto;

import java.time.OffsetDateTime;
import vn.edu.fpt.fschool.model.Notification;

public class NotificationDtos {

  public record NotificationResponse(
      Long notificationId,
      Long userId,
      String title,
      String content,
      String type,
      Long targetId,
      Boolean isRead,
      OffsetDateTime createdAt) {
    public static NotificationResponse from(Notification n) {
      return from(n, Boolean.TRUE.equals(n.getIsRead()));
    }

    public static NotificationResponse from(Notification n, boolean isRead) {
      return new NotificationResponse(
          n.getNotificationId(),
          n.getUser() != null ? n.getUser().getUserId() : null,
          n.getTitle(),
          n.getContent(),
          n.getType(),
          n.getTargetId(),
          isRead,
          n.getCreatedAt());
    }
  }

  public record UnreadCountResponse(long unreadCount) {}
}
