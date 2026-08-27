package edufit_com_lms.module.lms.event;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class NotificationEvent {
    private String title;
    private String message;
    private String type; // "SYSTEM_LOG", "REPORT"
    private UUID relatedCourseId;
    private UUID relatedLessionId;
    private Long senderId;
}
