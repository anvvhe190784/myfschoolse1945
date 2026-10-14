package vn.edu.fpt.fschool.dto;

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
            String status
    ) {}

    public record CreateScheduleRequest(
            Long classId,
            Integer slotNumber,
            Integer sessionNo,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String room,
            Long lecturerId,
            String note
    ) {}
}
