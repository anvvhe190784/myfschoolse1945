package vn.edu.fpt.fschool.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.fpt.fschool.config.JwtUtils;
import vn.edu.fpt.fschool.dto.AdminDtos.UpdateUserStatusRequest;
import vn.edu.fpt.fschool.dto.ScheduleDtos.ScheduleResponse;
import vn.edu.fpt.fschool.mapper.AcademicMapper;
import vn.edu.fpt.fschool.mapper.UserMapper;
import vn.edu.fpt.fschool.model.ClassEntity;
import vn.edu.fpt.fschool.model.Schedule;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.ClassEnrollmentRepository;
import vn.edu.fpt.fschool.repository.ClassRepository;
import vn.edu.fpt.fschool.repository.CourseRepository;
import vn.edu.fpt.fschool.repository.ParentStudentRepository;
import vn.edu.fpt.fschool.repository.RequestRepository;
import vn.edu.fpt.fschool.repository.ScheduleRepository;
import vn.edu.fpt.fschool.repository.UserRepository;
import vn.edu.fpt.fschool.util.PostCommitExecutor;

@ExtendWith(MockitoExtension.class)
class Phase1And2SecurityTest {

  @Mock private StringRedisTemplate stringRedisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;
  @Mock private UserRepository userRepository;
  @Mock private CourseRepository courseRepository;
  @Mock private ClassRepository classRepository;
  @Mock private ClassEnrollmentRepository classEnrollmentRepository;
  @Mock private ScheduleRepository scheduleRepository;
  @Mock private RequestRepository requestRepository;
  @Mock private ParentStudentRepository parentStudentRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private UserMapper userMapper;
  @Mock private AcademicMapper academicMapper;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private TokenVersionService tokenVersionService;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private JwtUtils jwtUtils;

  // --- 1. AuthRateLimiter Tests ---
  @Test
  @DisplayName("AuthRateLimiter: Cho phép request khi chưa vượt ngưỡng và thiết lập TTL lần đầu")
  void authRateLimiter_shouldAllow_whenUnderLimit() {
    AuthRateLimiter rateLimiter = new AuthRateLimiter(stringRedisTemplate);
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.increment("rate:test:key")).thenReturn(1L);

