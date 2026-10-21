package vn.edu.fpt.fschool.service;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.fpt.fschool.config.JwtUtils;
import vn.edu.fpt.fschool.dto.AuthDtos.ForgotPasswordRequest;
import vn.edu.fpt.fschool.dto.AuthDtos.ResetPasswordRequest;
import vn.edu.fpt.fschool.dto.AuthDtos.VerifyOtpRequest;
import vn.edu.fpt.fschool.dto.AuthDtos.VerifyOtpResponse;
import vn.edu.fpt.fschool.dto.NewsDtos.NewsResponse;
import vn.edu.fpt.fschool.dto.RequestDtos.UpdateRequestStatusDto;
import vn.edu.fpt.fschool.event.RequestProcessedEvent;
import vn.edu.fpt.fschool.mapper.AcademicMapper;
import vn.edu.fpt.fschool.mapper.UserMapper;
import vn.edu.fpt.fschool.model.News;
import vn.edu.fpt.fschool.model.RequestEntity;
import vn.edu.fpt.fschool.model.RequestStatus;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.ClassEnrollmentRepository;
import vn.edu.fpt.fschool.repository.ClassRepository;
import vn.edu.fpt.fschool.repository.CourseRepository;
import vn.edu.fpt.fschool.repository.NewsRepository;
import vn.edu.fpt.fschool.repository.RequestRepository;
import vn.edu.fpt.fschool.repository.ScheduleRepository;
import vn.edu.fpt.fschool.repository.UserRepository;
import vn.edu.fpt.fschool.util.PostCommitExecutor;

@ExtendWith(MockitoExtension.class)
class CoreAndSecurityAuditTest {

  // Mocks for AdminService
  @Mock private UserRepository userRepository;
  @Mock private CourseRepository courseRepository;
  @Mock private ClassRepository classRepository;
  @Mock private ClassEnrollmentRepository classEnrollmentRepository;
  @Mock private ScheduleRepository scheduleRepository;
  @Mock private RequestRepository requestRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private UserMapper userMapper;
  @Mock private AcademicMapper academicMapper;
  @Mock private ApplicationEventPublisher eventPublisher;

  // Mocks for NewsService & Scheduler
  @Mock private NewsRepository newsRepository;
  @Mock private StringRedisTemplate stringRedisTemplate;
  @Mock private HashOperations<String, Object, Object> hashOperations;
  @Mock private ValueOperations<String, String> valueOperations;

  // Mocks for AuthService
  @Mock private JwtUtils jwtUtils;
  @Mock private EmailService emailService;
  @Mock private TokenBlacklistService tokenBlacklistService;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private TokenVersionService tokenVersionService;

  private AdminService adminService;
  private NewsService newsService;
  private AuthService authService;

  @BeforeEach
  void setUp() {
    adminService =
        new AdminService(
            userRepository,
            courseRepository,
            classRepository,
            classEnrollmentRepository,
            scheduleRepository,
            requestRepository,
            passwordEncoder,
            userMapper,
            academicMapper,
            eventPublisher,
            tokenVersionService,
            refreshTokenService,
            new PostCommitExecutor());

    newsService =
        new NewsService(newsRepository, userRepository, eventPublisher, stringRedisTemplate);

    authService =
        new AuthService(
            userRepository,
            passwordEncoder,
            jwtUtils,
            emailService,
            tokenBlacklistService,
            refreshTokenService,
            tokenVersionService,
            stringRedisTemplate,
            eventPublisher,
            new PostCommitExecutor());
  }

  // --- LỖI #2: STATE MACHINE CHO DUYỆT ĐƠN TỪ ---
  @Test
  @DisplayName("Lỗi #2: RequestStatus State Machine - Cho phép chuyển PENDING -> APPROVED")
  void requestStatus_shouldAllowValidTransition() {
    assertTrue(RequestStatus.PENDING.canTransitionTo(RequestStatus.APPROVED));
    assertTrue(RequestStatus.PENDING.canTransitionTo(RequestStatus.PROCESSING));
    assertTrue(RequestStatus.PROCESSING.canTransitionTo(RequestStatus.APPROVED));
    assertTrue(RequestStatus.PROCESSING.canTransitionTo(RequestStatus.REJECTED));
  }

