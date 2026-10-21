package vn.edu.fpt.fschool.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.fschool.dto.AttendanceDtos.*;
import vn.edu.fpt.fschool.model.Attendance;
import vn.edu.fpt.fschool.model.ClassEnrollment;
import vn.edu.fpt.fschool.model.Schedule;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.AttendanceRepository;
import vn.edu.fpt.fschool.repository.ClassEnrollmentRepository;
import vn.edu.fpt.fschool.repository.ScheduleRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AttendanceService {

  private static final String ROLE_STUDENT = "STUDENT";
  private static final String ROLE_LECTURER = "LECTURER";
  private static final String ROLE_ADMIN = "ADMIN";
  private static final String USER_NOT_FOUND = "Không tìm thấy người dùng";
  private static final String SCHEDULE_NOT_FOUND = "Không tìm thấy buổi học với ID: ";
  private static final String STATUS_COMPLETED = "COMPLETED";

  private final AttendanceRepository attendanceRepository;
  private final ScheduleRepository scheduleRepository;
  private final UserRepository userRepository;
  private final ClassEnrollmentRepository classEnrollmentRepository;

  @Transactional(readOnly = true)
  public ScheduleAttendanceResponse getScheduleAttendance(Long scheduleId, String requesterEmail) {
    if (requesterEmail == null || requesterEmail.isBlank()) {
      throw new IllegalArgumentException(USER_NOT_FOUND);
    }

    Schedule schedule =
        scheduleRepository
            .findById(scheduleId)
            .orElseThrow(() -> new IllegalArgumentException(SCHEDULE_NOT_FOUND + scheduleId));

    User currentUser =
        userRepository
            .findByEmailIgnoreCase(requesterEmail)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

    verifyGetAttendanceAccess(schedule, currentUser);

    Long classId =
        schedule.getClassEntity() != null ? schedule.getClassEntity().getClassId() : null;
    String classCode =
        schedule.getClassEntity() != null ? schedule.getClassEntity().getClassCode() : null;
    String courseCode =
        schedule.getClassEntity() != null && schedule.getClassEntity().getCourse() != null
            ? schedule.getClassEntity().getCourse().getCourseCode()
            : null;

    List<ClassEnrollment> enrollments = resolveEnrollments(schedule);
    Map<Long, Attendance> attendanceMap = buildAttendanceMap(scheduleId);
    List<AttendanceItemResponse> items =
        filterItemsByRole(buildAttendanceItems(enrollments, attendanceMap), currentUser);

    int totalStudents = items.size();
    int presentCount =
        (int) items.stream().filter(i -> "PRESENT".equalsIgnoreCase(i.status())).count();
    int absentCount =
        (int) items.stream().filter(i -> "ABSENT".equalsIgnoreCase(i.status())).count();
    int lateCount =
        (int) items.stream().filter(i -> "LATE".equalsIgnoreCase(i.status())).count();

    return new ScheduleAttendanceResponse(
        schedule.getScheduleId(),
        classId,
        classCode,
        courseCode,
        schedule.getDate(),
        schedule.getSlotNumber(),
        items,
        totalStudents,
        presentCount,
        absentCount,
        lateCount);
  }

  private List<ClassEnrollment> resolveEnrollments(Schedule schedule) {
    Long classId =
        schedule.getClassEntity() != null ? schedule.getClassEntity().getClassId() : null;
    return (classId != null)
        ? classEnrollmentRepository.findByClassEntity_ClassIdAndStatus(classId, "ENROLLED")
        : List.of();
  }

  private Map<Long, Attendance> buildAttendanceMap(Long scheduleId) {
    List<Attendance> attendances = attendanceRepository.findByScheduleId(scheduleId);
    return attendances.stream()
        .filter(a -> a.getStudent() != null && a.getStudent().getUserId() != null)
        .collect(
            Collectors.toMap(
                a -> a.getStudent().getUserId(),
                a -> a,
                (existing, replacement) -> existing));
  }

  private List<AttendanceItemResponse> buildAttendanceItems(
      List<ClassEnrollment> enrollments, Map<Long, Attendance> attendanceMap) {
    List<AttendanceItemResponse> items = new ArrayList<>();
    for (ClassEnrollment enrollment : enrollments) {
      User student = enrollment.getStudent();
      if (student != null) {
        Attendance attendance = attendanceMap.get(student.getUserId());
        String status = attendance != null ? attendance.getStatus() : null;
        String note = attendance != null ? attendance.getNote() : null;
        items.add(
            new AttendanceItemResponse(
                student.getUserId(),
                student.getRollNumber(),
                student.getFullName(),
                status,
                note));
      }
    }
    items.sort(
        Comparator.comparing(
            AttendanceItemResponse::rollNumber, Comparator.nullsLast(String::compareTo)));
    return items;
  }

  private List<AttendanceItemResponse> filterItemsByRole(
      List<AttendanceItemResponse> items, User currentUser) {
    String role =
        currentUser.getRole() != null ? currentUser.getRole().toUpperCase() : ROLE_STUDENT;
    if (ROLE_STUDENT.equals(role)) {
      return items.stream()
          .filter(item -> Objects.equals(item.studentId(), currentUser.getUserId()))
          .toList();
    }
    return items;
  }

  @Transactional
  public ScheduleAttendanceResponse saveScheduleAttendance(
      Long scheduleId, TakeAttendanceRequest request, String lecturerEmail) {
    if (request == null || request.entries() == null) {
      throw new IllegalArgumentException("Danh sách điểm danh không được để trống");
    }
    if (lecturerEmail == null || lecturerEmail.isBlank()) {
      throw new IllegalArgumentException(USER_NOT_FOUND);
    }

    Schedule schedule =
        scheduleRepository
            .findById(scheduleId)
            .orElseThrow(() -> new IllegalArgumentException(SCHEDULE_NOT_FOUND + scheduleId));

    User lecturerOrAdmin =
        userRepository
            .findByEmailIgnoreCase(lecturerEmail)
            .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

    verifySaveAttendanceAccess(schedule, lecturerOrAdmin);

    for (StudentAttendanceEntry entry : request.entries()) {
      if (entry.studentId() == null) {
        continue;
      }
      User student =
          userRepository
              .findById(entry.studentId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Không tìm thấy sinh viên với ID: " + entry.studentId()));

      Optional<Attendance> existingOpt =
          attendanceRepository.findByScheduleIdAndStudentId(scheduleId, entry.studentId());

      Attendance attendance;
      if (existingOpt.isPresent()) {
        attendance = existingOpt.get();
        attendance.setStatus(entry.status());
        attendance.setNote(entry.note());
        attendance.setRecordedBy(lecturerOrAdmin);
        attendance.setRecordedAt(OffsetDateTime.now(ZoneOffset.UTC));
      } else {
        attendance =
            Attendance.builder()
                .schedule(schedule)
                .student(student)
                .status(entry.status())
                .note(entry.note())
                .recordedBy(lecturerOrAdmin)
                .recordedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
      }
      attendanceRepository.save(attendance);
    }

    schedule.setStatus(STATUS_COMPLETED);
    scheduleRepository.save(schedule);

    return getScheduleAttendance(scheduleId, lecturerEmail);
  }

  private void verifyGetAttendanceAccess(Schedule schedule, User user) {
    String role = user.getRole() != null ? user.getRole().toUpperCase() : ROLE_STUDENT;
    Long classId =
        schedule.getClassEntity() != null ? schedule.getClassEntity().getClassId() : null;
    Long userId = user.getUserId();

    switch (role) {
      case ROLE_ADMIN -> {
        // Admin has full access
      }
      case ROLE_LECTURER -> {
        if (schedule.getLecturer() == null
            || !Objects.equals(schedule.getLecturer().getUserId(), userId)) {
          throw new AccessDeniedException("Bạn không phụ trách giảng dạy buổi học này");
        }
      }
      case ROLE_STUDENT -> {
        if (classId == null
            || !classEnrollmentRepository
                .existsByClassEntity_ClassIdAndStudent_UserIdAndStatus(
                    classId, userId, "ENROLLED")) {
          throw new AccessDeniedException("Bạn không theo học lớp của buổi học này");
        }
      }
      default -> throw new AccessDeniedException("Bạn không có quyền truy cập buổi học này");
    }
  }

  private void verifySaveAttendanceAccess(Schedule schedule, User user) {
    String role = user.getRole() != null ? user.getRole().toUpperCase() : "";
    if (ROLE_ADMIN.equals(role)) {
      return;
    }
    if (ROLE_LECTURER.equals(role)) {
      if (schedule.getLecturer() == null
          || !Objects.equals(schedule.getLecturer().getUserId(), user.getUserId())) {
        throw new AccessDeniedException("Bạn không phụ trách giảng dạy buổi học này");
      }
      return;
    }
    throw new AccessDeniedException("Bạn không có quyền điểm danh cho buổi học này");
  }
}
