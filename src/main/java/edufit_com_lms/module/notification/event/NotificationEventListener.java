package edufit_com_lms.module.notification.event;

import edufit_com_lms.module.notification.service.AppNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final AppNotificationService appNotificationService;

    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        // LÃ†Â°u thÃƒÂ´ng bÃƒÂ¡o dÃ¡ÂºÂ¡ng bÃ¡ÂºÂ¥t Ã„â€˜Ã¡Â»â€œng bÃ¡Â»â„¢ hoÃ¡ÂºÂ·c Ã„â€˜Ã¡Â»â€œng bÃ¡Â»â„¢ tuÃ¡Â»Â³ config (mÃ¡ÂºÂ·c Ã„â€˜Ã¡Â»â€¹nh Ã„â€˜Ã¡Â»â€œng bÃ¡Â»â„¢)
        appNotificationService.createNotification(
                event.getTitle(),
                event.getMessage(),
                event.getType(),
                event.getRelatedCourseId(),
                event.getRelatedLessonId(),
                event.getSenderId(),
                event.getRecipientId());
    }
}
