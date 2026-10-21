package vn.edu.fpt.fschool.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import vn.edu.fpt.fschool.dto.AttendanceDtos.*;
import vn.edu.fpt.fschool.model.*;
import vn.edu.fpt.fschool.repository.AttendanceRepository;
import vn.edu.fpt.fschool.repository.ClassEnrollmentRepository;
import vn.edu.fpt.fschool.repository.ScheduleRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

  @Mock private AttendanceRepository attendanceRepository;
  @Mock private ScheduleRepository scheduleRepository;
  @Mock private UserRepository userRepository;
  @Mock private ClassEnrollmentRepository classEnrollmentRepository;

  @InjectMocks private AttendanceService attendanceService;

  private Schedule schedule;
  private ClassEntity classEntity;
  private Course course;
  private User lecturer;
  private User otherLecturer;
  private User student1;
  private User student2;

  @BeforeEach
  void setUp() {
    lecturer =
        User.builder()
            .userId(201L)
            .email("lecturer@fe.edu.vn")
            .fullName("Giang vien A")
            .role("LECTURER")
            .build();

    otherLecturer =
        User.builder()
            .userId(202L)
            .email("other@fe.edu.vn")
            .fullName("Giang vien Khac")
            .role("LECTURER")
            .build();

    student1 =
        User.builder()
            .userId(101L)
            .email("student1@fpt.edu.vn")
            .rollNumber("SE194501")
            .fullName("Sinh vien 1")
            .role("STUDENT")
            .build();

    student2 =
        User.builder()
            .userId(102L)
            .email("student2@fpt.edu.vn")
            .rollNumber("SE194502")
            .fullName("Sinh vien 2")
            .role("STUDENT")
            .build();

    course = new Course();
    course.setCourseId(1L);
    course.setCourseCode("PRM393");
    course.setCourseName("Mobile Programming");

    classEntity = new ClassEntity();
    classEntity.setClassId(10L);
    classEntity.setClassCode("SE1945");
    classEntity.setCourse(course);
    classEntity.setLecturer(lecturer);

    schedule = new Schedule();
    schedule.setScheduleId(1L);
    schedule.setClassEntity(classEntity);
    schedule.setLecturer(lecturer);
    schedule.setDate(LocalDate.of(2026, 10, 21));
    schedule.setSlotNumber(1);
    schedule.setStatus("UPCOMING");
  }

  @Test
  @DisplayName("getScheduleAttendance: Buổi học không tồn tại sẽ ném IllegalArgumentException")
  void getScheduleAttendance_scheduleNotFound_throwsException() {
    when(scheduleRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> attendanceService.getScheduleAttendance(999L, "lecturer@fe.edu.vn"));
  }

  @Test
  @DisplayName("getScheduleAttendance: Giảng viên không dạy buổi học sẽ bị AccessDeniedException")
  void getScheduleAttendance_otherLecturer_throwsAccessDenied() {
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
    when(userRepository.findByEmailIgnoreCase("other@fe.edu.vn"))
        .thenReturn(Optional.of(otherLecturer));

    assertThrows(
        AccessDeniedException.class,
        () -> attendanceService.getScheduleAttendance(1L, "other@fe.edu.vn"));
  }

  @Test
  @DisplayName("getScheduleAttendance: Sinh viên không thuộc lớp sẽ bị AccessDeniedException")
  void getScheduleAttendance_unenrolledStudent_throwsAccessDenied() {
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
    when(userRepository.findByEmailIgnoreCase("student1@fpt.edu.vn"))
        .thenReturn(Optional.of(student1));
    when(classEnrollmentRepository.existsByClassEntity_ClassIdAndStudent_UserIdAndStatus(
            10L, 101L, "ENROLLED"))
        .thenReturn(false);

    assertThrows(
        AccessDeniedException.class,
        () -> attendanceService.getScheduleAttendance(1L, "student1@fpt.edu.vn"));
  }

  @Test
  @DisplayName("getScheduleAttendance: Giảng viên phụ trách xem điểm danh trả về toàn bộ lớp")
  void getScheduleAttendance_asLecturer_returnsAllStudents() {
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
    when(userRepository.findByEmailIgnoreCase("lecturer@fe.edu.vn")).thenReturn(Optional.of(lecturer));

    ClassEnrollment e1 = ClassEnrollment.builder().classEntity(classEntity).student(student1).build();
    ClassEnrollment e2 = ClassEnrollment.builder().classEntity(classEntity).student(student2).build();
    when(classEnrollmentRepository.findByClassEntity_ClassIdAndStatus(10L, "ENROLLED"))
        .thenReturn(List.of(e1, e2));

    Attendance att1 =
        Attendance.builder()
            .schedule(schedule)
            .student(student1)
            .status("PRESENT")
            .note("Dung gio")
            .build();
    when(attendanceRepository.findByScheduleId(1L)).thenReturn(List.of(att1));

    ScheduleAttendanceResponse response =
        attendanceService.getScheduleAttendance(1L, "lecturer@fe.edu.vn");

    assertNotNull(response);
    assertEquals(1L, response.scheduleId());
    assertEquals("SE1945", response.classCode());
    assertEquals("PRM393", response.courseCode());
    assertEquals(2, response.totalStudents());
    assertEquals(1, response.presentCount());
    assertEquals(0, response.absentCount());
    assertEquals(0, response.lateCount());

    AttendanceItemResponse item1 =
        response.students().stream().filter(s -> s.studentId().equals(101L)).findFirst().orElseThrow();
    assertEquals("PRESENT", item1.status());
    assertEquals("Dung gio", item1.note());

    AttendanceItemResponse item2 =
        response.students().stream().filter(s -> s.studentId().equals(102L)).findFirst().orElseThrow();
    assertNull(item2.status());
  }

  @Test
  @DisplayName("getScheduleAttendance: Sinh viên xem chỉ thấy bản ghi của chính mình")
  void getScheduleAttendance_asStudent_returnsOnlyOwnRecord() {
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
    when(userRepository.findByEmailIgnoreCase("student1@fpt.edu.vn"))
        .thenReturn(Optional.of(student1));
    when(classEnrollmentRepository.existsByClassEntity_ClassIdAndStudent_UserIdAndStatus(
            10L, 101L, "ENROLLED"))
        .thenReturn(true);

    ClassEnrollment e1 = ClassEnrollment.builder().classEntity(classEntity).student(student1).build();
    ClassEnrollment e2 = ClassEnrollment.builder().classEntity(classEntity).student(student2).build();
    when(classEnrollmentRepository.findByClassEntity_ClassIdAndStatus(10L, "ENROLLED"))
        .thenReturn(List.of(e1, e2));

    Attendance att1 =
        Attendance.builder()
            .schedule(schedule)
            .student(student1)
            .status("PRESENT")
            .note("Co mat")
            .build();
    when(attendanceRepository.findByScheduleId(1L)).thenReturn(List.of(att1));

    ScheduleAttendanceResponse response =
        attendanceService.getScheduleAttendance(1L, "student1@fpt.edu.vn");

    assertNotNull(response);
    assertEquals(1, response.students().size());
    assertEquals(101L, response.students().get(0).studentId());
    assertEquals("PRESENT", response.students().get(0).status());
  }

  @Test
  @DisplayName("saveScheduleAttendance: Lưu điểm danh thành công và cập nhật status buổi học sang COMPLETED")
  void saveScheduleAttendance_success_updatesScheduleToCompleted() {
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
    when(userRepository.findByEmailIgnoreCase("lecturer@fe.edu.vn")).thenReturn(Optional.of(lecturer));
    when(userRepository.findById(101L)).thenReturn(Optional.of(student1));
    when(attendanceRepository.findByScheduleIdAndStudentId(1L, 101L)).thenReturn(Optional.empty());

    ClassEnrollment e1 = ClassEnrollment.builder().classEntity(classEntity).student(student1).build();
    when(classEnrollmentRepository.findByClassEntity_ClassIdAndStatus(10L, "ENROLLED"))
        .thenReturn(List.of(e1));

    Attendance savedAtt =
        Attendance.builder()
            .schedule(schedule)
            .student(student1)
            .status("PRESENT")
            .note("On time")
            .build();
    when(attendanceRepository.findByScheduleId(1L)).thenReturn(List.of(savedAtt));

    TakeAttendanceRequest request =
        new TakeAttendanceRequest(
            List.of(new StudentAttendanceEntry(101L, "PRESENT", "On time")));

    ScheduleAttendanceResponse response =
        attendanceService.saveScheduleAttendance(1L, request, "lecturer@fe.edu.vn");

    assertNotNull(response);
    assertEquals("COMPLETED", schedule.getStatus());
    verify(attendanceRepository, times(1)).save(any(Attendance.class));
    verify(scheduleRepository, times(1)).save(schedule);
  }

  @Test
  @DisplayName("saveScheduleAttendance: Người không phải giảng viên phụ trách hoặc ADMIN sẽ bị AccessDeniedException")
  void saveScheduleAttendance_unauthorizedUser_throwsAccessDenied() {
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
    when(userRepository.findByEmailIgnoreCase("other@fe.edu.vn"))
        .thenReturn(Optional.of(otherLecturer));

    TakeAttendanceRequest request =
        new TakeAttendanceRequest(
            List.of(new StudentAttendanceEntry(101L, "PRESENT", null)));

    assertThrows(
        AccessDeniedException.class,
        () -> attendanceService.saveScheduleAttendance(1L, request, "other@fe.edu.vn"));
  }
}
