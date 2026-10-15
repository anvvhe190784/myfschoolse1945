package vn.edu.fpt.fschool.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.fpt.fschool.event.NotificationEvents.NewsPublishedEvent;
import vn.edu.fpt.fschool.event.NotificationEvents.PasswordChangedEvent;
import vn.edu.fpt.fschool.model.Notification;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.NotificationRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

  @Mock private NotificationRepository notificationRepository;

  @Mock private UserRepository userRepository;

  @InjectMocks private NotificationEventListener eventListener;

  private User student1;

  @BeforeEach
  void setUp() {
    student1 =
        User.builder()
            .userId(1L)
            .email("student1@fpt.edu.vn")
            .fullName("Nguyen Van A")
            .role("STUDENT")
            .build();
  }

  @Test
  @DisplayName("Event: PasswordChangedEvent kích hoạt tạo thông báo bảo mật cho user")
  void onPasswordChanged_shouldCreateSecurityNotification() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(student1));

    eventListener.onPasswordChanged(new PasswordChangedEvent(1L, "student1@fpt.edu.vn"));

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository, times(1)).save(captor.capture());

    Notification saved = captor.getValue();
    assertNotNull(saved);
    assertEquals("SECURITY", saved.getType());
    assertEquals(student1, saved.getUser());
    assertFalse(saved.getIsRead());
    assertTrue(saved.getTitle().contains("Bảo mật"));
  }

  @Test
  @DisplayName(
      "Event: NewsPublishedEvent tạo đúng 1 bản ghi thông báo Broadcast toàn trường (user = null)")
  void onNewsPublished_shouldCreateSingleBroadcastNotification() {
    eventListener.onNewsPublished(
        new NewsPublishedEvent(42L, "Lịch thi mới", "Chi tiết ca thi đã có"));

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository, times(1)).save(captor.capture());

    Notification saved = captor.getValue();
    assertNotNull(saved);
    assertNull(saved.getUser()); // Broadcast notification: user is null
    assertEquals("NEWS", saved.getType());
    assertEquals(42L, saved.getTargetId());
    assertFalse(saved.getIsRead());
    assertTrue(saved.getTitle().contains("Lịch thi mới"));
  }

  @Test
  @DisplayName(
      "Lỗi #2: RequestProcessedEvent tạo thông báo trực tiếp cho requester sau khi đơn được duyệt")
  void onRequestProcessed_shouldCreateNotificationForRequester() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(student1));

    eventListener.onRequestProcessed(
        new RequestProcessedEvent(
            100L, 1L, "Đơn xin nghỉ ốm", "PENDING", "APPROVED", "Đã duyệt đơn"));

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository, times(1)).save(captor.capture());

    Notification saved = captor.getValue();
    assertNotNull(saved);
    assertEquals(student1, saved.getUser());
    assertEquals("REQUEST", saved.getType());
    assertEquals(100L, saved.getTargetId());
    assertTrue(saved.getContent().contains("ĐÃ ĐƯỢC PHÊ DUYỆT"));
    assertTrue(saved.getContent().contains("Đã duyệt đơn"));
  }
}
