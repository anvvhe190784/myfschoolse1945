package vn.edu.fpt.fschool.event;

public class NotificationEvents {

  public record PasswordChangedEvent(Long userId, String email) {}

  public record NewsPublishedEvent(Long newsId, String title, String summary) {}
}
