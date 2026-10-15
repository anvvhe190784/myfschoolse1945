package vn.edu.fpt.fschool.model;

public enum RequestStatus {
  PENDING,
  PROCESSING,
  APPROVED,
  REJECTED,
  CANCELLED;

  public static RequestStatus fromString(String status) {
    if (status == null || status.isBlank()) {
      throw new IllegalArgumentException("Trạng thái đơn từ không được để trống");
    }
    try {
      return RequestStatus.valueOf(status.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Trạng thái đơn từ không hợp lệ: " + status);
    }
  }

  public boolean canTransitionTo(RequestStatus target) {
    if (this == target) {
      return true;
    }
    return switch (this) {
      case PENDING ->
          target == PROCESSING || target == APPROVED || target == REJECTED || target == CANCELLED;
      case PROCESSING -> target == APPROVED || target == REJECTED || target == CANCELLED;
      case APPROVED, REJECTED, CANCELLED -> false;
    };
  }
}
