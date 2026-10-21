package vn.edu.fpt.fschool.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import vn.edu.fpt.fschool.dto.RequestDtos.CreateRequestDto;
import vn.edu.fpt.fschool.dto.RequestDtos.RequestResponse;
import vn.edu.fpt.fschool.model.RequestEntity;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.ClassEnrollmentRepository;
import vn.edu.fpt.fschool.repository.ParentStudentRepository;
import vn.edu.fpt.fschool.repository.RequestRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ParentAccessTest {

  @Mock private ParentStudentRepository parentStudentRepository;
  @Mock private ClassEnrollmentRepository classEnrollmentRepository;
  @Mock private UserRepository userRepository;
  @Mock private RequestRepository requestRepository;
  @Mock private vn.edu.fpt.fschool.mapper.AcademicMapper academicMapper;

  private StudentAccessResolver studentAccessResolver;
  private RequestService requestService;

  private User studentUser;
  private User parentUser;

  @BeforeEach
  void setUp() {
    studentAccessResolver =
        new StudentAccessResolver(parentStudentRepository, classEnrollmentRepository);
    requestService =
        new RequestService(
            requestRepository, userRepository, studentAccessResolver, academicMapper);

    studentUser =
        User.builder()
            .userId(10L)
            .email("student10@fpt.edu.vn")
            .fullName("Nguyen Van Con")
            .role("STUDENT")
            .build();

    parentUser =
        User.builder()
            .userId(20L)
            .email("parent20@fpt.edu.vn")
            .fullName("Nguyen Van Cha")
            .role("PARENT")
            .build();
  }

  @Test
  @DisplayName(
      "Lỗi #1: StudentAccessResolver - Sinh viên luôn nhận ID của chính mình, chặn IDOR xem trộm")
  void studentAccessResolver_studentAlwaysGetsSelf() {
    Long resolved = studentAccessResolver.resolveTargetStudentId(studentUser, 99L);
    assertEquals(10L, resolved, "Sinh viên phải nhận ID của chính mình dù truyền ID người khác");
  }

  @Test
  @DisplayName("Lỗi #1: StudentAccessResolver - Phụ huynh xem con hợp lệ thành công")
  void studentAccessResolver_parentValidChild_success() {
    when(parentStudentRepository.existsByParent_UserIdAndStudent_UserId(20L, 10L)).thenReturn(true);

    Long resolved = studentAccessResolver.resolveTargetStudentId(parentUser, 10L);
    assertEquals(10L, resolved);
  }

  @Test
  @DisplayName(
      "Lỗi #1: StudentAccessResolver - Phụ huynh xem học sinh không phải con em bị ném AccessDeniedException")
  void studentAccessResolver_parentStrangerChild_throwsAccessDenied() {
    when(parentStudentRepository.existsByParent_UserIdAndStudent_UserId(20L, 99L))
        .thenReturn(false);

    assertThrows(
        AccessDeniedException.class,
        () -> studentAccessResolver.resolveTargetStudentId(parentUser, 99L));
  }

  @Test
  @DisplayName(
      "Lỗi #1: Phụ huynh gửi đơn từ cho con - Lưu đúng requester_id là phụ huynh và student_id là học sinh")
  void createRequest_byParentForChild_savesProperRoles() {
    when(userRepository.findByEmailIgnoreCase("parent20@fpt.edu.vn"))
        .thenReturn(Optional.of(parentUser));
    when(parentStudentRepository.existsByParent_UserIdAndStudent_UserId(20L, 10L)).thenReturn(true);
    when(userRepository.findById(10L)).thenReturn(Optional.of(studentUser));
    when(requestRepository.save(any(RequestEntity.class)))
        .thenAnswer(
            i -> {
              RequestEntity r = i.getArgument(0);
              r.setRequestId(500L);
              return r;
            });
    when(academicMapper.toRequestResponse(any(RequestEntity.class)))
        .thenAnswer(
            i -> {
              RequestEntity r = i.getArgument(0);
              return new RequestResponse(
                  r.getRequestId(),
                  r.getStudent() != null ? r.getStudent().getUserId() : null,
                  r.getStudent() != null ? r.getStudent().getFullName() : "N/A",
                  r.getStudent() != null ? r.getStudent().getRollNumber() : "N/A",
                  r.getRequestType(),
                  r.getTitle(),
                  r.getContent(),
                  r.getAttachmentUrl(),
                  r.getStatus(),
                  r.getResponseNote(),
                  r.getCreatedAt());
            });

    CreateRequestDto dto =
        new CreateRequestDto(10L, "LEAVE_OF_ABSENCE", "Xin nghỉ ốm", "Em bị ốm", null);
    RequestResponse resp = requestService.createRequest("parent20@fpt.edu.vn", dto);

    assertNotNull(resp);
    assertEquals(10L, resp.studentId());
    assertEquals("Nguyen Van Con", resp.studentName());
  }
}
