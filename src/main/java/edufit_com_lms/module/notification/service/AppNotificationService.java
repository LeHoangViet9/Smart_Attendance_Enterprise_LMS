package edufit_com_lms.module.notification.service;

import edufit_com_lms.module.notification.entity.AppNotification;

import java.util.UUID;

public interface AppNotificationService {
    org.springframework.data.domain.Page<edufit_com_lms.module.notification.dto.response.AppNotificationResponse> getAllNotifications(Long userId, org.springframework.data.domain.Pageable pageable);

    AppNotification createNotification(String title, String message, String type, UUID relatedCourseId,
            UUID relatedLessionId, Long senderId, Long recipientId);

    void markAsRead(UUID id, Long userId);
    
    long getUnreadCount(Long userId);

    void markAllAsRead(Long userId);
}
