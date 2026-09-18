package edufit_com_lms.module.notification.repository;

import edufit_com_lms.module.notification.entity.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, UUID> {
    org.springframework.data.domain.Page<AppNotification> findAllByOrderByCreatedAtDesc(org.springframework.data.domain.Pageable pageable);
    
    @org.springframework.data.jpa.repository.Query("SELECT n FROM AppNotification n WHERE n.recipientId = :recipientId OR n.recipientId IS NULL ORDER BY n.createdAt DESC")
    org.springframework.data.domain.Page<AppNotification> findByRecipientIdOrNullOrderByCreatedAtDesc(
            @org.springframework.data.repository.query.Param("recipientId") Long recipientId,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(n) FROM AppNotification n WHERE (n.recipientId = :recipientId OR n.recipientId IS NULL) AND n.isRead = false")
    long countUnreadByRecipientId(@org.springframework.data.repository.query.Param("recipientId") Long recipientId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE AppNotification n SET n.isRead = true WHERE (n.recipientId = :recipientId OR n.recipientId IS NULL) AND n.isRead = false")
    void markAllAsRead(@org.springframework.data.repository.query.Param("recipientId") Long recipientId);

    long countByRecipientIdIsNullAndIsReadFalse();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE AppNotification n SET n.isRead = true WHERE n.recipientId IS NULL AND n.isRead = false")
    void markAllAsReadForNullRecipient();
}
