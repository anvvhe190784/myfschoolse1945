package vn.edu.fpt.fschool.event;

public record RequestProcessedEvent(
    Long requestId,
    Long requesterId,
    String title,
    String oldStatus,
    String newStatus,
    String responseNote) {}
