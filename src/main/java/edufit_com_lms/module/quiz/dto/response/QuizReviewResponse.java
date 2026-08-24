package edufit_com_lms.module.quiz.dto.response;

import edufit_com_lms.module.quiz.entity.QuestionType;
import edufit_com_lms.module.quiz.entity.QuizStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizReviewResponse {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double score;
    private QuizStatus status;
    private List<ReviewQuestionDto> questions;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReviewQuestionDto {
        private Long id;
        private String content;
        private QuestionType questionType;
        private Double points;

        // Options available for the question
        private List<ReviewOptionDto> options;

        // The answer submitted by the student
        private ReviewStudentAnswerDto studentAnswer;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReviewOptionDto {
        private Long id;
        private String content;
        private Boolean isCorrect; // Bật mí đáp án đúng ở chế độ xem lại bài
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReviewStudentAnswerDto {
        private Long selectedOptionId;
        private String answerText;
        private Boolean isAwarded;
    }
}
