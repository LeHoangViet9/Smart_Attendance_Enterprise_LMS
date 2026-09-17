package edufit_com_lms.module.quiz.service;

import edufit_com_lms.module.quiz.dto.request.QuizRequest;
import edufit_com_lms.module.quiz.dto.response.QuizResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface QuizService {
    QuizResponse createQuiz(QuizRequest quizRequest, Long creatorId);
    QuizResponse updateQuiz(Long quizId, QuizRequest quizRequest, Long lecturerId);
    void deleteQuiz(Long quizId, Long lecturerId);
    Page<QuizResponse> getQuizzes(String keyword, String searchBy, java.util.UUID majorId, Long lecturerId, Pageable pageable);
    QuizResponse findQuizById(Long quizId);
}
