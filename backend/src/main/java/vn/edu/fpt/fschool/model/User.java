package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long userId;

  @Column(name = "roll_number", unique = true)
  private String rollNumber;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(name = "phone_number")
  private String phoneNumber;

  @Column(name = "avatar_url")
  private String avatarUrl;

  @Column(name = "role", nullable = false)
  private String role; // 'STUDENT', 'LECTURER', 'PARENT', 'ADMIN'

  @Column(name = "status", nullable = false)
  private String status; // 'ACTIVE', 'INACTIVE', 'LOCKED'

  @Column(name = "token_version", nullable = false)
  @Builder.Default
  private Integer tokenVersion = 1;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = OffsetDateTime.now();
    if (updatedAt == null) updatedAt = OffsetDateTime.now();
    if (status == null) status = "ACTIVE";
    if (role == null) role = "STUDENT";
    if (tokenVersion == null) tokenVersion = 1;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = OffsetDateTime.now();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User other)) return false;
    return userId != null && userId.equals(other.getUserId());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
