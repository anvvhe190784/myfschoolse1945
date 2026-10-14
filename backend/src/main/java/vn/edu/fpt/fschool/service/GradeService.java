package vn.edu.fpt.fschool.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.fschool.dto.GradeDtos.GradeResponse;
import vn.edu.fpt.fschool.model.Grade;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.GradeRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

import java.util.List;

@Service
public class GradeService {

    private final GradeRepository gradeRepository;
    private final UserRepository userRepository;

    public GradeService(GradeRepository gradeRepository, UserRepository userRepository) {
        this.gradeRepository = gradeRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getMyGrades(String email, Long classId) {
        User student = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên"));

        List<Grade> grades = (classId != null)
                ? gradeRepository.findByStudent_UserIdAndClassEntity_ClassId(student.getUserId(), classId)
                : gradeRepository.findByStudent_UserId(student.getUserId());

        return grades.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public GradeResponse getGradeDetail(Long gradeId, String email) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy điểm số với ID: " + gradeId));

        if (email != null) {
            User currentUser = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
            boolean isOwner = grade.getStudent() != null && grade.getStudent().getUserId().equals(currentUser.getUserId());
            boolean isTeacherOfClass = grade.getClassEntity() != null
                    && grade.getClassEntity().getLecturer() != null
                    && grade.getClassEntity().getLecturer().getUserId().equals(currentUser.getUserId());

            if (!isAdmin && !isOwner && !isTeacherOfClass) {
                throw new org.springframework.security.access.AccessDeniedException("Bạn không có quyền truy cập đầu điểm này");
            }
        }

        return toResponse(grade);
    }

    private GradeResponse toResponse(Grade g) {
        String classCode = g.getClassEntity() != null ? g.getClassEntity().getClassCode() : "N/A";
        String courseName = (g.getClassEntity() != null && g.getClassEntity().getCourse() != null)
                ? g.getClassEntity().getCourse().getCourseName() : "N/A";

        return new GradeResponse(
                g.getGradeId(),
                g.getClassEntity() != null ? g.getClassEntity().getClassId() : null,
                classCode,
                courseName,
                g.getAssessmentName(),
                g.getWeight(),
                g.getScore(),
                g.getFeedback(),
                g.getStatus()
        );
    }
}
