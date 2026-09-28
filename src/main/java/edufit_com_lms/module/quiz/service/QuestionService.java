package edufit_com_lms.module.quiz.service;

import edufit_com_lms.module.quiz.dto.request.QuestionRequest;
import edufit_com_lms.module.quiz.dto.response.QuestionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionService {
    QuestionResponse findById(Long quizId, Long id);

    QuestionResponse createQuestion(Long quizId, QuestionRequest request, Long lecturerId);

    QuestionResponse updateQuestion(Long quizId, Long questionId, QuestionRequest request, Long lecturerId);

    void deleteQuestion(Long quizId, Long questionId, Long lecturerId);

    Page<QuestionResponse> findAllQuestions(Long quizId, String keyword, Pageable pageable);

    void importQuestionsFromExcel(Long quizId, org.springframework.web.multipart.MultipartFile file, Long lecturerId);
}
