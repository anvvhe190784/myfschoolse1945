package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "club_memberships",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_club_user",
          columnNames = {"club_id", "user_id"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubMembership {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "membership_id")
  private Long membershipId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "club_id", nullable = false)
  private Club club;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "role", nullable = false)
  @Builder.Default
  private String role = "MEMBER";

  @Column(name = "joined_at", nullable = false, updatable = false)
  private OffsetDateTime joinedAt;

  @PrePersist
  protected void onCreate() {
    if (joinedAt == null) {
      joinedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
    if (role == null) {
      role = "MEMBER";
    }
  }
}
