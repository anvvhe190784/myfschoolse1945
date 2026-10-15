package vn.edu.fpt.fschool.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
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
import vn.edu.fpt.fschool.dto.ScheduleDtos.ScheduleResponse;
import vn.edu.fpt.fschool.mapper.AcademicMapper;
import vn.edu.fpt.fschool.model.ClassEntity;
import vn.edu.fpt.fschool.model.Course;
import vn.edu.fpt.fschool.model.Schedule;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.ClassEnrollmentRepository;
import vn.edu.fpt.fschool.repository.ScheduleRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ScheduleSecurityTest {

  @Mock private ScheduleRepository scheduleRepository;
  @Mock private UserRepository userRepository;
  @Mock private StudentAccessResolver studentAccessResolver;
  @Mock private ClassEnrollmentRepository classEnrollmentRepository;
  @Mock private AcademicMapper academicMapper;

  @InjectMocks private ScheduleService scheduleService;

  private User student;
  private User lecturer;
  private User parent;
  private Schedule schedule;
  private ClassEntity classEntity;

  @BeforeEach
  void setUp() {
    student = User.builder().userId(101L).email("student@fpt.edu.vn").role("STUDENT").build();
    lecturer = User.builder().userId(201L).email("lecturer@fe.edu.vn").role("LECTURER").build();
    parent = User.builder().userId(301L).email("parent@gmail.com").role("PARENT").build();

    Course course = new Course();
    course.setCourseCode("PRJ301");
    course.setCourseName("Java Web");

    classEntity = new ClassEntity();
    classEntity.setClassId(10L);
    classEntity.setClassCode("SE1945");
    classEntity.setCourse(course);
    classEntity.setLecturer(lecturer);

    schedule = new Schedule();
    schedule.setScheduleId(1L);
    schedule.setClassEntity(classEntity);
    schedule.setLecturer(lecturer);
    schedule.setDate(LocalDate.now());
    schedule.setSlotNumber(1);
    schedule.setSessionNo(1);
    schedule.setStartTime(LocalTime.of(7, 30));
    schedule.setEndTime(LocalTime.of(9, 50));
    schedule.setRoom("BE-301");
  }

  @Test
  @DisplayName("Sinh viên: Lấy đúng lịch học của các lớp mình đã đăng ký")
  void getSchedules_student_shouldQueryEnrolledClasses() {
    LocalDate today = LocalDate.now();
    when(userRepository.findByEmailIgnoreCase("student@fpt.edu.vn"))
        .thenReturn(Optional.of(student));
    when(studentAccessResolver.resolveTargetStudentId(student, null)).thenReturn(101L);
    when(scheduleRepository.findByStudentIdAndDate(101L, today)).thenReturn(List.of(schedule));
    ScheduleResponse response =
        new ScheduleResponse(
            1L,
            10L,
            "SE1945",
            "PRJ301",
            "Java Web",
            1,
            1,
            today,
            LocalTime.of(7, 30),
            LocalTime.of(9, 50),
            "BE-301",
            "Mr. Teacher",
            "Note",
            "ACTIVE");
    when(academicMapper.toScheduleResponseList(List.of(schedule))).thenReturn(List.of(response));

    List<ScheduleResponse> results =
        scheduleService.getSchedules("student@fpt.edu.vn", today, null);
    assertEquals(1, results.size());
    assertEquals("SE1945", results.get(0).classCode());
    verify(scheduleRepository).findByStudentIdAndDate(101L, today);
  }

  @Test
  @DisplayName("Giảng viên: Lấy lịch dạy theo ID giảng viên")
  void getSchedules_lecturer_shouldQueryTaughtClasses() {
    LocalDate today = LocalDate.now();
    when(userRepository.findByEmailIgnoreCase("lecturer@fe.edu.vn"))
        .thenReturn(Optional.of(lecturer));
    when(scheduleRepository.findByLecturerIdAndDate(201L, today)).thenReturn(List.of(schedule));
    ScheduleResponse response =
        new ScheduleResponse(
            1L,
            10L,
            "SE1945",
            "PRJ301",
            "Java Web",
            1,
            1,
            today,
            LocalTime.of(7, 30),
            LocalTime.of(9, 50),
            "BE-301",
            "Mr. Teacher",
            "Note",
            "ACTIVE");
    when(academicMapper.toScheduleResponseList(List.of(schedule))).thenReturn(List.of(response));

    List<ScheduleResponse> results =
        scheduleService.getSchedules("lecturer@fe.edu.vn", today, null);
    assertEquals(1, results.size());
    verify(scheduleRepository).findByLecturerIdAndDate(201L, today);
  }

  @Test
  @DisplayName("Phụ huynh: Lấy lịch học của con thông qua StudentAccessResolver")
  void getSchedules_parent_shouldResolveChildId() {
    LocalDate today = LocalDate.now();
    when(userRepository.findByEmailIgnoreCase("parent@gmail.com")).thenReturn(Optional.of(parent));
    when(studentAccessResolver.resolveTargetStudentId(parent, 101L)).thenReturn(101L);
    when(scheduleRepository.findByStudentIdAndDate(101L, today)).thenReturn(List.of(schedule));
    ScheduleResponse response =
        new ScheduleResponse(
            1L,
            10L,
            "SE1945",
            "PRJ301",
            "Java Web",
            1,
            1,
            today,
            LocalTime.of(7, 30),
            LocalTime.of(9, 50),
            "BE-301",
            "Mr. Teacher",
            "Note",
            "ACTIVE");
    when(academicMapper.toScheduleResponseList(List.of(schedule))).thenReturn(List.of(response));

    List<ScheduleResponse> results = scheduleService.getSchedules("parent@gmail.com", today, 101L);
    assertEquals(1, results.size());
    verify(studentAccessResolver).resolveTargetStudentId(parent, 101L);
    verify(scheduleRepository).findByStudentIdAndDate(101L, today);
  }

  @Test
  @DisplayName("Chi tiết lịch: Sinh viên không theo học lớp bị từ chối truy cập (BOLA/IDOR)")
  void getScheduleDetail_studentNotEnrolled_shouldThrowAccessDenied() {
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
    when(userRepository.findByEmailIgnoreCase("student@fpt.edu.vn"))
        .thenReturn(Optional.of(student));
    when(classEnrollmentRepository.existsByClassEntity_ClassIdAndStudent_UserIdAndStatus(
            10L, 101L, "ENROLLED"))
        .thenReturn(false);

    assertThrows(
        AccessDeniedException.class,
        () -> scheduleService.getScheduleDetail(1L, "student@fpt.edu.vn"));
  }
}
