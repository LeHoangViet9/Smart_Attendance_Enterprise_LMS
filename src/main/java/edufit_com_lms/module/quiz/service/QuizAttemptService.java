package edufit_com_lms.module.quiz.service;

import edufit_com_lms.module.quiz.dto.request.SubmitQuizRequest;
import edufit_com_lms.module.quiz.dto.response.QuizAttemptResponse;
import edufit_com_lms.module.quiz.dto.response.QuizReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuizAttemptService {
    QuizAttemptResponse startAttempt(Long quizId, Long studentId);

    QuizAttemptResponse submitAttempt(Long attemptId, SubmitQuizRequest submitRequest);

    Page<QuizAttemptResponse> getStudentAttemptHistory(Long studentId, Pageable pageable);

    QuizReviewResponse getAttemptReview(Long attemptId, Long studentId);

    void autosaveAttempt(Long attemptId, Long studentId, SubmitQuizRequest request);
    
    Page<QuizAttemptResponse> getAttemptsByQuizId(Long quizId, Pageable pageable);

    QuizAttemptResponse gradeQuizAttempt(Long attemptId, edufit_com_lms.module.quiz.dto.request.GradeEssayRequest request, Long lecturerId);

    edufit_com_lms.module.quiz.dto.response.AIGradeSuggestionResponse suggestGradeWithAI(Long attemptId, Long answerId, Long lecturerId);
}
