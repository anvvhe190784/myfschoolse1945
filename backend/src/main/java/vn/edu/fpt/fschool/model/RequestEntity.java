package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "requests")
public class RequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Column(name = "request_type", nullable = false)
    private String requestType; // 'ATTENDANCE_EXEMPTION', 'COURSE_WITHDRAWAL', 'LEAVE_OF_ABSENCE', 'GRADE_REVIEW', 'CERTIFICATE_REQUEST', 'OTHER'

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // 'PENDING', 'PROCESSING', 'APPROVED', 'REJECTED'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handler_id")
    private User handler;

    @Column(name = "response_note", columnDefinition = "TEXT")
    private String responseNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
        if (status == null) status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }

    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getHandler() { return handler; }
    public void setHandler(User handler) { this.handler = handler; }

    public String getResponseNote() { return responseNote; }
    public void setResponseNote(String responseNote) { this.responseNote = responseNote; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
