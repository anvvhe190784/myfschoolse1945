package vn.edu.fpt.fschool.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
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

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private NotificationEventListener eventListener;

  private User student1;
  private User student2;

  @BeforeEach
  void setUp() {
    student1 = User.builder()
        .userId(1L)
        .email("student1@fpt.edu.vn")
        .fullName("Nguyen Van A")
        .role("STUDENT")
        .build();

    student2 = User.builder()
        .userId(2L)
        .email("student2@fpt.edu.vn")
        .fullName("Tran Thi B")
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
  @DisplayName("Event: NewsPublishedEvent kích hoạt batch insert thông báo NEWS cho tất cả học sinh")
  void onNewsPublished_shouldBroadcastToAllStudents() {
    when(userRepository.findByRole("STUDENT")).thenReturn(List.of(student1, student2));

    eventListener.onNewsPublished(new NewsPublishedEvent(42L, "Lịch thi mới", "Chi tiết ca thi đã có"));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
    verify(notificationRepository, times(1)).saveAll(captor.capture());

    List<Notification> savedList = captor.getValue();
    assertEquals(2, savedList.size());
    for (Notification n : savedList) {
      assertEquals("NEWS", n.getType());
      assertEquals(42L, n.getTargetId());
      assertFalse(n.getIsRead());
      assertTrue(n.getTitle().contains("Lịch thi mới"));
    }
  }
}
