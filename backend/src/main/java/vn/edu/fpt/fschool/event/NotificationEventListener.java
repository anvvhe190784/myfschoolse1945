package vn.edu.fpt.fschool.event;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.edu.fpt.fschool.event.NotificationEvents.NewsPublishedEvent;
import vn.edu.fpt.fschool.event.NotificationEvents.PasswordChangedEvent;
import vn.edu.fpt.fschool.model.Notification;
import vn.edu.fpt.fschool.repository.NotificationRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void onPasswordChanged(PasswordChangedEvent event) {
    log.info("[EVENT] Xử lý sự kiện đổi mật khẩu cho userId: {}", event.userId());
    userRepository
        .findById(event.userId())
        .ifPresent(
            user -> {
              Notification notification =
                  Notification.builder()
                      .user(user)
                      .title("Bảo mật: Mật khẩu đã được cập nhật")
                      .content("Mật khẩu tài khoản của bạn đã được thay đổi thành công.")
                      .type("SECURITY")
                      .targetId(null)
                      .isRead(false)
                      .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                      .build();

              notificationRepository.save(notification);
              log.info("[EVENT] Đã tạo thông báo bảo mật cho user: {}", user.getEmail());
            });
  }

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void onNewsPublished(NewsPublishedEvent event) {
    log.info(
        "[EVENT] Xử lý sự kiện đăng tin mới broadcast: {} (ID: {})", event.title(), event.newsId());

    String summary =
        (event.summary() != null && !event.summary().isBlank())
            ? event.summary()
            : "Nhà trường vừa cập nhật thông báo tin tức mới, sinh viên vui lòng vào xem chi tiết.";

    // Tạo đúng 1 bản ghi thông báo Broadcast toàn trường (user = null)
    // Loại bỏ hoàn toàn vòng lặp fan-out write 25.000 dòng gây tràn RAM và sập DB
    Notification broadcastNotification =
        Notification.builder()
            .user(null) // Broadcast toàn trường
            .title("Tin tức mới: " + event.title())
            .content(summary)
            .type("NEWS")
            .targetId(event.newsId())
            .isRead(false)
            .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
            .build();

    notificationRepository.save(broadcastNotification);
    log.info(
        "[EVENT] Đã tạo thông báo broadcast toàn trường thành công (ID: {})",
        broadcastNotification.getNotificationId());
  }

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void onRequestProcessed(RequestProcessedEvent event) {
    log.info(
        "[EVENT] Xử lý sự kiện xử lý đơn từ ID: {} cho requesterId: {}",
        event.requestId(),
        event.requesterId());
    userRepository
        .findById(event.requesterId())
        .ifPresent(
            requester -> {
              String statusDesc =
                  switch (event.newStatus()) {
                    case "APPROVED" -> "ĐÃ ĐƯỢC PHÊ DUYỆT";
                    case "REJECTED" -> "BỊ TỪ CHỐI";
                    case "PROCESSING" -> "ĐANG ĐƯỢC XỬ LÝ";
                    case "CANCELLED" -> "ĐÃ HỦY";
                    default -> event.newStatus();
                  };

              String noteContent =
                  (event.responseNote() != null && !event.responseNote().isBlank())
                      ? "\nGhi chú: " + event.responseNote()
                      : "";

              Notification notification =
                  Notification.builder()
                      .user(requester)
                      .title("Đơn từ: " + event.title())
                      .content(
                          "Đơn từ của bạn đã chuyển sang trạng thái: "
                              + statusDesc
                              + "."
                              + noteContent)
                      .type("REQUEST")
                      .targetId(event.requestId())
                      .isRead(false)
                      .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                      .build();

              notificationRepository.save(notification);
              log.info("[EVENT] Đã tạo thông báo đơn từ cho requester: {}", requester.getEmail());
            });
  }
}
