package vn.edu.fpt.fschool.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import vn.edu.fpt.fschool.dto.NotificationDtos.NotificationResponse;
import vn.edu.fpt.fschool.dto.NotificationDtos.UnreadCountResponse;
import vn.edu.fpt.fschool.model.Notification;
import vn.edu.fpt.fschool.model.NotificationRead;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.NotificationReadRepository;
import vn.edu.fpt.fschool.repository.NotificationRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private NotificationRepository notificationRepository;

  @Mock private NotificationReadRepository notificationReadRepository;

  @Mock private UserRepository userRepository;

  @InjectMocks private NotificationService notificationService;

  private User testUser;
  private Notification testNotification;
  private Notification broadcastNotification;

  @BeforeEach
  void setUp() {
    testUser =
        User.builder()
            .userId(1L)
            .email("student@fpt.edu.vn")
            .fullName("Nguyen Van A")
            .role("STUDENT")
            .build();

    testNotification =
        Notification.builder()
            .notificationId(100L)
            .user(testUser)
            .title("Lịch thi mới")
            .content("Đã có lịch thi PRM393")
            .type("NEWS")
            .targetId(1L)
            .isRead(false)
            .createdAt(OffsetDateTime.now())
            .build();

    broadcastNotification =
        Notification.builder()
            .notificationId(101L)
            .user(null) // Broadcast toàn trường
            .title("Thông báo nghỉ lễ")
            .content("Toàn trường nghỉ lễ")
            .type("NEWS")
            .targetId(2L)
            .isRead(false)
            .createdAt(OffsetDateTime.now())
            .build();
  }

  @Test
  @DisplayName("Lấy danh sách thông báo của user thành công bao gồm cả broadcast")
  void getNotifications_shouldReturnMappedList() {
    when(userRepository.findByEmailIgnoreCase("student@fpt.edu.vn"))
        .thenReturn(Optional.of(testUser));
    when(notificationReadRepository.findReadNotificationIdsByUserId(1L))
        .thenReturn(Collections.emptySet());
    when(notificationRepository.findVisibleNotificationsForUser(1L))
        .thenReturn(List.of(testNotification, broadcastNotification));

    List<NotificationResponse> result = notificationService.getNotifications("student@fpt.edu.vn");

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Lịch thi mới", result.get(0).title());
    assertEquals("Thông báo nghỉ lễ", result.get(1).title());
    assertFalse(result.get(0).isRead());
    assertFalse(result.get(1).isRead());
  }

  @Test
  @DisplayName("Lấy danh sách thông báo phân trang thành công")
  void getNotifications_withPaging_shouldReturnPage() {
    when(userRepository.findByEmailIgnoreCase("student@fpt.edu.vn"))
        .thenReturn(Optional.of(testUser));
    when(notificationReadRepository.findReadNotificationIdsByUserId(1L))
        .thenReturn(Collections.emptySet());
    Pageable pageable = PageRequest.of(0, 10);
    Page<Notification> mockPage = new PageImpl<>(List.of(testNotification), pageable, 1);
    when(notificationRepository.findVisibleNotificationsForUser(1L, pageable)).thenReturn(mockPage);

    Page<NotificationResponse> result =
        notificationService.getNotifications("student@fpt.edu.vn", pageable, false);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals("Lịch thi mới", result.getContent().get(0).title());
  }

  @Test
  @DisplayName("Đếm số lượng thông báo chưa đọc bao gồm cả broadcast chưa đọc")
  void getUnreadCount_shouldReturnCorrectCount() {
    when(userRepository.findByEmailIgnoreCase("student@fpt.edu.vn"))
        .thenReturn(Optional.of(testUser));
    when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(2L);
    when(notificationReadRepository.findReadNotificationIdsByUserId(1L))
        .thenReturn(Collections.emptySet());
    when(notificationRepository.findVisibleNotificationsForUser(1L))
        .thenReturn(List.of(broadcastNotification));

    UnreadCountResponse result = notificationService.getUnreadCount("student@fpt.edu.vn");

    assertNotNull(result);
    assertEquals(3L, result.unreadCount()); // 2 personal + 1 unread broadcast
  }

  @Test
  @DisplayName("Đánh dấu đã đọc một thông báo cá nhân")
  void markAsRead_personalNotification_shouldUpdateStatus() {
    when(userRepository.findByEmailIgnoreCase("student@fpt.edu.vn"))
        .thenReturn(Optional.of(testUser));
    when(notificationRepository.findVisibleByIdAndUserId(100L, 1L))
        .thenReturn(Optional.of(testNotification));
    when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

    NotificationResponse result = notificationService.markAsRead(100L, "student@fpt.edu.vn");

    assertNotNull(result);
    assertTrue(result.isRead());
  }

  @Test
  @DisplayName("Đánh dấu đã đọc một thông báo broadcast tạo bản ghi NotificationRead")
  void markAsRead_broadcastNotification_shouldSaveNotificationRead() {
    when(userRepository.findByEmailIgnoreCase("student@fpt.edu.vn"))
        .thenReturn(Optional.of(testUser));
    when(notificationRepository.findVisibleByIdAndUserId(101L, 1L))
        .thenReturn(Optional.of(broadcastNotification));
    when(notificationReadRepository.existsByNotification_NotificationIdAndUser_UserId(101L, 1L))
        .thenReturn(false);

    NotificationResponse result = notificationService.markAsRead(101L, "student@fpt.edu.vn");

    assertNotNull(result);
    assertTrue(result.isRead());
    verify(notificationReadRepository, times(1)).save(any(NotificationRead.class));
  }

  @Test
  @DisplayName("Tạo thông báo mới lưu thành công")
  void createNotification_shouldSaveAndReturnResponse() {
    when(notificationRepository.save(any(Notification.class)))
        .thenAnswer(
            i -> {
              Notification n = i.getArgument(0);
              n.setNotificationId(200L);
              return n;
            });

    NotificationResponse result =
        notificationService.createNotification(
            testUser, "Bảo mật", "Đổi mật khẩu thành công", "SECURITY", null);

    assertNotNull(result);
    assertEquals("Bảo mật", result.title());
    assertEquals("SECURITY", result.type());
    assertFalse(result.isRead());
  }
}
