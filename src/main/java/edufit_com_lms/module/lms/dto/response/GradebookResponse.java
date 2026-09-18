package edufit_com_lms.module.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradebookResponse {
    private UUID classId;
    private String className;
    private List<AssignmentSummary> assignmentHeaders;
    private List<StudentGradeReport> studentReports;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentSummary {
        private UUID assignmentId;
        private String title;
        private Boolean isExam;
        private Double maxScore;
    }
}
