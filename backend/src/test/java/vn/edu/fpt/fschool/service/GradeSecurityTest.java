package vn.edu.fpt.fschool.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import vn.edu.fpt.fschool.dto.GradeDtos.GradeResponse;
import vn.edu.fpt.fschool.mapper.AcademicMapper;
import vn.edu.fpt.fschool.model.ClassEntity;
import vn.edu.fpt.fschool.model.Grade;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.GradeRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class GradeSecurityTest {

  @Mock private GradeRepository gradeRepository;

  @Mock private UserRepository userRepository;

  @Mock private StudentAccessResolver studentAccessResolver;

  @Mock private AcademicMapper academicMapper;

  @InjectMocks private GradeService gradeService;

  private User studentA;
  private User studentB;
  private User lecturer;
  private Grade gradeStudentA;
  private GradeResponse gradeResponse;

  @BeforeEach
  void setUp() {
    studentA = User.builder().userId(101L).email("studentA@fpt.edu.vn").role("STUDENT").build();
    studentB = User.builder().userId(102L).email("studentB@fpt.edu.vn").role("STUDENT").build();
    lecturer = User.builder().userId(201L).email("teacher@fe.edu.vn").role("LECTURER").build();

    ClassEntity classEntity = new ClassEntity();
    classEntity.setClassId(1L);
    classEntity.setClassCode("SE1945");
    classEntity.setLecturer(lecturer);

    gradeStudentA = new Grade();
    gradeStudentA.setGradeId(500L);
    gradeStudentA.setStudent(studentA);
    gradeStudentA.setClassEntity(classEntity);
    gradeStudentA.setAssessmentName("Progress Test 1");
    gradeStudentA.setWeight(new BigDecimal("10.00"));
    gradeStudentA.setScore(new BigDecimal("8.50"));

    gradeResponse =
        new GradeResponse(
            500L,
            1L,
            "SE1945",
            "PRJ301",
            "Progress Test 1",
            new BigDecimal("10.00"),
            new BigDecimal("8.50"),
            "Good",
            "PASSED");
  }

  @Test
  @DisplayName("Bảo mật BOLA/IDOR: Sinh viên B KHÔNG ĐƯỢC PHÉP xem điểm của Sinh viên A")
  void getGradeDetail_shouldReject_whenAccessedByAnotherStudent() {
    when(gradeRepository.findById(500L)).thenReturn(Optional.of(gradeStudentA));
    when(userRepository.findByEmailIgnoreCase("studentB@fpt.edu.vn"))
        .thenReturn(Optional.of(studentB));

    assertThrows(
        AccessDeniedException.class,
        () -> gradeService.getGradeDetail(500L, "studentB@fpt.edu.vn"));
  }

  @Test
  @DisplayName("Thành công: Sinh viên A được phép xem điểm của chính mình")
  void getGradeDetail_shouldSucceed_whenAccessedByOwner() {
    when(gradeRepository.findById(500L)).thenReturn(Optional.of(gradeStudentA));
    when(userRepository.findByEmailIgnoreCase("studentA@fpt.edu.vn"))
        .thenReturn(Optional.of(studentA));
    when(academicMapper.toGradeResponse(gradeStudentA)).thenReturn(gradeResponse);

    GradeResponse response = gradeService.getGradeDetail(500L, "studentA@fpt.edu.vn");
    assertNotNull(response);
    assertEquals(500L, response.gradeId());
    assertEquals(new BigDecimal("8.50"), response.score());
  }

  @Test
  @DisplayName("Thành công: Giảng viên phụ trách lớp được phép xem điểm của sinh viên trong lớp")
  void getGradeDetail_shouldSucceed_whenAccessedByTeacherOfClass() {
    when(gradeRepository.findById(500L)).thenReturn(Optional.of(gradeStudentA));
    when(userRepository.findByEmailIgnoreCase("teacher@fe.edu.vn"))
        .thenReturn(Optional.of(lecturer));
    when(academicMapper.toGradeResponse(gradeStudentA)).thenReturn(gradeResponse);

    GradeResponse response = gradeService.getGradeDetail(500L, "teacher@fe.edu.vn");
    assertNotNull(response);
    assertEquals(500L, response.gradeId());
  }

  @Test
  @DisplayName("Thành công: Phụ huynh được phép xem chi tiết điểm của con mình")
  void getGradeDetail_shouldSucceed_whenAccessedByParentOfStudent() {
    User parent = User.builder().userId(301L).email("parent@gmail.com").role("PARENT").build();
    when(gradeRepository.findById(500L)).thenReturn(Optional.of(gradeStudentA));
    when(userRepository.findByEmailIgnoreCase("parent@gmail.com")).thenReturn(Optional.of(parent));
    when(studentAccessResolver.isParentOf(301L, 101L)).thenReturn(true);
    when(academicMapper.toGradeResponse(gradeStudentA)).thenReturn(gradeResponse);

    GradeResponse response = gradeService.getGradeDetail(500L, "parent@gmail.com");
    assertNotNull(response);
    assertEquals(500L, response.gradeId());
  }

  @Test
  @DisplayName("Bảo mật: Phụ huynh KHÔNG ĐƯỢC PHÉP xem điểm của học sinh khác")
  void getGradeDetail_shouldReject_whenNotParentOfStudent() {
    User parent = User.builder().userId(301L).email("parent@gmail.com").role("PARENT").build();
    when(gradeRepository.findById(500L)).thenReturn(Optional.of(gradeStudentA));
    when(userRepository.findByEmailIgnoreCase("parent@gmail.com")).thenReturn(Optional.of(parent));
    when(studentAccessResolver.isParentOf(301L, 101L)).thenReturn(false);

    assertThrows(
        AccessDeniedException.class, () -> gradeService.getGradeDetail(500L, "parent@gmail.com"));
  }
}
