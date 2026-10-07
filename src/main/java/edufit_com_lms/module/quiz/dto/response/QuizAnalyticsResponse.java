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

    // PhÃ¢n bá»• phá»• Ä‘iá»ƒm, vÃ­ dá»¥: "0-2": 5, "2-4": 15, "4-6": 30...
    private Map<String, Integer> scoreDistribution;

    // Top 3 cÃ¢u há»i sinh viÃªn sai nhiá»u nháº¥t (LÆ°u id vÃ  ná»™i dung cÃ¢u há»i)
    private Map<String, Integer> hardestQuestions; 
}
