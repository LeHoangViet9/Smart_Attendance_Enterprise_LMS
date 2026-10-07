package edufit_com_lms.module.quiz.service;

import edufit_com_lms.module.quiz.dto.request.QuestionRequest;
import java.util.List;

public interface AIQuizGenerationService {
    List<QuestionRequest> generateQuestions(String documentText, int numberOfQuestions);
}
