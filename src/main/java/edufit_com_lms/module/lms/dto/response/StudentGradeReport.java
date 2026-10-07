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
    private Double assignmentScore; // 10% (Trung bÃ¬nh cÃ¡c bÃ i táº­p)
    private Double midtermScore;    // 30% (isExam = false, nhÆ°ng cÃ³ thá»ƒ Ä‘Ã¡nh dáº¥u lÃ  Giá»¯a ká»³. Táº¡m láº¥y bÃ i Exam Ä‘áº§u tiÃªn lÃ m giá»¯a ká»³ náº¿u cÃ³ nhiá»u bÃ i exam, hoáº·c cáº§n filter)
    private Double finalScore;      // 50%
    
    // Map of assignmentId -> achieved score
    private Map<UUID, Double> assignmentScores;
    
    // Calculated total
    private Double totalScore; // TÃ­nh theo cÃ´ng thá»©c: (ChuyÃªn cáº§n + BÃ i táº­p + Giá»¯a ká»³ * 3 + Cuá»‘i ká»³ * 5) / 10
}
