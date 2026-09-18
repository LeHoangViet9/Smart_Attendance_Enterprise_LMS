package edufit_com_lms.module.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppNotificationResponse {
    private UUID id;
    private String title;
    private String message;
    private String type;
    private UUID relatedCourseId;
    private UUID relatedLessionId;
    private Long senderId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
