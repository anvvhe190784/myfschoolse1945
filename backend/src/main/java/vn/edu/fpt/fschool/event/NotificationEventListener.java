package vn.edu.fpt.fschool.event;

import java.time.OffsetDateTime;
import java.util.List;
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
import vn.edu.fpt.fschool.model.User;
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
                      .createdAt(OffsetDateTime.now())
                      .build();

              notificationRepository.save(notification);
              log.info("[EVENT] Đã tạo thông báo bảo mật cho user: {}", user.getEmail());
            });
  }

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void onNewsPublished(NewsPublishedEvent event) {
    log.info("[EVENT] Xử lý sự kiện đăng tin mới: {} (ID: {})", event.title(), event.newsId());
    List<User> students = userRepository.findByRole("STUDENT");
    if (students.isEmpty()) {
      log.warn("[EVENT] Không tìm thấy học sinh nào để gửi thông báo tin tức");
      return;
    }

    String summary =
        (event.summary() != null && !event.summary().isBlank())
            ? event.summary()
            : "Nhà trường vừa cập nhật thông báo tin tức mới, sinh viên vui lòng vào xem chi tiết.";

    List<Notification> notifications =
        students.stream()
            .map(
                student ->
                    Notification.builder()
                        .user(student)
                        .title("Tin tức mới: " + event.title())
                        .content(summary)
                        .type("NEWS")
                        .targetId(event.newsId())
                        .isRead(false)
                        .createdAt(OffsetDateTime.now())
                        .build())
            .toList();

    notificationRepository.saveAll(notifications);
    log.info("[EVENT] Đã gửi thông báo tin tức đến {} học sinh", notifications.size());
  }
}
