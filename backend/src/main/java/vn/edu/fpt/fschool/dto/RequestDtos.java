package vn.edu.fpt.fschool.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

public class RequestDtos {
  public record RequestResponse(
      Long requestId,
      Long studentId,
      String studentName,
      String studentRollNumber,
      String requestType,
      String title,
      String content,
      String attachmentUrl,
      String status,
      String responseNote,
      OffsetDateTime createdAt) {}

  public record CreateRequestDto(
      Long studentId,
      @NotBlank(message = "Loại đơn không được để trống") String requestType,
      @NotBlank(message = "Tiêu đề không được để trống") String title,
      @NotBlank(message = "Nội dung không được để trống") String content,
      String attachmentUrl) {}

  public record UpdateRequestStatusDto(
      @NotBlank(message = "Trạng thái không được để trống") String status, // 'APPROVED', 'REJECTED'
      String responseNote) {}
}
