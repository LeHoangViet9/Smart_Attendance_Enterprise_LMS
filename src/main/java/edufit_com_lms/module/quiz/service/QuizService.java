package edufit_com_lms.module.quiz.service;

import edufit_com_lms.module.quiz.dto.request.QuizRequest;
import edufit_com_lms.module.quiz.dto.response.QuizResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface QuizService {
    QuizResponse createQuiz(QuizRequest quizRequest);
    QuizResponse updateQuiz(Long quizId,QuizRequest quizRequest);
    void  deleteQuiz(Long quizId);
    Page<QuizResponse> getQuizzes(String keyword,String searchBy,Pageable pageable);
    QuizResponse findQuizById(Long quizId);
}
