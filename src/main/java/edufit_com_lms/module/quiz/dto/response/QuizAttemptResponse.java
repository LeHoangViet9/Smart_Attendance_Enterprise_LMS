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
    private Long quizId; // ID cá»§a Ä‘á» thi
    private Long studentId; // ID cá»§a há»c viÃªn
    private String studentName;
    private String studentCode;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Tá»•ng Ä‘iá»ƒm Ä‘áº¡t Ä‘Æ°á»£c (Sáº½ null náº¿u Ä‘ang thi / chÆ°a cháº¥m)
    private Double score;

    // Tráº¡ng thÃ¡i: IN_PROGRESS, COMPLETED, ABANDONED
    private QuizStatus status;

    private String proctoringImageUrl;

    // Bá»™ cache táº¡m phá»¥c vá»¥ trÆ°á»ng há»£p rá»›t máº¡ng
    private java.util.List<edufit_com_lms.module.quiz.dto.request.StudentAnswerRequest> cachedAnswers;
}
