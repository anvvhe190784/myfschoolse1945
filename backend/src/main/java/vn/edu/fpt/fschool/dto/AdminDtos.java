package vn.edu.fpt.fschool.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AdminDtos {
    public record CreateUserRequest(
            String rollNumber,
            @NotBlank(message = "Email không được để trống")
            @Email(message = "Email không hợp lệ")
            String email,
            @NotBlank(message = "Mật khẩu không được để trống")
            String password,
            @NotBlank(message = "Họ tên không được để trống")
            String fullName,
            String phoneNumber,
            @NotBlank(message = "Role không được để trống")
            String role // 'STUDENT', 'LECTURER', 'PARENT', 'ADMIN'
    ) {}

    public record UserResponse(
            Long userId,
            String rollNumber,
            String email,
            String fullName,
            String phoneNumber,
            String avatarUrl,
            String role,
            String status
    ) {}

    public record UpdateUserStatusRequest(
            @NotBlank(message = "Trạng thái không được để trống")
            String status // 'ACTIVE', 'INACTIVE', 'LOCKED'
    ) {}

    public record CreateCourseRequest(
            @NotBlank(message = "Mã môn không được để trống")
            String courseCode,
            @NotBlank(message = "Tên môn không được để trống")
            String courseName,
            Integer credits,
            String description
    ) {}

    public record CourseResponse(
            Long courseId,
            String courseCode,
            String courseName,
            Integer credits,
            String description
    ) {}

    public record CreateClassRequest(
            @NotBlank(message = "Mã lớp không được để trống")
            String classCode,
            Long courseId,
            Long lecturerId,
            @NotBlank(message = "Học kỳ không được để trống")
            String semester,
            Integer academicYear,
            String roomDefault
    ) {}

    public record ClassResponse(
            Long classId,
            String classCode,
            String courseCode,
            String courseName,
            String lecturerName,
            String semester,
            Integer academicYear,
            String roomDefault
    ) {}
}
