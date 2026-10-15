package vn.edu.fpt.fschool.controller;

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
public class NewsController {

  private final NewsService newsService;

  public NewsController(NewsService newsService) {
    this.newsService = newsService;
  }

  @GetMapping
  public ResponseEntity<List<NewsResponse>> getAllNews(
      @RequestParam(required = false) Integer limit) {
    return ResponseEntity.ok(newsService.getAllPublishedNews(limit));
  }

  @GetMapping("/{id}")
  public ResponseEntity<NewsResponse> getNewsDetail(@PathVariable Long id) {
    return ResponseEntity.ok(newsService.getNewsDetail(id));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<NewsResponse> createNews(
      Authentication authentication, @Valid @RequestBody CreateNewsRequest req) {
    String adminEmail = authentication.getName();
    return ResponseEntity.status(HttpStatus.CREATED).body(newsService.createNews(req, adminEmail));
  }
}
