package vn.edu.fpt.fschool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.fschool.dto.NewsDtos.CreateNewsRequest;
import vn.edu.fpt.fschool.dto.NewsDtos.NewsResponse;
import vn.edu.fpt.fschool.service.NewsService;

@RestController
@RequestMapping("/api/v1/news")
@Tag(name = "6. News (Tin tức & Sự kiện)", description = "Bảng tin thông báo và sự kiện nhà trường")
public class NewsController {

  private final NewsService newsService;

  public NewsController(NewsService newsService) {
    this.newsService = newsService;
  }

  @GetMapping
  @Operation(summary = "Danh sách tin tức (Use Case 8: ListNews)", description = "Xem danh sách các bài viết tin tức và thông báo mới nhất")
  public ResponseEntity<List<NewsResponse>> getAllNews(@RequestParam(required = false) Integer limit) {
    return ResponseEntity.ok(newsService.getAllPublishedNews(limit));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Chi tiết tin tức (Use Case 13: ViewNewsDetail)", description = "Đọc toàn bộ nội dung bài viết tin tức và tự động tăng lượt xem")
  public ResponseEntity<NewsResponse> getNewsDetail(@PathVariable Long id) {
    return ResponseEntity.ok(newsService.getNewsDetail(id));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Tạo tin tức mới (Dành riêng cho ADMIN)", description = "Admin đăng bài viết tin tức mới và tự động kích hoạt thông báo toàn trường")
  public ResponseEntity<NewsResponse> createNews(
      Authentication authentication,
      @Valid @RequestBody CreateNewsRequest req
  ) {
    String adminEmail = authentication.getName();
    return ResponseEntity.status(HttpStatus.CREATED).body(newsService.createNews(req, adminEmail));
  }
}
