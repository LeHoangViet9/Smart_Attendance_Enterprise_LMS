package edufit_com_lms.module.quiz.service.impl;

import edufit_com_lms.module.quiz.entity.Question;
import edufit_com_lms.module.quiz.service.QuizScoringService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizScoringServiceImpl implements QuizScoringService {
    @Override
    public double calculateScoreOnTenPointScale(double earnedPoints, List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return 0.0;
        }
        double totalMaxPoints = questions.stream()
                .mapToDouble(question -> question.getPoints() != null ? question.getPoints() : 0.0)
                .sum();
        if (totalMaxPoints <= 0) {
            return 0.0;
        }
        double score = (earnedPoints / totalMaxPoints) * 10.0;
        return Math.round(score * 100.0) / 100.0;
    }
}
