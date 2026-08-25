package edufit_com_lms.module.lms.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAssignmentRequest {
    private String title;
    private String description;
    private LocalDateTime dueDate;

    @Positive(message = "Max score must be greater than 0")
    private Double maxScore;

    private String attachmentUrl;
}
