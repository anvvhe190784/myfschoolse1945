package vn.edu.fpt.fschool.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.fpt.fschool.dto.ImportDtos.ImportResultDto;
import vn.edu.fpt.fschool.model.ClassEntity;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.ClassEnrollmentRepository;
import vn.edu.fpt.fschool.repository.ClassRepository;
import vn.edu.fpt.fschool.repository.GradeRepository;
import vn.edu.fpt.fschool.repository.ScheduleRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminImportTest {

  @Mock private UserRepository userRepository;
  @Mock private ClassRepository classRepository;
  @Mock private ClassEnrollmentRepository classEnrollmentRepository;
  @Mock private GradeRepository gradeRepository;
  @Mock private ScheduleRepository scheduleRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AdminImportService adminImportService;

  @BeforeEach
  void setUp() {
    lenient().when(passwordEncoder.encode(any())).thenReturn("hashedDefaultPass");
  }

  @Test
  @DisplayName("Bulk Import Users: Đọc file CSV và lưu danh sách sinh viên hợp lệ")
  void importUsers_shouldParseCsv_andSaveAll() {
    String csv =
        """
        rollNumber,email,fullName,phoneNumber,role,status
        SE194599,test_import@fpt.edu.vn,Nguyen Test,0911223344,STUDENT,ACTIVE
        """;
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "users.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

    when(userRepository.existsByEmail("test_import@fpt.edu.vn")).thenReturn(false);
    when(userRepository.existsByRollNumber("SE194599")).thenReturn(false);

    ImportResultDto result = adminImportService.importUsers(file);

    assertEquals(1, result.totalRows());
    assertEquals(1, result.successCount());
    assertEquals(0, result.failedCount());
    verify(userRepository, times(1)).saveAll(any());
  }

  @Test
  @DisplayName("Bulk Import Users: Báo lỗi khi email đã tồn tại")
  void importUsers_shouldCollectErrors_whenEmailExists() {
    String csv =
        """
        rollNumber,email,fullName,phoneNumber,role,status
        SE194599,duplicate@fpt.edu.vn,Nguyen Duplicate,0911223344,STUDENT,ACTIVE
        """;
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "users.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

    when(userRepository.existsByEmail("duplicate@fpt.edu.vn")).thenReturn(true);

    ImportResultDto result = adminImportService.importUsers(file);

    assertEquals(1, result.totalRows());
    assertEquals(0, result.successCount());
    assertEquals(1, result.failedCount());
    assertTrue(result.errors().get(0).contains("đã tồn tại"));
    verify(userRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("Bulk Import Grades: Đọc file CSV kèm mã môn và lưu điểm thành công")
  void importGrades_shouldParseCsv_andSaveAll() {
    String csv =
        """
        rollNumber,classCode,courseCode,assessmentName,weight,score,feedback,status
        SE194501,SE1945,PRM393,Lab 3,10.00,9.0,Good,PUBLISHED
        """;
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "grades.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

    User student = User.builder().userId(10L).rollNumber("SE194501").build();
    ClassEntity classEntity = new ClassEntity();
    classEntity.setClassId(20L);
    classEntity.setClassCode("SE1945");

    when(userRepository.findByRollNumber("SE194501")).thenReturn(Optional.of(student));
    when(classRepository.findByClassCodeAndCourse_CourseCode("SE1945", "PRM393"))
        .thenReturn(Optional.of(classEntity));

    ImportResultDto result = adminImportService.importGrades(file);

    assertEquals(1, result.totalRows());
    assertEquals(1, result.successCount());
    assertEquals(0, result.failedCount());
    verify(gradeRepository, times(1)).saveAll(any());
  }

  @Test
  @DisplayName("Bulk Import Enrollments: Đọc file CSV và ghi danh sinh viên thành công")
  void importEnrollments_shouldParseCsv_andSaveAll() {
    String csv =
        """
        rollNumber,classCode,courseCode,status
        SE194501,SE1945,PRM393,ENROLLED
        """;
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "enrollments.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

    User student = User.builder().userId(10L).rollNumber("SE194501").build();
    ClassEntity classEntity = new ClassEntity();
    classEntity.setClassId(20L);
    classEntity.setClassCode("SE1945");

    when(userRepository.findByRollNumber("SE194501")).thenReturn(Optional.of(student));
    when(classRepository.findByClassCodeAndCourse_CourseCode("SE1945", "PRM393"))
        .thenReturn(Optional.of(classEntity));
    when(classEnrollmentRepository.findByClassEntity_ClassIdAndStudent_UserId(20L, 10L))
        .thenReturn(Optional.empty());

    ImportResultDto result = adminImportService.importEnrollments(file);

    assertEquals(1, result.totalRows());
    assertEquals(1, result.successCount());
    assertEquals(0, result.failedCount());
    verify(classEnrollmentRepository, times(1)).saveAll(any());
  }

  @Test
  @DisplayName("Download Template: Trả về nội dung CSV mẫu chính xác")
  void getTemplateContent_shouldReturnCsvHeader() {
    String userTemplate = adminImportService.getTemplateContent("users");
    assertTrue(userTemplate.contains("rollNumber,email,fullName"));

    String gradeTemplate = adminImportService.getTemplateContent("grades");
    assertTrue(gradeTemplate.contains("rollNumber,classCode,courseCode,assessmentName"));

    String scheduleTemplate = adminImportService.getTemplateContent("schedules");
    assertTrue(scheduleTemplate.contains("classCode,courseCode,date,slotNumber"));

    String enrollmentTemplate = adminImportService.getTemplateContent("enrollments");
    assertTrue(enrollmentTemplate.contains("rollNumber,classCode,courseCode,status"));
  }
}
