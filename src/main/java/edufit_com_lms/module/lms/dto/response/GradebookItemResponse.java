package edufit_com_lms.module.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradebookItemResponse {
    private UUID id;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String className;
    private String courseName;
    
    private Float attendanceScore;
    private Float assignmentScore;
    private Float midtermScore;
    private Float finalScore;
    private Float averageScore;
    private String teacherComment;
    
    private UUID classId;
}
