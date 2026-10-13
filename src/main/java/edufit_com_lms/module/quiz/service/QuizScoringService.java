package edufit_com_lms.module.quiz.service;

import edufit_com_lms.module.quiz.entity.Question;

import java.util.List;

public interface QuizScoringService {
    double calculateScoreOnTenPointScale(double earnedPoints, List<Question> questions);
}
