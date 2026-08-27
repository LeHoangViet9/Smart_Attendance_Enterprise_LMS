package edufit_com_lms.module.lms.service;

import edufit_com_lms.module.lms.entity.AppNotification;

import java.util.List;
import java.util.UUID;

public interface AppNotificationService {
    List<AppNotification> getAllNotifications();

    AppNotification createNotification(String title, String message, String type, UUID relatedCourseId,
            UUID relatedLessionId, Long senderId);

    void markAsRead(UUID id);
}
