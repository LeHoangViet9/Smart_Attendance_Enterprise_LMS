package edufit_com_lms.module.quiz.service;

import edufit_com_lms.module.quiz.dto.response.AIGradeSuggestionResponse;

public interface AIGradingService {
    AIGradeSuggestionResponse suggestGrade(String questionContent, String studentAnswer, double maxPoints);
}
