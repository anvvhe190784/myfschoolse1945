package vn.edu.fpt.fschool.exception;

import java.net.URI;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    problem.setTitle("Bad Request");
    problem.setType(URI.create("https://fschool.edu.vn/errors/bad-request"));
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }

  @ExceptionHandler(IllegalStateException.class)
  public ProblemDetail handleIllegalState(IllegalStateException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    problem.setTitle("Resource State Conflict");
    problem.setType(URI.create("https://fschool.edu.vn/errors/conflict"));
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED, "Tài khoản hoặc mật khẩu không chính xác");
    problem.setTitle("Unauthorized");
    problem.setType(URI.create("https://fschool.edu.vn/errors/unauthorized"));
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền thực hiện thao tác này (Yêu cầu quyền ADMIN)");
    problem.setTitle("Forbidden");
    problem.setType(URI.create("https://fschool.edu.vn/errors/forbidden"));
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }

  @ExceptionHandler(io.github.resilience4j.ratelimiter.RequestNotPermitted.class)
  public ProblemDetail handleRateLimit(io.github.resilience4j.ratelimiter.RequestNotPermitted ex) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.TOO_MANY_REQUESTS,
            "Hệ thống đang bận hoặc bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau giây lát.");
    problem.setTitle("Too Many Requests");
    problem.setType(URI.create("https://fschool.edu.vn/errors/too-many-requests"));
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }

  @ExceptionHandler(org.springframework.mail.MailException.class)
  public ProblemDetail handleMailException(org.springframework.mail.MailException ex) {
    log.error("Mail service exception: {}", ex.getMessage());
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Máy chủ gửi email đang gặp sự cố. Vui lòng thử lại sau giây lát.");
    problem.setTitle("Mail Service Unavailable");
    problem.setType(URI.create("https://fschool.edu.vn/errors/mail-service-unavailable"));
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneralException(Exception ex) {
    log.error("Unhandled server exception: {}", ex.getMessage(), ex);
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Đã xảy ra lỗi máy chủ nội bộ. Vui lòng liên hệ quản trị viên.");
    problem.setTitle("Internal Server Error");
    problem.setType(URI.create("https://fschool.edu.vn/errors/server-error"));
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }
}
