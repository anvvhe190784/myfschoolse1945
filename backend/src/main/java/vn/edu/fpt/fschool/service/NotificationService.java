package vn.edu.fpt.fschool.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.fschool.dto.NotificationDtos.NotificationResponse;
import vn.edu.fpt.fschool.dto.NotificationDtos.UnreadCountResponse;
import vn.edu.fpt.fschool.model.Notification;
import vn.edu.fpt.fschool.model.NotificationRead;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.NotificationReadRepository;
import vn.edu.fpt.fschool.repository.NotificationRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationReadRepository notificationReadRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public Page<NotificationResponse> getNotifications(
      String email, Pageable pageable, Boolean unreadOnly) {
    User user = getUserByEmail(email);
    Set<Long> readBroadcastIds =
        notificationReadRepository.findReadNotificationIdsByUserId(user.getUserId());

    Page<Notification> page =
        notificationRepository.findVisibleNotificationsForUser(user.getUserId(), pageable);

    return page.map(
        n -> {
          boolean isRead =
              (n.getUser() == null)
                  ? readBroadcastIds.contains(n.getNotificationId())
                  : Boolean.TRUE.equals(n.getIsRead());
          return NotificationResponse.from(n, isRead);
        });
  }

  @Transactional(readOnly = true)
  public List<NotificationResponse> getNotifications(String email) {
    User user = getUserByEmail(email);
    Set<Long> readBroadcastIds =
        notificationReadRepository.findReadNotificationIdsByUserId(user.getUserId());

    return notificationRepository.findVisibleNotificationsForUser(user.getUserId()).stream()
        .map(
            n -> {
              boolean isRead =
                  (n.getUser() == null)
                      ? readBroadcastIds.contains(n.getNotificationId())
                      : Boolean.TRUE.equals(n.getIsRead());
              return NotificationResponse.from(n, isRead);
            })
        .toList();
  }

  @Transactional(readOnly = true)
  public UnreadCountResponse getUnreadCount(String email) {
    User user = getUserByEmail(email);
    long personalUnread = notificationRepository.countByUserIdAndIsReadFalse(user.getUserId());
    long broadcastCount =
        notificationRepository.countUnreadBroadcastsForUser(user.getUserId());

    return new UnreadCountResponse(personalUnread + broadcastCount);
  }

  @Transactional
  public NotificationResponse markAsRead(Long notificationId, String email) {
    User user = getUserByEmail(email);
    Notification notification =
        notificationRepository
            .findVisibleByIdAndUserId(notificationId, user.getUserId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Không tìm thấy thông báo với ID: " + notificationId));

    if (notification.getUser() == null) {
      // Broadcast notification
      if (!notificationReadRepository.existsByNotification_NotificationIdAndUser_UserId(
          notificationId, user.getUserId())) {
        NotificationRead nr =
            NotificationRead.builder()
                .notification(notification)
                .user(user)
                .readAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
        notificationReadRepository.save(nr);
      }
      return NotificationResponse.from(notification, true);
    } else {
      // Personal notification
      notification.setIsRead(true);
      Notification saved = notificationRepository.save(notification);
      return NotificationResponse.from(saved, true);
    }
  }

  @Transactional
  public int markAllAsRead(String email) {
    User user = getUserByEmail(email);
    int personalCount = notificationRepository.markAllAsReadByUserId(user.getUserId());

    // Mark unread broadcasts as read via batch save
    List<Notification> unreadBroadcasts =
        notificationRepository.findUnreadBroadcastsForUser(user.getUserId());

    if (!unreadBroadcasts.isEmpty()) {
      OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
      List<NotificationRead> readsToSave =
          unreadBroadcasts.stream()
              .map(
                  n ->
                      NotificationRead.builder()
                          .notification(n)
                          .user(user)
                          .readAt(now)
                          .build())
              .toList();
      notificationReadRepository.saveAll(readsToSave);
    }

    return personalCount + unreadBroadcasts.size();
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
            .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
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
