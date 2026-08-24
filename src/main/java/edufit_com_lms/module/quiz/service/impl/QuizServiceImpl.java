package edufit_com_lms.module.quiz.service.impl;

import edufit_com_lms.module.quiz.dto.request.QuizRequest;
import edufit_com_lms.module.quiz.dto.response.QuizResponse;
import edufit_com_lms.module.quiz.entity.Quiz;
import edufit_com_lms.module.quiz.repository.QuizRepository;
import edufit_com_lms.module.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import edufit_com_lms.module.quiz.mapper.QuizMapper;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {
    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;

    @Override
    public QuizResponse createQuiz(QuizRequest request) {
        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        Quiz savedQuiz = quizRepository.save(quiz);
        return quizMapper.toResponse(savedQuiz);
    }

    @Override
    public QuizResponse updateQuiz(Long quizId, QuizRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz không tồn tại với id: " + quizId));

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setTimeLimitMinutes(request.getTimeLimitMinutes());
        quiz.setStartTime(request.getStartTime());
        quiz.setEndTime(request.getEndTime());

        Quiz updatedQuiz = quizRepository.save(quiz);
        return quizMapper.toResponse(updatedQuiz);
    }

    @Override
    public void deleteQuiz(Long quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new RuntimeException("Quiz không tồn tại với id: " + quizId);
        }
        quizRepository.deleteById(quizId);
    }

    @Override
    public Page<QuizResponse> getQuizzes(String keyword, String searchBy, Pageable pageable) {
        Page<Quiz> quizPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            if ("title".equalsIgnoreCase(searchBy)) {
                quizPage = quizRepository.searchByKeyword(kw, pageable);
                // Tạm thời nếu searchBy là title hoặc rỗng thì dùng chung searchByKeyword
                // (title + desc)
            } else {
                quizPage = quizRepository.searchByKeyword(kw, pageable);
            }
        } else {
            quizPage = quizRepository.findAll(pageable);
        }

        return quizPage.map(quizMapper::toResponse);
    }

    @Override
    public QuizResponse findQuizById(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz không tồn tại với id: " + quizId));
        return quizMapper.toResponse(quiz);
    }
}
