package edufit_com_lms.module.quiz.service.impl;

import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.module.quiz.dto.request.QuestionRequest;
import edufit_com_lms.module.quiz.dto.response.QuestionResponse;
import edufit_com_lms.module.quiz.entity.Question;
import edufit_com_lms.module.quiz.entity.QuestionOption;
import edufit_com_lms.module.quiz.entity.Quiz;
import edufit_com_lms.module.quiz.mapper.QuizMapper;
import edufit_com_lms.module.quiz.repository.QuestionRepository;
import edufit_com_lms.module.quiz.repository.QuizRepository;
import edufit_com_lms.module.quiz.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;


    public QuestionResponse findById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Can not found question"));
        return quizMapper.toQuestionResponse(question);
    }

    @Override
    public QuestionResponse createQuestion(Long quizId, QuestionRequest request) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFound("Can not found quiz"));
        Question question = new Question();
        question.setQuiz(quiz);
        question.setContent(request.getContent());
        question.setPoints(request.getPoints());
        question.setQuestionType(request.getQuestionType());
        if (request.getOptions() != null) {
            List<QuestionOption> options = request.getOptions().stream().map(opt -> {
                QuestionOption option = QuestionOption.builder()
                        .content(opt.getContent())
                        .isCorrect(opt.getIsCorrect())
                        .question(question)
                        .build();
                return option;
            }).toList();
            question.setOptions(options);
        }
        Question savedQuestion = questionRepository.save(question);
        return quizMapper.toQuestionResponse(savedQuestion);
    }

    @Override
    public QuestionResponse updateQuestion(Long quizId, Long questionId, QuestionRequest request) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFound("Can not found quiz"));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFound("Can not found question"));
        if (!question.getQuiz().getId().equals(quizId)) {
            throw new RuntimeException("Câu hỏi này không thuộc về bài Quiz này!");
        }
        question.setContent(request.getContent());
        question.setPoints(request.getPoints());
        question.setQuestionType(request.getQuestionType());
        if (request.getOptions() != null) {
            List<QuestionOption> options = request.getOptions().stream().map(opt -> {
                QuestionOption option = QuestionOption.builder()
                        .content(opt.getContent())
                        .isCorrect(opt.getIsCorrect())
                        .question(question)
                        .build();
                return option;
            }).toList();
            question.getOptions().clear();
            question.getOptions().addAll(options);
        }
        Question savedQuestion = questionRepository.save(question);
        return quizMapper.toQuestionResponse(savedQuestion);
    }

    @Override
    public void deleteQuestion(Long quizId, Long questionId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFound("Can not found quiz"));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFound("Can not found question"));
        if (!question.getQuiz().getId().equals(quizId)) {
            throw new RuntimeException("Câu hỏi này không thuộc về bài Quiz này!");
        }
        questionRepository.delete(question);
    }

    @Override
    public Page<QuestionResponse> findAllQuestions(Long quizId, String keyword, Pageable pageable) {
        // Kiểm tra quiz có tồn tại không
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFound("Can not found quiz");
        }

        String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Page<Question> questions = questionRepository.findByQuizIdAndKeyword(quizId, kw, pageable);

        return questions.map(quizMapper::toQuestionResponse);
    }
}
