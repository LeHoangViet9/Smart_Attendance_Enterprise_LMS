package edufit_com_lms.module.lms.event;

import edufit_com_lms.module.lms.service.AppNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final AppNotificationService appNotificationService;

    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        // Lưu thông báo dạng bất đồng bộ hoặc đồng bộ tuỳ config (mặc định đồng bộ)
        appNotificationService.createNotification(
                event.getTitle(),
                event.getMessage(),
                event.getType(),
                event.getRelatedCourseId(),
                event.getRelatedLessionId(),
                event.getSenderId());
    }
}
