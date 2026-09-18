package edufit_com_lms.module.notification.service.impl;

import edufit_com_lms.module.notification.entity.AppNotification;
import edufit_com_lms.module.notification.repository.AppNotificationRepository;
import edufit_com_lms.module.notification.service.AppNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppNotificationServiceImpl implements AppNotificationService {

    private final AppNotificationRepository repository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Override
    public org.springframework.data.domain.Page<edufit_com_lms.module.notification.dto.response.AppNotificationResponse> getAllNotifications(Long userId, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<AppNotification> page;
        if (userId != null) {
            page = repository.findByRecipientIdOrNullOrderByCreatedAtDesc(userId, pageable);
        } else {
            page = repository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return page.map(this::mapToResponse);
    }

    private edufit_com_lms.module.notification.dto.response.AppNotificationResponse mapToResponse(AppNotification notification) {
        return edufit_com_lms.module.notification.dto.response.AppNotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .relatedCourseId(notification.getRelatedCourseId())
                .relatedLessionId(notification.getRelatedLessionId())
                .senderId(notification.getSenderId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    @Override
    public AppNotification createNotification(String title, String message, String type, UUID relatedCourseId,
            UUID relatedLessionId, Long senderId, Long recipientId) {
        AppNotification notification = AppNotification.builder()
                .title(title)
                .message(message)
                .type(type)
                .relatedCourseId(relatedCourseId)
                .relatedLessionId(relatedLessionId)
                .senderId(senderId)
                .recipientId(recipientId)
                .isRead(false)
                .build();
        AppNotification saved = repository.save(notification);
        
        // Broadcast over WebSocket
        if (recipientId != null) {
            messagingTemplate.convertAndSendToUser(recipientId.toString(), "/queue/notifications", mapToResponse(saved));
        } else {
            messagingTemplate.convertAndSend("/topic/notifications", mapToResponse(saved));
        }
        
        return saved;
    }

    @Override
    public void markAsRead(UUID id, Long userId) {
        repository.findById(id).ifPresent(notif -> {
            // Either broadcast (null recipient) or direct to this user
            if (notif.getRecipientId() == null || notif.getRecipientId().equals(userId)) {
                notif.setIsRead(true);
                repository.save(notif);
            } else {
                throw new edufit_com_lms.common.exception.BadRequestException("You do not have permission to mark this notification as read");
            }
        });
    }

    @Override
    public long getUnreadCount(Long userId) {
        if (userId != null) {
            return repository.countUnreadByRecipientId(userId);
        }
        return repository.countByRecipientIdIsNullAndIsReadFalse();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void markAllAsRead(Long userId) {
        if (userId != null) {
            repository.markAllAsRead(userId);
        } else {
            repository.markAllAsReadForNullRecipient();
        }
    }
}
