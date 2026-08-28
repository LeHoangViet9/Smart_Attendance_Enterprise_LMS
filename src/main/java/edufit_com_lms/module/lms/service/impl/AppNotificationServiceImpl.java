package edufit_com_lms.module.lms.service.impl;

import edufit_com_lms.module.lms.entity.AppNotification;
import edufit_com_lms.module.lms.repository.AppNotificationRepository;
import edufit_com_lms.module.lms.service.AppNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppNotificationServiceImpl implements AppNotificationService {

    private final AppNotificationRepository repository;

    @Override
    public List<AppNotification> getAllNotifications() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public AppNotification createNotification(String title, String message, String type, UUID relatedCourseId,
            UUID relatedLessionId, Long senderId) {
        AppNotification notification = AppNotification.builder()
                .title(title)
                .message(message)
                .type(type)
                .relatedCourseId(relatedCourseId)
                .relatedLessionId(relatedLessionId)
                .senderId(senderId)
                .isRead(false)
                .build();
        return repository.save(notification);
    }

    @Override
    public void markAsRead(UUID id) {
        repository.findById(id).ifPresent(notif -> {
            notif.setIsRead(true);
            repository.save(notif);
        });
    }
}
