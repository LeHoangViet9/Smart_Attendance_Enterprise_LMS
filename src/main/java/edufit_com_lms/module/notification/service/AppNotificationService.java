package edufit_com_lms.module.notification.service;

import edufit_com_lms.module.notification.dto.response.AppNotificationResponse;
import edufit_com_lms.module.notification.entity.AppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AppNotificationService {
    Page<AppNotificationResponse> getAllNotifications(Long userId, Pageable pageable);

    AppNotification createNotification(String title, String message, String type, UUID relatedCourseId,
            UUID relatedLessonId, Long senderId, Long recipientId);

    void markAsRead(UUID id, Long userId);
    
    long getUnreadCount(Long userId);

    void markAllAsRead(Long userId);
}
