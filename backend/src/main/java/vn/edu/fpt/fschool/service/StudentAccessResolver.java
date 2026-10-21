package vn.edu.fpt.fschool.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.ClassEnrollmentRepository;
import vn.edu.fpt.fschool.repository.ParentStudentRepository;

/**
 * Thành phần dùng chung xác thực quyền truy cập học sinh và giải quyết đúng studentId mục tiêu cho
 * các nghiệp vụ: Bảng điểm, Đơn từ và Thời khóa biểu.
 */
@Component
@RequiredArgsConstructor
public class StudentAccessResolver {

  private final ParentStudentRepository parentStudentRepository;
  private final ClassEnrollmentRepository classEnrollmentRepository;

  /**
   * Giải quyết studentId mục tiêu dựa trên vai trò của người dùng hiện tại:
   * - STUDENT: Luôn trả về userId của chính mình (chặn IDOR/xem trộm).
   * - PARENT: Bắt buộc truyền requestedStudentId và phải có quan hệ trong bảng parent_students.
   * - LECTURER: Trả về requestedStudentId nếu sinh viên thuộc lớp giảng viên phụ trách.
   * - ADMIN: Trả về requestedStudentId nếu có.
   */
  public Long resolveTargetStudentId(User currentUser, Long requestedStudentId) {
    if (currentUser == null) {
      throw new IllegalArgumentException("Người dùng không hợp lệ");
    }

    String role = currentUser.getRole() != null ? currentUser.getRole().toUpperCase() : "STUDENT";

    if ("STUDENT".equals(role)) {
      // Luôn lấy ID của chính sinh viên, bỏ qua requestedStudentId để chống IDOR
      return currentUser.getUserId();
    }

    if ("PARENT".equals(role)) {
      if (requestedStudentId == null) {
        throw new IllegalArgumentException(
            "Phụ huynh vui lòng cung cấp studentId của con em cần tra cứu");
      }
      boolean hasAccess =
          parentStudentRepository.existsByParent_UserIdAndStudent_UserId(
              currentUser.getUserId(), requestedStudentId);
      if (!hasAccess) {
        throw new AccessDeniedException(
            "Bạn không có quyền giám hộ đối với học sinh này (ID: " + requestedStudentId + ")");
      }
      return requestedStudentId;
    }

    if ("LECTURER".equals(role)) {
      if (requestedStudentId != null) {
        boolean taughtByLecturer =
            classEnrollmentRepository
                .existsByStudent_UserIdAndClassEntity_Lecturer_UserIdAndStatus(
                    requestedStudentId, currentUser.getUserId(), "ENROLLED");
        if (!taughtByLecturer) {
          throw new AccessDeniedException("Sinh viên không thuộc lớp bạn phụ trách");
        }
        return requestedStudentId;
      }
      return currentUser.getUserId();
    }

    // Với ADMIN hoặc các vai trò quản trị khác
    if (requestedStudentId != null) {
      return requestedStudentId;
    }
    return currentUser.getUserId();
  }

  public boolean isParentOf(Long parentId, Long studentId) {
    if (parentId == null || studentId == null) {
      return false;
    }
    return parentStudentRepository.existsByParent_UserIdAndStudent_UserId(parentId, studentId);
  }
}
