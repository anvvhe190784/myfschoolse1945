package vn.edu.fpt.fschool.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleDtos {
  public record ScheduleResponse(
      Long scheduleId,
      Long classId,
      String classCode,
      String courseCode,
      String courseName,
      Integer slotNumber,
      Integer sessionNo,
      LocalDate date,
      LocalTime startTime,
      LocalTime endTime,
      String room,
      String lecturerName,
      String note,
      String status,
      String semester) {

    public ScheduleResponse(
        Long scheduleId,
        Long classId,
        String classCode,
        String courseCode,
        String courseName,
        Integer slotNumber,
        Integer sessionNo,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String room,
        String lecturerName,
        String note,
        String status) {
      this(
          scheduleId,
          classId,
          classCode,
          courseCode,
          courseName,
          slotNumber,
          sessionNo,
          date,
          startTime,
          endTime,
          room,
          lecturerName,
          note,
          status,
          null);
    }
  }

  public record SemesterResponse(String code, String name) {}

  public record CreateScheduleRequest(
      @NotNull(message = "Class ID không được để trống") Long classId,
      @NotNull(message = "Slot number không được để trống") Integer slotNumber,
      Integer sessionNo,
      @NotNull(message = "Ngày học không được để trống") LocalDate date,
      @NotNull(message = "Giờ bắt đầu không được để trống") LocalTime startTime,
      @NotNull(message = "Giờ kết thúc không được để trống") LocalTime endTime,
      @NotBlank(message = "Phòng học không được để trống") String room,
      Long lecturerId,
      String note) {}
}
