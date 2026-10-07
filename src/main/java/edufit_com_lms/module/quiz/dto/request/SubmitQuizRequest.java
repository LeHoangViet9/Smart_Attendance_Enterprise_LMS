package edufit_com_lms.module.quiz.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitQuizRequest {
    private Long quizAttemptId;
    private List<StudentAnswerRequest> answers; // Danh sÃ¡ch hÃ²m chá»©a toÃ n bá»™ tick A B C cá»§a SV
    private String proctoringImageUrl;
}
