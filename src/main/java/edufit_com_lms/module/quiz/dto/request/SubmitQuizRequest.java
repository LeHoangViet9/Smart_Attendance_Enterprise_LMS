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
    private List<StudentAnswerRequest> answers; // Danh sách hòm chứa toàn bộ tick A B C của SV
}
