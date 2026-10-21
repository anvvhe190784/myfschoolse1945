package vn.edu.fpt.fschool.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AdminDtos {
  public record CreateUserRequest(
      String rollNumber,
      @NotBlank(message = "Email không được để trống") @Email(message = "Email không hợp lệ")
          String email,
      @NotBlank(message = "Mật khẩu không được để trống")
          @Size(min = 6, max = 50, message = "Mật khẩu phải từ 6 đến 50 ký tự")
          String password,
      @NotBlank(message = "Họ tên không được để trống") String fullName,
      String phoneNumber,
      @NotBlank(message = "Role không được để trống")
          @Pattern(
              regexp = "^(STUDENT|LECTURER|PARENT|ADMIN)$",
              message = "Role không hợp lệ. Phải là STUDENT, LECTURER, PARENT hoặc ADMIN")
          String role) {}

  public record UserResponse(
      Long userId,
      String rollNumber,
      String email,
      String fullName,
      String phoneNumber,
      String avatarUrl,
      String role,
      String status,
      Boolean mustChangePassword) {}

  public record UpdateUserStatusRequest(
      @NotBlank(message = "Trạng thái không được để trống")
          @Pattern(
              regexp = "^(ACTIVE|INACTIVE|LOCKED)$",
              message = "Trạng thái không hợp lệ. Phải là ACTIVE, INACTIVE hoặc LOCKED")
          String status) {}

  public record CreateCourseRequest(
      @NotBlank(message = "Mã môn không được để trống") String courseCode,
      @NotBlank(message = "Tên môn không được để trống") String courseName,
      Integer credits,
      String description) {}

  public record CourseResponse(
      Long courseId, String courseCode, String courseName, Integer credits, String description) {}

  public record CreateClassRequest(
      @NotBlank(message = "Mã lớp không được để trống") String classCode,
      @NotNull(message = "Course ID không được để trống") Long courseId,
      Long lecturerId,
      @NotBlank(message = "Học kỳ không được để trống") String semester,
      Integer academicYear,
      String roomDefault) {}

  public record ClassResponse(
      Long classId,
      String classCode,
      String courseCode,
      String courseName,
      String lecturerName,
      String semester,
      Integer academicYear,
      String roomDefault) {}
}
