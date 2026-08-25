package edufit_com_lms.module.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreateAssignmentRequest {
    @NotNull(message = "classId must not be null")
    private UUID classId;

    @NotBlank(message = "Title must not be blank")
    private String title;

    private String description;

    @NotNull(message = "Due date must not be null")
    private LocalDateTime dueDate;

    @Positive(message = "Max score must be greater than 0")
    private Double maxScore;

    private String attachmentUrl;
}
