package edufit_com_lms.module.lms.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeSubmissionRequest {
    @NotNull(message = "Score must not be null")
    @Min(value = 0, message = "Score must not be less than 0")
    private Double score;

    private String feedback;
}
