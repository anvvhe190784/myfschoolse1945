package vn.edu.fpt.fschool.repository;

import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.NotificationRead;

@Repository
public interface NotificationReadRepository extends JpaRepository<NotificationRead, Long> {

  boolean existsByNotification_NotificationIdAndUser_UserId(Long notificationId, Long userId);

  @Query(
      "SELECT nr.notification.notificationId FROM NotificationRead nr WHERE nr.user.userId = :userId")
  Set<Long> findReadNotificationIdsByUserId(@Param("userId") Long userId);

  void deleteByNotification_NotificationIdAndUser_UserId(Long notificationId, Long userId);
}