  @Test
  @DisplayName(
      "Lỗi #2: RequestStatus State Machine - Khóa trạng thái kết thúc (APPROVED không thể chuyển sang PENDING)")
  void requestStatus_shouldLockTerminalStates() {
    assertFalse(RequestStatus.APPROVED.canTransitionTo(RequestStatus.PENDING));
    assertFalse(RequestStatus.APPROVED.canTransitionTo(RequestStatus.REJECTED));
    assertFalse(RequestStatus.REJECTED.canTransitionTo(RequestStatus.APPROVED));
    assertFalse(RequestStatus.CANCELLED.canTransitionTo(RequestStatus.PROCESSING));
  }

  @Test
  @DisplayName(
      "Lỗi #2: processRequest từ chối khi chuyển từ trạng thái kết thúc APPROVED sang PENDING")
  void processRequest_shouldThrowException_onInvalidTransition() {
    User requester = User.builder().userId(10L).email("student@fpt.edu.vn").build();
    RequestEntity entity = new RequestEntity();
    entity.setRequestId(1L);
    entity.setStatus("APPROVED");
    entity.setRequester(requester);

    when(requestRepository.findById(1L)).thenReturn(Optional.of(entity));

    UpdateRequestStatusDto dto = new UpdateRequestStatusDto("PENDING", "Thử đảo ngược trạng thái");
    assertThrows(
        IllegalStateException.class,
        () -> adminService.processRequest(1L, "admin@fschool.edu.vn", dto));
  }

  @Test
  @DisplayName(
      "Lỗi #2: processRequest thành công và phát sự kiện RequestProcessedEvent sau khi commit")
  void processRequest_shouldSucceedAndPublishEvent() {
    User requester = User.builder().userId(10L).email("student@fpt.edu.vn").build();
    User admin = User.builder().userId(99L).email("admin@fschool.edu.vn").role("ADMIN").build();

    RequestEntity entity = new RequestEntity();
    entity.setRequestId(1L);
    entity.setTitle("Xin nghỉ học");
    entity.setStatus("PENDING");
    entity.setRequester(requester);

    when(requestRepository.findById(1L)).thenReturn(Optional.of(entity));
    when(userRepository.findByEmailIgnoreCase("admin@fschool.edu.vn"))
        .thenReturn(Optional.of(admin));
    when(requestRepository.save(any(RequestEntity.class))).thenAnswer(i -> i.getArgument(0));

    UpdateRequestStatusDto dto = new UpdateRequestStatusDto("APPROVED", "Đã phê duyệt đơn");
    adminService.processRequest(1L, "admin@fschool.edu.vn", dto);

    assertEquals("APPROVED", entity.getStatus());
    ArgumentCaptor<RequestProcessedEvent> captor =
        ArgumentCaptor.forClass(RequestProcessedEvent.class);
    verify(eventPublisher, times(1)).publishEvent(captor.capture());

    RequestProcessedEvent event = captor.getValue();
    assertEquals(1L, event.requestId());
    assertEquals(10L, event.requesterId());
    assertEquals("PENDING", event.oldStatus());
    assertEquals("APPROVED", event.newStatus());
  }

  // --- LỖI #4: PHÂN TRANG DB & GIỚI HẠN SIZE ---
  @Test
  @DisplayName(
      "Lỗi #4: getAllPublishedNews giới hạn tối đa 50 bản ghi và cắt bằng Spring Data Pageable")
  void getAllPublishedNews_shouldCapAt50AndUsePageable() {
    News sampleNews = new News();
    sampleNews.setNewsId(1L);
    sampleNews.setTitle("Tin tức");
    sampleNews.setViewsCount(10);
    sampleNews.setIsPublished(true);

    Page<News> page = new PageImpl<>(List.of(sampleNews));
    when(newsRepository.findByIsPublishedTrueOrderByPublishedAtDesc(any(Pageable.class)))
        .thenReturn(page);

    List<NewsResponse> result = newsService.getAllPublishedNews(1000); // Client yêu cầu 1000

    assertNotNull(result);
    assertEquals(1, result.size());
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(newsRepository).findByIsPublishedTrueOrderByPublishedAtDesc(pageableCaptor.capture());
    assertEquals(50, pageableCaptor.getValue().getPageSize()); // Bị trần ở 50
  }

