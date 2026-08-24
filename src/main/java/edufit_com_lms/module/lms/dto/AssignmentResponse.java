package edufit_com_lms.module.lms.dto;

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
public class AssignmentResponse {
    private UUID id;
    private UUID classId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Double maxScore;
    private String attachmentUrl;
    private LocalDateTime createdAt;
    private Boolean isExpired;
}
