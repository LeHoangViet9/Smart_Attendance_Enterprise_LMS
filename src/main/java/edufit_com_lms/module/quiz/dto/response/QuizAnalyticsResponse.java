package edufit_com_lms.module.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizAnalyticsResponse {
    private Long quizId;
    private String quizTitle;
    private int totalAttempts;
    private double averageScore;
    private double highestScore;
    private double lowestScore;
    private double passRate;
    private int passedCount;
    private int failedCount;

    // Phân bổ phổ điểm, ví dụ: "0-2": 5, "2-4": 15, "4-6": 30...
    private Map<String, Integer> scoreDistribution;

    // Top 3 câu hỏi sinh viên sai nhiều nhất (Lưu id và nội dung câu hỏi)
    private Map<String, Integer> hardestQuestions; 
}
