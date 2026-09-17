package edufit_com_lms.module.lms.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGradeRequest {
    
    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 10, message = "Score cannot be more than 10")
    private Float attendanceScore;

    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 10, message = "Score cannot be more than 10")
    private Float assignmentScore;

    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 10, message = "Score cannot be more than 10")
    private Float midtermScore;

    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 10, message = "Score cannot be more than 10")
    private Float finalScore;

    private String teacherComment;
}
