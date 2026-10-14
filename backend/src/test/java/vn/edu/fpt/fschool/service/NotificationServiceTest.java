package vn.edu.fpt.fschool.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.fpt.fschool.dto.NotificationDtos.NotificationResponse;
import vn.edu.fpt.fschool.dto.NotificationDtos.UnreadCountResponse;
import vn.edu.fpt.fschool.model.Notification;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.NotificationRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private NotificationService notificationService;

  private User testUser;
  private Notification testNotification;

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .userId(1L)
        .email("student@fpt.edu.vn")
        .fullName("Nguyen Van A")
        .role("STUDENT")
        .build();

    testNotification = Notification.builder()
        .notificationId(100L)
        .user(testUser)
        .title("Lịch thi mới")
        .content("Đã có lịch thi PRM393")
        .type("NEWS")
        .targetId(1L)
        .isRead(false)
        .createdAt(OffsetDateTime.now())
        .build();
  }

  @Test
  @DisplayName("Lấy danh sách thông báo của user thành công")
  void getNotifications_shouldReturnMappedList() {
    when(userRepository.findByEmailIgnoreCase("student@fpt.edu.vn")).thenReturn(Optional.of(testUser));
    when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(testNotification));

    List<NotificationResponse> result = notificationService.getNotifications("student@fpt.edu.vn");

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Lịch thi mới", result.get(0).title());
    assertEquals("NEWS", result.get(0).type());
    assertFalse(result.get(0).isRead());
    verify(notificationRepository, times(1)).findByUserIdOrderByCreatedAtDesc(1L);
  }

  @Test
  @DisplayName("Đếm số lượng thông báo chưa đọc")
  void getUnreadCount_shouldReturnCorrectCount() {
    when(userRepository.findByEmailIgnoreCase("student@fpt.edu.vn")).thenReturn(Optional.of(testUser));
    when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(3L);

    UnreadCountResponse result = notificationService.getUnreadCount("student@fpt.edu.vn");

    assertNotNull(result);
    assertEquals(3L, result.unreadCount());
  }

  @Test
  @DisplayName("Đánh dấu đã đọc một thông báo cụ thể")
  void markAsRead_shouldUpdateStatus() {
    when(userRepository.findByEmailIgnoreCase("student@fpt.edu.vn")).thenReturn(Optional.of(testUser));
    when(notificationRepository.findByNotificationIdAndUser_UserId(100L, 1L)).thenReturn(Optional.of(testNotification));
    when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

    NotificationResponse result = notificationService.markAsRead(100L, "student@fpt.edu.vn");

    assertNotNull(result);
    assertTrue(result.isRead());
  }

  @Test
  @DisplayName("Đánh dấu đã đọc tất cả thông báo qua bulk query")
  void markAllAsRead_shouldInvokeRepositoryBulkUpdate() {
    when(userRepository.findByEmailIgnoreCase("student@fpt.edu.vn")).thenReturn(Optional.of(testUser));
    when(notificationRepository.markAllAsReadByUserId(1L)).thenReturn(5);

    int count = notificationService.markAllAsRead("student@fpt.edu.vn");

    assertEquals(5, count);
    verify(notificationRepository, times(1)).markAllAsReadByUserId(1L);
  }

  @Test
  @DisplayName("Tạo thông báo mới lưu thành công")
  void createNotification_shouldSaveAndReturnResponse() {
    when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
      Notification n = i.getArgument(0);
      n.setNotificationId(200L);
      return n;
    });

    NotificationResponse result = notificationService.createNotification(
        testUser,
        "Bảo mật",
        "Đổi mật khẩu thành công",
        "SECURITY",
        null
    );

    assertNotNull(result);
    assertEquals("Bảo mật", result.title());
    assertEquals("SECURITY", result.type());
    assertFalse(result.isRead());
  }
}