    Duration window = Duration.ofMinutes(1);
    assertDoesNotThrow(() -> rateLimiter.checkRateLimit("rate:test:key", 5, window));
    verify(stringRedisTemplate, times(1)).expire("rate:test:key", window);
  }

  @Test
  @DisplayName("AuthRateLimiter: Chặn và ném RequestNotPermitted khi vượt quá ngưỡng cấu hình")
  void authRateLimiter_shouldThrow_whenExceedsLimit() {
    AuthRateLimiter rateLimiter = new AuthRateLimiter(stringRedisTemplate);
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.increment("rate:test:key")).thenReturn(6L);

    Duration window = Duration.ofMinutes(1);
    assertThrows(
        RequestNotPermitted.class,
        () -> rateLimiter.checkRateLimit("rate:test:key", 5, window));
  }

  // --- 2. TokenBlacklistService Fail-Closed Tests ---
  @Test
  @DisplayName("TokenBlacklistService: Fail-closed trả về true khi Redis template null")
  void tokenBlacklistService_shouldReturnTrue_whenRedisNull() {
    TokenBlacklistService service = new TokenBlacklistService(null, jwtUtils);
    boolean blacklisted = service.isBlacklisted("Bearer some.sample.token");
    assertTrue(blacklisted, "Phải fail-closed (từ chối token) khi Redis null");
  }

  @Test
  @DisplayName("TokenBlacklistService: Fail-closed trả về true khi Redis ném ngoại lệ")
  void tokenBlacklistService_shouldReturnTrue_whenRedisThrows() {
    when(stringRedisTemplate.hasKey("jwt_blacklist:sample.token"))
        .thenThrow(new RuntimeException("Redis connection refused"));

    TokenBlacklistService service = new TokenBlacklistService(stringRedisTemplate, jwtUtils);
    boolean blacklisted = service.isBlacklisted("Bearer sample.token");
    assertTrue(blacklisted, "Phải fail-closed (từ chối token) khi Redis connection gặp lỗi");
  }

  // --- 3. AdminService updateUserStatus (Revocation on Lock/Inactive) ---
  @Test
  @DisplayName("AdminService: Khi khóa tài khoản (LOCKED), tăng tokenVersion và thu hồi toàn bộ token")
  void updateUserStatus_shouldRevokeTokens_whenStatusBecomesLocked() {
    AdminService adminService =
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

    User user =
        User.builder()
            .userId(100L)
            .email("target@fpt.edu.vn")
            .status("ACTIVE")
            .tokenVersion(1)
            .build();

    when(userRepository.findById(100L)).thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);

    adminService.updateUserStatus(100L, new UpdateUserStatusRequest("LOCKED"));

    assertEquals("LOCKED", user.getStatus());
    assertEquals(2, user.getTokenVersion());
    verify(tokenVersionService, times(1)).updateUserTokenVersion("target@fpt.edu.vn", 2);
    verify(refreshTokenService, times(1)).revokeAllUserTokens("target@fpt.edu.vn");
  }

  // --- 4. StudentAccessResolver (Lecturer BOLA Protection) ---
  @Test
  @DisplayName("StudentAccessResolver: Giảng viên tra cứu sinh viên thuộc lớp phụ trách thành công")
  void resolveTargetStudentId_lecturer_shouldAllow_whenStudentEnrolled() {
    StudentAccessResolver resolver =
        new StudentAccessResolver(parentStudentRepository, classEnrollmentRepository);

    User lecturer =
        User.builder().userId(20L).email("teacher@fpt.edu.vn").role("LECTURER").build();

    when(classEnrollmentRepository.existsByStudent_UserIdAndClassEntity_Lecturer_UserIdAndStatus(
            50L, 20L, "ENROLLED"))
        .thenReturn(true);

    Long resolvedId = resolver.resolveTargetStudentId(lecturer, 50L);
    assertEquals(50L, resolvedId);
  }

  @Test
  @DisplayName("StudentAccessResolver: Giảng viên tra cứu sinh viên KHÔNG thuộc lớp bị từ chối")
  void resolveTargetStudentId_lecturer_shouldThrow_whenStudentNotEnrolled() {
    StudentAccessResolver resolver =
        new StudentAccessResolver(parentStudentRepository, classEnrollmentRepository);

    User lecturer =
        User.builder().userId(20L).email("teacher@fpt.edu.vn").role("LECTURER").build();

    when(classEnrollmentRepository.existsByStudent_UserIdAndClassEntity_Lecturer_UserIdAndStatus(
            99L, 20L, "ENROLLED"))
        .thenReturn(false);

    assertThrows(
        AccessDeniedException.class, () -> resolver.resolveTargetStudentId(lecturer, 99L));
  }

  // --- 5. ScheduleService findScheduleDetail (Parent Access Check) ---
  @Test
  @DisplayName("ScheduleService: Phụ huynh xem chi tiết buổi học khi con em theo học lớp")
  void findScheduleDetail_parent_shouldAllow_whenChildEnrolled() {
    ScheduleService scheduleService =
        new ScheduleService(
            scheduleRepository,
            userRepository,
            mock(StudentAccessResolver.class),
            classEnrollmentRepository,
            academicMapper);

    ClassEntity classEntity = new ClassEntity();
    classEntity.setClassId(15L);

    Schedule schedule = new Schedule();
    schedule.setScheduleId(1L);
    schedule.setClassEntity(classEntity);

    User parentUser =
        User.builder().userId(30L).email("parent@fpt.edu.vn").role("PARENT").build();

    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
    when(userRepository.findByEmailIgnoreCase("parent@fpt.edu.vn"))
        .thenReturn(Optional.of(parentUser));
    when(classEnrollmentRepository.existsChildEnrolled(15L, 30L)).thenReturn(true);

    ScheduleResponse expectedResponse = mock(ScheduleResponse.class);
    when(academicMapper.toScheduleResponse(schedule)).thenReturn(expectedResponse);

    ScheduleResponse result = scheduleService.getScheduleDetail(1L, "parent@fpt.edu.vn");
    assertNotNull(result);
    assertEquals(expectedResponse, result);
  }

  @Test
  @DisplayName("ScheduleService: Phụ huynh bị từ chối xem buổi học khi con em KHÔNG theo học")
  void findScheduleDetail_parent_shouldThrow_whenChildNotEnrolled() {
    ScheduleService scheduleService =
        new ScheduleService(
            scheduleRepository,
            userRepository,
            mock(StudentAccessResolver.class),
            classEnrollmentRepository,
            academicMapper);

    ClassEntity classEntity = new ClassEntity();
    classEntity.setClassId(15L);

    Schedule schedule = new Schedule();
    schedule.setScheduleId(1L);
    schedule.setClassEntity(classEntity);

    User parentUser =
        User.builder().userId(30L).email("parent@fpt.edu.vn").role("PARENT").build();

    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
    when(userRepository.findByEmailIgnoreCase("parent@fpt.edu.vn"))
        .thenReturn(Optional.of(parentUser));
    when(classEnrollmentRepository.existsChildEnrolled(15L, 30L)).thenReturn(false);

    assertThrows(
        AccessDeniedException.class,
        () -> scheduleService.getScheduleDetail(1L, "parent@fpt.edu.vn"));
  }
}
