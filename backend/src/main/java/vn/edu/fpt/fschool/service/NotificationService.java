package vn.edu.fpt.fschool.service;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.fschool.dto.NotificationDtos.NotificationResponse;
import vn.edu.fpt.fschool.dto.NotificationDtos.UnreadCountResponse;
import vn.edu.fpt.fschool.model.Notification;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.NotificationRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public Page<NotificationResponse> getNotifications(
      String email, Pageable pageable, Boolean unreadOnly) {
    User user = getUserByEmail(email);
    Page<Notification> page =
        (Boolean.TRUE.equals(unreadOnly))
            ? notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(
                user.getUserId(), pageable)
            : notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId(), pageable);
    return page.map(NotificationResponse::from);
  }

  @Transactional(readOnly = true)
  public List<NotificationResponse> getNotifications(String email) {
    User user = getUserByEmail(email);
    return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId()).stream()
        .map(NotificationResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public UnreadCountResponse getUnreadCount(String email) {
    User user = getUserByEmail(email);
    long count = notificationRepository.countByUserIdAndIsReadFalse(user.getUserId());
    return new UnreadCountResponse(count);
  }

  @Transactional
  public NotificationResponse markAsRead(Long notificationId, String email) {
    User user = getUserByEmail(email);
    Notification notification =
        notificationRepository
            .findByNotificationIdAndUser_UserId(notificationId, user.getUserId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Không tìm thấy thông báo với ID: " + notificationId));

    notification.setIsRead(true);
    Notification saved = notificationRepository.save(notification);
    return NotificationResponse.from(saved);
  }

  @Transactional
  public int markAllAsRead(String email) {
    User user = getUserByEmail(email);
    return notificationRepository.markAllAsReadByUserId(user.getUserId());
  }

  @Transactional
  public NotificationResponse createNotification(
      User user, String title, String content, String type, Long targetId) {
    Notification notification =
        Notification.builder()
            .user(user)
            .title(title)
            .content(content)
            .type(type)
            .targetId(targetId)
            .isRead(false)
            .createdAt(OffsetDateTime.now())
            .build();

    Notification saved = notificationRepository.save(notification);
    return NotificationResponse.from(saved);
  }

  private User getUserByEmail(String email) {
    return userRepository
        .findByEmailIgnoreCase(email)
        .orElseThrow(
            () -> new IllegalArgumentException("Không tìm thấy người dùng với email: " + email));
  }
}
