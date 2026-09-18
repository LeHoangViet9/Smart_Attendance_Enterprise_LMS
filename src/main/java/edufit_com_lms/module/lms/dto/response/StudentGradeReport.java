package edufit_com_lms.module.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentGradeReport {
    private Long studentId;
    private String studentCode;
    private String fullName;
    
    // Detailed scores
    private Double attendanceScore; // 10%
    private Double assignmentScore; // 10% (Trung bình các bài tập)
    private Double midtermScore;    // 30% (isExam = false, nhưng có thể đánh dấu là Giữa kỳ. Tạm lấy bài Exam đầu tiên làm giữa kỳ nếu có nhiều bài exam, hoặc cần filter)
    private Double finalScore;      // 50%
    
    // Map of assignmentId -> achieved score
    private Map<UUID, Double> assignmentScores;
    
    // Calculated total
    private Double totalScore; // Tính theo công thức: (Chuyên cần + Bài tập + Giữa kỳ * 3 + Cuối kỳ * 5) / 10
}
