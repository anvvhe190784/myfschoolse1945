package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "clubs")
@Getter
@Setter
public class Club {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "club_id")
  private Long clubId;

  @Column(name = "club_name", nullable = false, unique = true)
  private String clubName;

  @Column(name = "description", nullable = false, columnDefinition = "TEXT")
  private String description;

  @Column(name = "category", nullable = false)
  private String
      category; // 'ACADEMIC', 'ARTS', 'SPORTS', 'VOLUNTEER', 'TECHNOLOGY', 'MEDIA', 'OTHER'

  @Column(name = "avatar_url")
  private String avatarUrl;

  @Column(name = "banner_url")
  private String bannerUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "leader_id")
  private User leader;

  @Column(name = "contact_email")
  private String contactEmail;

  @Column(name = "member_count", nullable = false)
  private Integer memberCount = 1;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    if (memberCount == null) memberCount = 1;
    if (isActive == null) isActive = true;
  }
}
