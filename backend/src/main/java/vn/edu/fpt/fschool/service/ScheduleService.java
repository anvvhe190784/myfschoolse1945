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
  private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  private final ScheduleRepository scheduleRepository;
  private final UserRepository userRepository;
  private final StudentAccessResolver studentAccessResolver;
  private final ClassEnrollmentRepository classEnrollmentRepository;
  private final AcademicMapper academicMapper;

  @Transactional(readOnly = true)
  public List<ScheduleResponse> getSchedules(
      String email, LocalDate date, Long requestedStudentId) {
    LocalDate targetDate = (date != null) ? date : LocalDate.now(DEFAULT_ZONE);

    User currentUser =
        userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

    String role =
        currentUser.getRole() != null ? currentUser.getRole().toUpperCase() : ROLE_STUDENT;
    List<Schedule> schedules;

    switch (role) {
      case "LECTURER" ->
          schedules =
              scheduleRepository.findByLecturerIdAndDate(currentUser.getUserId(), targetDate);
      case ROLE_STUDENT, "PARENT" -> {
        Long targetStudentId =
            studentAccessResolver.resolveTargetStudentId(currentUser, requestedStudentId);
        schedules = scheduleRepository.findByStudentIdAndDate(targetStudentId, targetDate);
      }
      case "ADMIN" -> {
        if (requestedStudentId != null) {
          schedules = scheduleRepository.findByStudentIdAndDate(requestedStudentId, targetDate);
        } else {
          schedules = scheduleRepository.findByDate(targetDate);
        }
      }
      default -> schedules = List.of();
    }

    return academicMapper.toScheduleResponseList(schedules);
  }

  @Transactional(readOnly = true)
  public List<ScheduleResponse> getSchedules(LocalDate date) {
    LocalDate targetDate = (date != null) ? date : LocalDate.now(DEFAULT_ZONE);
    return academicMapper.toScheduleResponseList(scheduleRepository.findByDate(targetDate));
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
              .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
      String role = user.getRole() != null ? user.getRole().toUpperCase() : ROLE_STUDENT;

      if (ROLE_STUDENT.equals(role)) {
        boolean enrolled =
            classEnrollmentRepository.existsByClassEntity_ClassIdAndStudent_UserIdAndStatus(
                schedule.getClassEntity().getClassId(), user.getUserId(), "ENROLLED");
        if (!enrolled) {
          throw new AccessDeniedException("Bạn không theo học lớp của buổi học này");
        }
      } else if ("LECTURER".equals(role)
          && (schedule.getLecturer() == null
              || !schedule.getLecturer().getUserId().equals(user.getUserId()))) {
        throw new AccessDeniedException("Bạn không phụ trách giảng dạy buổi học này");
      }
    }

    return academicMapper.toScheduleResponse(schedule);
  }
}
