package edufit_com_lms.module.quiz.service;

import edufit_com_lms.module.quiz.dto.request.QuestionRequest;
import edufit_com_lms.module.quiz.dto.response.QuestionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionService {
    QuestionResponse findById(Long id);

    QuestionResponse createQuestion(Long quizId, QuestionRequest request);

    QuestionResponse updateQuestion(Long quizId, Long questionId, QuestionRequest request);

    void deleteQuestion(Long quizId, Long questionId);

    Page<QuestionResponse> findAllQuestions(Long quizId, String keyword, Pageable pageable);
}
