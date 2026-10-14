package vn.edu.fpt.fschool.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

public class NewsDtos {

  public record CreateNewsRequest(
      @NotBlank(message = "Tiêu đề không được để trống") String title,
      @NotBlank(message = "Tóm tắt không được để trống") String summary,
      @NotBlank(message = "Nội dung bài viết không được để trống") String content,
      String thumbnailUrl,
      String category) {}

  public record NewsResponse(
      Long newsId,
      String title,
      String summary,
      String content,
      String thumbnailUrl,
      String authorName,
      String category,
      Integer viewsCount,
      OffsetDateTime publishedAt) {}
}
