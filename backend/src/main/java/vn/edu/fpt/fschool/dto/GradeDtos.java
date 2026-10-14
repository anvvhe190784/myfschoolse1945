package vn.edu.fpt.fschool.dto;

import java.math.BigDecimal;

public class GradeDtos {
  public record GradeResponse(
      Long gradeId,
      Long classId,
      String classCode,
      String courseName,
      String assessmentName,
      BigDecimal weight,
      BigDecimal score,
      String feedback,
      String status) {}
}
