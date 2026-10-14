package edufit_com_lms.module.quiz.service.impl;

import edufit_com_lms.common.exception.AppException;
import edufit_com_lms.common.exception.ConflictException;
import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.module.quiz.dto.request.GenerateQuizAiRequest;
import edufit_com_lms.module.quiz.dto.request.QuestionRequest;
import edufit_com_lms.module.quiz.dto.response.QuestionResponse;
import edufit_com_lms.module.quiz.entity.Question;
import edufit_com_lms.module.quiz.entity.QuestionOption;
import edufit_com_lms.module.quiz.entity.QuestionType;
import edufit_com_lms.module.quiz.entity.Quiz;
import edufit_com_lms.module.quiz.mapper.QuizMapper;
import edufit_com_lms.module.quiz.repository.QuestionRepository;
import edufit_com_lms.module.quiz.repository.QuizRepository;
import edufit_com_lms.module.quiz.service.AIQuizGenerationService;
import edufit_com_lms.module.quiz.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;
    private final AIQuizGenerationService aiQuizGenerationService;

    @Override
    public QuestionResponse findById(Long quizId, Long id) {
        Question question = findQuestionOrThrow(id);
        validateQuestionBelongsToQuiz(question, quizId);
        return quizMapper.toQuestionResponse(question);
    }

    @Override
    public QuestionResponse createQuestion(Long quizId, QuestionRequest request, Long lecturerId) {
        Quiz quiz = findQuizOrThrow(quizId);
        checkQuizOwnership(quiz, lecturerId);

        Question question = new Question();
        question.setQuiz(quiz);
        question.setContent(request.getContent());
        question.setPoints(request.getPoints());
        question.setQuestionType(request.getQuestionType());
        question.setOptions(mapOptions(request, question));

        return quizMapper.toQuestionResponse(questionRepository.save(question));
    }

    @Override
    public QuestionResponse updateQuestion(Long quizId, Long questionId, QuestionRequest request, Long lecturerId) {
        Quiz quiz = findQuizOrThrow(quizId);
        checkQuizOwnership(quiz, lecturerId);

        Question question = findQuestionOrThrow(questionId);
        validateQuestionBelongsToQuiz(question, quizId);

        question.setContent(request.getContent());
        question.setPoints(request.getPoints());
        question.setQuestionType(request.getQuestionType());
        if (request.getOptions() != null) {
            question.getOptions().clear();
            question.getOptions().addAll(mapOptions(request, question));
        }

        return quizMapper.toQuestionResponse(questionRepository.save(question));
    }

    @Override
    public void deleteQuestion(Long quizId, Long questionId, Long lecturerId) {
        Quiz quiz = findQuizOrThrow(quizId);
        checkQuizOwnership(quiz, lecturerId);

        Question question = findQuestionOrThrow(questionId);
        validateQuestionBelongsToQuiz(question, quizId);

        try {
            questionRepository.delete(question);
            questionRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Cannot delete this question because quiz attempts already reference it.");
        }
    }

    @Override
    public Page<QuestionResponse> findAllQuestions(Long quizId, String keyword, Pageable pageable) {
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFound("Can not found quiz");
        }

        String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Page<Question> questions = kw != null
                ? questionRepository.findByQuizIdAndKeyword(quizId, kw, pageable)
                : questionRepository.findByQuizId(quizId, pageable);

        return questions.map(quizMapper::toQuestionResponse);
    }

    @Override
    public void importQuestionsFromExcel(Long quizId, org.springframework.web.multipart.MultipartFile file, Long lecturerId) {
        Quiz quiz = findQuizOrThrow(quizId);
        checkQuizOwnership(quiz, lecturerId);

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            DataFormatter formatter = new DataFormatter();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                if (rowNumber++ == 0) {
                    continue;
                }

                Question question = buildQuestionFromRow(quiz, currentRow, formatter);
                if (question != null) {
                    questionRepository.save(question);
                }
            }
        } catch (Exception e) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Fail to parse Excel file: " + e.getMessage());
        }
    }

    @Override
    public void generateQuestionsFromAI(Long quizId, GenerateQuizAiRequest request, Long lecturerId) {
        Quiz quiz = findQuizOrThrow(quizId);
        checkQuizOwnership(quiz, lecturerId);

        List<QuestionRequest> aiGeneratedQuestions = aiQuizGenerationService.generateQuestions(
                request.getDocumentText(),
                request.getNumberOfQuestions());

        for (QuestionRequest qReq : aiGeneratedQuestions) {
            createQuestion(quizId, qReq, lecturerId);
        }
    }

    private void checkQuizOwnership(Quiz quiz, Long lecturerId) {
        if (lecturerId != null && (quiz.getCreatedBy() == null || !quiz.getCreatedBy().getUserId().equals(lecturerId))) {
            throw new AccessDeniedException("You do not have permission to edit questions for this quiz.");
        }
    }

    private Quiz findQuizOrThrow(Long quizId) {
        return quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFound("Can not found quiz"));
    }

    private Question findQuestionOrThrow(Long questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new ResourceNotFound("Can not found question"));
    }

    private void validateQuestionBelongsToQuiz(Question question, Long quizId) {
        if (!question.getQuiz().getId().equals(quizId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Question does not belong to this quiz.");
        }
    }

    private List<QuestionOption> mapOptions(QuestionRequest request, Question question) {
        if (request.getOptions() == null) {
            return new ArrayList<>();
        }
        return request.getOptions().stream()
                .map(opt -> QuestionOption.builder()
                        .content(opt.getContent())
                        .isCorrect(opt.getIsCorrect())
                        .question(question)
                        .build())
                .toList();
    }

    private Question buildQuestionFromRow(Quiz quiz, Row row, DataFormatter formatter) {
        Cell typeCell = row.getCell(0);
        if (typeCell == null || formatter.formatCellValue(typeCell).trim().isEmpty()) {
            return null;
        }

        int typeId;
        try {
            typeId = (int) Double.parseDouble(formatter.formatCellValue(typeCell).trim());
        } catch (NumberFormatException e) {
            return null;
        }

        Question question = new Question();
        question.setQuiz(quiz);
        question.setContent(readCell(row, 1, formatter));
        question.setPoints(1.0);

        List<QuestionOption> options = new ArrayList<>();
        String correctVal = readCell(row, 6, formatter).trim().toUpperCase();

        if (typeId == 1 || typeId == 2 || typeId == 5) {
            question.setQuestionType(typeId == 1 ? QuestionType.SINGLE_CHOICE : typeId == 2 ? QuestionType.TRUE_FALSE : QuestionType.MULTIPLE_CHOICE);
            addChoiceOptions(row, formatter, question, options, correctVal, typeId == 2 ? 2 : 4);
        } else if (typeId == 3) {
            question.setQuestionType(QuestionType.FILL_BLANK);
            options.add(QuestionOption.builder().content(correctVal).isCorrect(true).question(question).build());
        } else if (typeId == 4) {
            question.setQuestionType(QuestionType.ESSAY);
            options.add(QuestionOption.builder().content(correctVal).isCorrect(true).question(question).build());
        } else {
            return null;
        }

        question.setOptions(options);
        return question;
    }

    private void addChoiceOptions(Row row, DataFormatter formatter, Question question, List<QuestionOption> options, String correctVal, int maxCols) {
        String[] labels = {"A", "B", "C", "D"};
        List<String> correctAnswers = Arrays.stream(correctVal.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        for (int i = 0; i < maxCols; i++) {
            String optContent = readCell(row, 2 + i, formatter).trim();
            if (!optContent.isEmpty()) {
                options.add(QuestionOption.builder()
                        .content(optContent)
                        .isCorrect(correctAnswers.contains(labels[i]))
                        .question(question)
                        .build());
            }
        }
    }

    private String readCell(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index);
        return cell != null ? formatter.formatCellValue(cell) : "";
    }
}
