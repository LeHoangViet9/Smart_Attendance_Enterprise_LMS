package edufit_com_lms.module.lms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGradebookRequest {
    private Float attendanceScore;
    private Float assignmentScore;
    private Float midtermScore;
    private Float finalScore;
    private String teacherComment;
}
