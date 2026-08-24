package edufit_com_lms.module.quiz.dto.response;

import edufit_com_lms.module.quiz.entity.QuizStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizAttemptResponse {
    private Long id;
    private Long quizId; // ID của đề thi
    private Long studentId; // ID của học viên

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Tổng điểm đạt được (Sẽ null nếu đang thi / chưa chấm)
    private Double score;

    // Trạng thái: IN_PROGRESS, COMPLETED, ABANDONED
    private QuizStatus status;

    // Bộ cache tạm phục vụ trường hợp rớt mạng
    private java.util.List<edufit_com_lms.module.quiz.dto.request.StudentAnswerRequest> cachedAnswers;
}
