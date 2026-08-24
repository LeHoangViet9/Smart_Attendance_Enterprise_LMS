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
public class SubmissionResponse {
    private UUID id;
    private UUID assignmentId;
    private UUID studentId;
    private String fileUrl;
    private LocalDateTime submittedAt;
    private Double score;
    private String feedback;
    private Boolean isGraded;
    private Boolean isLate;
}
