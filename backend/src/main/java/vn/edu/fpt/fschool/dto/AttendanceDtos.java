package vn.edu.fpt.fschool.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;

public class AttendanceDtos {

  public record StudentAttendanceEntry(
      @NotNull(message = "ID sinh viên không được để trống")
      Long studentId,

      @NotBlank(message = "Trạng thái điểm danh không được để trống")
      @Pattern(regexp = "^(PRESENT|ABSENT|LATE)$", message = "Trạng thái điểm danh phải là PRESENT, ABSENT hoặc LATE")
      String status,

      String note
  ) {}

  public record TakeAttendanceRequest(
      @NotNull(message = "Danh sách điểm danh không được để trống")
      @Valid
      List<StudentAttendanceEntry> entries
  ) {}

  public record AttendanceItemResponse(
      Long studentId,
      String rollNumber,
      String fullName,
      String status,
      String note
  ) {}

  public record ScheduleAttendanceResponse(
      Long scheduleId,
      Long classId,
      String classCode,
      String courseCode,
      LocalDate date,
      Integer slotNumber,
      List<AttendanceItemResponse> students,
      Integer totalStudents,
      Integer presentCount,
      Integer absentCount,
      Integer lateCount
  ) {}
}
