package vn.edu.fpt.fschool.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.fschool.dto.GradeDtos.GradeResponse;
import vn.edu.fpt.fschool.model.Grade;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.GradeRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

@Service
public class GradeService {

  private final GradeRepository gradeRepository;
  private final UserRepository userRepository;
  private final StudentAccessResolver studentAccessResolver;
  private final vn.edu.fpt.fschool.mapper.AcademicMapper academicMapper;

  public GradeService(
      GradeRepository gradeRepository,
      UserRepository userRepository,
      StudentAccessResolver studentAccessResolver,
      vn.edu.fpt.fschool.mapper.AcademicMapper academicMapper) {
    this.gradeRepository = gradeRepository;
    this.userRepository = userRepository;
    this.studentAccessResolver = studentAccessResolver;
    this.academicMapper = academicMapper;
  }

  @Transactional(readOnly = true)
  public List<GradeResponse> getMyGrades(String email, Long classId, Long requestedStudentId) {
    return findMyGrades(email, classId, requestedStudentId);
  }

  @Transactional(readOnly = true)
  public List<GradeResponse> getMyGrades(String email, Long classId) {
    return findMyGrades(email, classId, null);
  }

  private List<GradeResponse> findMyGrades(String email, Long classId, Long requestedStudentId) {
    User currentUser =
        userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

    Long targetStudentId =
        studentAccessResolver.resolveTargetStudentId(currentUser, requestedStudentId);

    List<Grade> grades =
        (classId != null)
            ? gradeRepository.findByStudent_UserIdAndClassEntity_ClassId(targetStudentId, classId)
            : gradeRepository.findByStudent_UserId(targetStudentId);

    return academicMapper.toGradeResponseList(grades);
  }

  @Transactional(readOnly = true)
  public GradeResponse getGradeDetail(Long gradeId, String email) {
    Grade grade =
        gradeRepository
            .findById(gradeId)
            .orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy điểm số với ID: " + gradeId));

    if (email != null) {
      User currentUser =
          userRepository
              .findByEmailIgnoreCase(email)
              .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

      boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
      boolean isOwner =
          grade.getStudent() != null
              && grade.getStudent().getUserId().equals(currentUser.getUserId());
      boolean isTeacherOfClass =
          grade.getClassEntity() != null
              && grade.getClassEntity().getLecturer() != null
              && grade.getClassEntity().getLecturer().getUserId().equals(currentUser.getUserId());
      boolean isParentOfStudent =
          "PARENT".equalsIgnoreCase(currentUser.getRole())
              && grade.getStudent() != null
              && studentAccessResolver.isParentOf(
                  currentUser.getUserId(), grade.getStudent().getUserId());

      if (!isAdmin && !isOwner && !isTeacherOfClass && !isParentOfStudent) {
        throw new org.springframework.security.access.AccessDeniedException(
            "Bạn không có quyền truy cập đầu điểm này");
      }
    }

    return academicMapper.toGradeResponse(grade);
  }
}