  // --- LỖI #5: BỘ ĐẾM LƯỢT XEM KHÔNG GIỮ KHÓA DÒNG DB ---
  @Test
  @DisplayName("Lỗi #5: getNewsDetail tăng views trên Redis và không thực hiện UPDATE DB đồng bộ")
  void getNewsDetail_shouldIncrementOnRedis_withoutDbWrite() {
    News sampleNews = new News();
    sampleNews.setNewsId(100L);
    sampleNews.setTitle("Bài viết hot");
    sampleNews.setViewsCount(50);

    when(newsRepository.findById(100L)).thenReturn(Optional.of(sampleNews));
    when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
    when(hashOperations.increment(NewsService.NEWS_VIEWS_KEY, "100", 1L)).thenReturn(5L);

    NewsResponse resp = newsService.getNewsDetail(100L);

    assertNotNull(resp);
    assertEquals(55, resp.viewsCount()); // 50 DB + 5 Redis pending
    verify(newsRepository, never()).save(any()); // KHÔNG lock dòng DB
  }

  // --- LỖI #7: CHỐNG DÒ QUÉT EMAIL & RATE LIMIT OTP ---
  @Test
  @DisplayName(
      "Lỗi #7: forgotPassword trả thông điệp trung lập khi email KHÔNG tồn tại (Chống Enumeration)")
  void forgotPassword_shouldReturnNeutralMessage_whenEmailNotFound() {
    when(userRepository.findByEmailIgnoreCase("ghost@fpt.edu.vn")).thenReturn(Optional.empty());

    var resp = authService.forgotPassword(new ForgotPasswordRequest("ghost@fpt.edu.vn"));

    assertTrue(resp.success());
    assertTrue(resp.message().contains("Nếu email tồn tại trong hệ thống"));
    verify(emailService, never()).sendOtpEmail(any(), any());
  }

  @Test
  @DisplayName("Lỗi #7: verifyOtp khóa và hủy OTP sau 5 lần nhập sai liên tiếp")
  void verifyOtp_shouldLock_after5FailedAttempts() {
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("otp:reset:user@fpt.edu.vn")).thenReturn("123456");
    when(valueOperations.increment("otp:attempts:user@fpt.edu.vn")).thenReturn(6L); // Lần thử thứ 6

    VerifyOtpRequest req = new VerifyOtpRequest("user@fpt.edu.vn", "999999");

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> authService.verifyOtp(req));
    assertTrue(ex.getMessage().contains("quá 5 lần"));
    verify(stringRedisTemplate).delete("otp:reset:user@fpt.edu.vn");
  }

  // --- LỖI #9: ONE-TIME RESET TOKEN ---
  @Test
  @DisplayName("Lỗi #9: verifyOtp thành công cấp One-Time Reset Token (UUID)")
  void verifyOtp_shouldReturnResetToken_onSuccess() {
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("otp:reset:user@fpt.edu.vn")).thenReturn("123456");
    when(valueOperations.increment("otp:attempts:user@fpt.edu.vn")).thenReturn(1L);

    VerifyOtpResponse resp =
        authService.verifyOtp(new VerifyOtpRequest("user@fpt.edu.vn", "123456"));

    assertTrue(resp.success());
    assertNotNull(resp.resetToken());
    verify(stringRedisTemplate).delete("otp:reset:user@fpt.edu.vn");
    ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
    verify(valueOperations)
        .set(tokenCaptor.capture(), eq("user@fpt.edu.vn"), any(java.time.Duration.class));
    assertTrue(tokenCaptor.getValue().startsWith("otp:token:"));
  }

  @Test
  @DisplayName("Lỗi #9: resetPassword tiêu thụ One-Time Reset Token nguyên tử (getAndDelete)")
  void resetPassword_shouldConsumeTokenAtomically() {
    User user = User.builder().userId(1L).email("user@fpt.edu.vn").passwordHash("oldHash").build();

    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.getAndDelete("otp:token:valid-uuid-token")).thenReturn("user@fpt.edu.vn");
    when(userRepository.findByEmailIgnoreCase("user@fpt.edu.vn")).thenReturn(Optional.of(user));
    when(passwordEncoder.encode("NewSecret123!")).thenReturn("newHashedSecret");

    ResetPasswordRequest req = new ResetPasswordRequest("valid-uuid-token", "NewSecret123!");
    var resp = authService.resetPassword(req);

    assertTrue(resp.success());
    verify(userRepository).save(user);
    verify(tokenVersionService).updateUserTokenVersion(eq("user@fpt.edu.vn"), anyInt());
  }
}
