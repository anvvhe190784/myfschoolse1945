package vn.edu.fpt.fschool.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.*;

@Entity
@Table(
    name = "parent_students",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_parent_student",
          columnNames = {"parent_id", "student_id"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentStudent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id", nullable = false)
  private User parent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private User student;

  @Column(name = "relationship_type", nullable = false)
  @Builder.Default
  private String relationshipType = "PARENT";

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
    if (relationshipType == null) {
      relationshipType = "PARENT";
    }
  }
}
