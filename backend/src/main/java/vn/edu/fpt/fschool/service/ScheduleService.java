package vn.edu.fpt.fschool.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.fschool.dto.ScheduleDtos.ScheduleResponse;
import vn.edu.fpt.fschool.mapper.AcademicMapper;
import vn.edu.fpt.fschool.model.Schedule;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.ClassEnrollmentRepository;
import vn.edu.fpt.fschool.repository.ScheduleRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ScheduleService {

  private static final String ROLE_STUDENT = "STUDENT";
  private static final String ROLE_LECTURER = "LECTURER";
  private static final String ROLE_PARENT = "PARENT";
  private static final String ROLE_ADMIN = "ADMIN";
  private static final String USER_NOT_FOUND = "Không tìm thấy người dùng";
  private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  private final ScheduleRepository scheduleRepository;
  private final UserRepository userRepository;
  private final StudentAccessResolver studentAccessResolver;
  private final ClassEnrollmentRepository classEnrollmentRepository;
  private final AcademicMapper academicMapper;

  @Transactional(readOnly = true)
  public List<ScheduleResponse> getSchedules(
      String email, LocalDate startDate, LocalDate endDate, Long requestedStudentId) {
    if (startDate != null && endDate != null) {
      LocalDate start = startDate.isAfter(endDate) ? endDate : startDate;
      LocalDate end = startDate.isAfter(endDate) ? startDate : endDate;

      User currentUser =
          userRepository
              .findByEmailIgnoreCase(email)
              .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

      String role =
          currentUser.getRole() != null ? currentUser.getRole().toUpperCase() : ROLE_STUDENT;
      List<Schedule> schedules =
          findSchedulesForRoleInRange(role, currentUser, start, end, requestedStudentId);

      return academicMapper.toScheduleResponseList(schedules);
    }

    LocalDate targetDate = resolveTargetDate(startDate, endDate);
    return findSchedulesByEmailAndDate(email, targetDate, requestedStudentId);
  }

  @Transactional(readOnly = true)
  public List<ScheduleResponse> getSchedules(
      String email, LocalDate date, Long requestedStudentId) {
    return findSchedulesByEmailAndDate(email, date, requestedStudentId);
  }

  private List<ScheduleResponse> findSchedulesByEmailAndDate(
      String email, LocalDate date, Long requestedStudentId) {
    LocalDate targetDate = (date != null) ? date : LocalDate.now(DEFAULT_ZONE);

    User currentUser =
        userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

    String role =
        currentUser.getRole() != null ? currentUser.getRole().toUpperCase() : ROLE_STUDENT;
    List<Schedule> schedules =
        findSchedulesForRoleOnDate(role, currentUser, targetDate, requestedStudentId);

    return academicMapper.toScheduleResponseList(schedules);
  }

  private List<Schedule> findSchedulesForRoleInRange(
      String role, User currentUser, LocalDate start, LocalDate end, Long requestedStudentId) {
    return switch (role) {
      case ROLE_LECTURER ->
          scheduleRepository.findByLecturerIdAndDateBetween(currentUser.getUserId(), start, end);
      case ROLE_STUDENT, ROLE_PARENT -> {
        Long targetStudentId =
            studentAccessResolver.resolveTargetStudentId(currentUser, requestedStudentId);
        yield scheduleRepository.findByStudentIdAndDateBetween(targetStudentId, start, end);
      }
      case String r when ROLE_ADMIN.equals(r) && requestedStudentId != null ->
          scheduleRepository.findByStudentIdAndDateBetween(requestedStudentId, start, end);
      case ROLE_ADMIN ->
          scheduleRepository.findByDateBetween(start, end);
      default -> List.of();
    };
  }

  private List<Schedule> findSchedulesForRoleOnDate(
      String role, User currentUser, LocalDate targetDate, Long requestedStudentId) {
    return switch (role) {
      case ROLE_LECTURER ->
          scheduleRepository.findByLecturerIdAndDate(currentUser.getUserId(), targetDate);
      case ROLE_STUDENT, ROLE_PARENT -> {
        Long targetStudentId =
            studentAccessResolver.resolveTargetStudentId(currentUser, requestedStudentId);
        yield scheduleRepository.findByStudentIdAndDate(targetStudentId, targetDate);
      }
      case String r when ROLE_ADMIN.equals(r) && requestedStudentId != null ->
          scheduleRepository.findByStudentIdAndDate(requestedStudentId, targetDate);
      case ROLE_ADMIN ->
          scheduleRepository.findByDate(targetDate);
      default -> List.of();
    };
  }

  @Transactional(readOnly = true)
  public List<ScheduleResponse> getSchedules(LocalDate startDate, LocalDate endDate) {
    if (startDate != null && endDate != null) {
      LocalDate start = startDate.isAfter(endDate) ? endDate : startDate;
      LocalDate end = startDate.isAfter(endDate) ? startDate : endDate;
      return academicMapper.toScheduleResponseList(scheduleRepository.findByDateBetween(start, end));
    }
    LocalDate targetDate = resolveTargetDate(startDate, endDate);
    return findSchedulesByDate(targetDate);
  }

  private LocalDate resolveTargetDate(LocalDate startDate, LocalDate endDate) {
    if (startDate != null) {
      return startDate;
    }
    if (endDate != null) {
      return endDate;
    }
    return LocalDate.now(DEFAULT_ZONE);
  }

  @Transactional(readOnly = true)
  public List<ScheduleResponse> getSchedules(LocalDate date) {
    return findSchedulesByDate(date);
  }

  private List<ScheduleResponse> findSchedulesByDate(LocalDate date) {
    LocalDate targetDate = (date != null) ? date : LocalDate.now(DEFAULT_ZONE);
    return academicMapper.toScheduleResponseList(scheduleRepository.findByDate(targetDate));
  }

  @Transactional(readOnly = true)
  public List<String> getSemesters() {
    List<String> list = scheduleRepository.findDistinctSemesters();
    if (list.isEmpty()) {
      return List.of("Fall2026", "Summer2026", "Spring2026");
    }
    return list;
  }

  @Transactional(readOnly = true)
  public ScheduleResponse getScheduleDetail(Long id, String email) {
    return findScheduleDetail(id, email);
  }

  @Transactional(readOnly = true)
  public ScheduleResponse getScheduleDetail(Long id) {
    return findScheduleDetail(id, null);
  }

  private ScheduleResponse findScheduleDetail(Long id, String email) {
    Schedule schedule =
        scheduleRepository
            .findById(id)
            .orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy buổi học với ID: " + id));

    if (email != null) {
      User user =
          userRepository
              .findByEmailIgnoreCase(email)
              .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
      verifyScheduleAccess(schedule, user);
    }

    return academicMapper.toScheduleResponse(schedule);
  }

  private void verifyScheduleAccess(Schedule schedule, User user) {
    String role = user.getRole() != null ? user.getRole().toUpperCase() : ROLE_STUDENT;
    Long classId = schedule.getClassEntity() != null ? schedule.getClassEntity().getClassId() : null;
    Long userId = user.getUserId();

    switch (role) {
      case String r when ROLE_STUDENT.equals(r) && (classId == null
          || !classEnrollmentRepository.existsByClassEntity_ClassIdAndStudent_UserIdAndStatus(
              classId, userId, "ENROLLED")) ->
          throw new AccessDeniedException("Bạn không theo học lớp của buổi học này");
      case String r when ROLE_LECTURER.equals(r) && (schedule.getLecturer() == null
          || !schedule.getLecturer().getUserId().equals(userId)) ->
          throw new AccessDeniedException("Bạn không phụ trách giảng dạy buổi học này");
      case String r when ROLE_PARENT.equals(r) && (classId == null
          || !classEnrollmentRepository.existsChildEnrolled(classId, userId)) ->
          throw new AccessDeniedException("Bạn không có con em theo học lớp của buổi học này");
      case ROLE_STUDENT, ROLE_LECTURER, ROLE_PARENT, ROLE_ADMIN -> {
        // Access permitted
      }
      default -> throw new AccessDeniedException("Bạn không có quyền truy cập buổi học này");
    }
  }
}
