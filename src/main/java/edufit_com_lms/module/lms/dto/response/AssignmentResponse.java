package edufit_com_lms.module.lms.dto.response;

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
    private java.util.List<UUID> classIds;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Double maxScore;
    private String attachmentUrl;
    private Boolean isExam;
    private Boolean isPublished;
    private LocalDateTime createdAt;
    private Boolean isExpired;
    private java.util.List<String> classNames;
}
