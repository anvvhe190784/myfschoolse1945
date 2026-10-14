package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "clubs")
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
    if (createdAt == null) createdAt = OffsetDateTime.now();
    if (memberCount == null) memberCount = 1;
    if (isActive == null) isActive = true;
  }

  public Long getClubId() {
    return clubId;
  }

  public void setClubId(Long clubId) {
    this.clubId = clubId;
  }

  public String getClubName() {
    return clubName;
  }

  public void setClubName(String clubName) {
    this.clubName = clubName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public String getBannerUrl() {
    return bannerUrl;
  }

  public void setBannerUrl(String bannerUrl) {
    this.bannerUrl = bannerUrl;
  }

  public User getLeader() {
    return leader;
  }

  public void setLeader(User leader) {
    this.leader = leader;
  }

  public String getContactEmail() {
    return contactEmail;
  }

  public void setContactEmail(String contactEmail) {
    this.contactEmail = contactEmail;
  }

  public Integer getMemberCount() {
    return memberCount;
  }

  public void setMemberCount(Integer memberCount) {
    this.memberCount = memberCount;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
