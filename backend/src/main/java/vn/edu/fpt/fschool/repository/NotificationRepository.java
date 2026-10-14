package vn.edu.fpt.fschool.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.user WHERE n.user.userId = :userId ORDER BY n.createdAt DESC")
  List<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

  @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.userId = :userId AND n.isRead = false")
  long countByUserIdAndIsReadFalse(@Param("userId") Long userId);

  Optional<Notification> findByNotificationIdAndUser_UserId(Long notificationId, Long userId);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.userId = :userId AND n.isRead = false")
  int markAllAsReadByUserId(@Param("userId") Long userId);
}
