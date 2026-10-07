package edufit_com_lms.module.quiz.service.impl;

import edufit_com_lms.common.exception.ConflictException;
import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.module.quiz.dto.request.GenerateQuizAiRequest;
import edufit_com_lms.module.quiz.dto.request.QuestionRequest;
import edufit_com_lms.module.quiz.dto.response.QuestionResponse;
import edufit_com_lms.module.quiz.entity.Question;
import edufit_com_lms.module.quiz.entity.QuestionOption;
import edufit_com_lms.module.quiz.entity.Quiz;
import edufit_com_lms.module.quiz.mapper.QuizMapper;
import edufit_com_lms.module.quiz.repository.QuestionRepository;
import edufit_com_lms.module.quiz.repository.QuizRepository;
import edufit_com_lms.module.quiz.service.QuestionService;
import edufit_com_lms.module.quiz.service.AIQuizGenerationService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
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

    private void checkQuizOwnership(Quiz quiz, Long lecturerId) {
        if (lecturerId != null) {
            if (quiz.getCreatedBy() == null || !quiz.getCreatedBy().getUserId().equals(lecturerId)) {
                throw new org.springframework.security.access.AccessDeniedException("Báº¡n khÃ´ng cÃ³ quyá»n chá»‰nh sá»­a bá»™ cÃ¢u há»i cá»§a bÃ i Quiz nÃ y!");
            }
        }
    }

    public QuestionResponse findById(Long quizId, Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Can not found question"));
        if (!question.getQuiz().getId().equals(quizId)) {
            throw new RuntimeException("CÃ¢u há»i nÃ y khÃ´ng thuá»™c vá» bÃ i Quiz nÃ y!");
        }
        return quizMapper.toQuestionResponse(question);
    }

    @Override
    public QuestionResponse createQuestion(Long quizId, QuestionRequest request, Long lecturerId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFound("Can not found quiz"));
        checkQuizOwnership(quiz, lecturerId);
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
    public QuestionResponse updateQuestion(Long quizId, Long questionId, QuestionRequest request, Long lecturerId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFound("Can not found quiz"));
        checkQuizOwnership(quiz, lecturerId);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFound("Can not found question"));
        if (!question.getQuiz().getId().equals(quizId)) {
            throw new RuntimeException("CÃ¢u há»i nÃ y khÃ´ng thuá»™c vá» bÃ i Quiz nÃ y!");
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
    public void deleteQuestion(Long quizId, Long questionId, Long lecturerId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFound("Can not found quiz"));
        checkQuizOwnership(quiz, lecturerId);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFound("Can not found question"));
        if (!question.getQuiz().getId().equals(quizId)) {
            throw new RuntimeException("CÃ¢u há»i nÃ y khÃ´ng thuá»™c vá» bÃ i Quiz nÃ y!");
        }
        try {
            questionRepository.delete(question);
            questionRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new RuntimeException("KhÃ´ng thá»ƒ xÃ³a cÃ¢u há»i nÃ y vÃ¬ Ä‘Ã£ cÃ³ sinh viÃªn lÃ m bÃ i (dá»¯ liá»‡u lá»‹ch sá»­ Ä‘ang tham chiáº¿u).");
        }
    }

    @Override
    public Page<QuestionResponse> findAllQuestions(Long quizId, String keyword, Pageable pageable) {
        // Kiá»ƒm tra quiz cÃ³ tá»“n táº¡i khÃ´ng
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFound("Can not found quiz");
        }

        String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Page<Question> questions;
        if (kw != null) {
            questions = questionRepository.findByQuizIdAndKeyword(quizId, kw, pageable);
        } else {
            questions = questionRepository.findByQuizId(quizId, pageable);
        }

        return questions.map(quizMapper::toQuestionResponse);
    }

    @Override
    public void importQuestionsFromExcel(Long quizId, org.springframework.web.multipart.MultipartFile file, Long lecturerId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFound("Can not found quiz"));
        checkQuizOwnership(quiz, lecturerId);
        try (InputStream is = file.getInputStream(); Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            DataFormatter formatter = new DataFormatter();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                if (rowNumber == 0) { // skip header
                    rowNumber++;
                    continue;
                }

                Cell typeCell = currentRow.getCell(0);
                if (typeCell == null || formatter.formatCellValue(typeCell).trim().isEmpty()) continue;

                int typeId;
                try {
                    typeId = (int) Double.parseDouble(formatter.formatCellValue(typeCell).trim());
                } catch (NumberFormatException e) {
                    continue; // Skip invalid rows
                }
                Cell contentCell = currentRow.getCell(1);
                String content = contentCell != null ? formatter.formatCellValue(contentCell) : "";

                Question question = new Question();
                question.setQuiz(quiz);
                question.setContent(content);
                question.setPoints(1.0); // Default points
                List<QuestionOption> options = new ArrayList<>();

                Cell correctCell = currentRow.getCell(6);
                String correctVal = correctCell != null ? formatter.formatCellValue(correctCell).trim().toUpperCase() : "";

                if (typeId == 1 || typeId == 2 || typeId == 5) {
                    if (typeId == 1) question.setQuestionType(edufit_com_lms.module.quiz.entity.QuestionType.SINGLE_CHOICE);
                    else if (typeId == 2) question.setQuestionType(edufit_com_lms.module.quiz.entity.QuestionType.TRUE_FALSE);
                    else question.setQuestionType(edufit_com_lms.module.quiz.entity.QuestionType.MULTIPLE_CHOICE);

                    String[] labels = {"A", "B", "C", "D"};
                    int maxCols = typeId == 2 ? 2 : 4; // True/False chá»‰ cÃ³ 2 cá»™t Ä‘Ã¡p Ã¡n
                    
                    // Há»— trá»£ trÆ°á»ng há»£p nhiá»u Ä‘Ã¡p Ã¡n Ä‘Ãºng, vÃ­ dá»¥: "A, B" hoáº·c "A,B,C"
                    java.util.List<String> correctAnswers = java.util.Arrays.stream(correctVal.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();

                    for (int i = 0; i < maxCols; i++) {
                        Cell optCell = currentRow.getCell(2 + i);
                        if (optCell != null) {
                            String optContent = formatter.formatCellValue(optCell).trim();
                            if (!optContent.isEmpty()) {
                                boolean isCorrect = correctAnswers.contains(labels[i]);
                                options.add(QuestionOption.builder()
                                        .content(optContent)
                                        .isCorrect(isCorrect)
                                        .question(question)
                                        .build());
                            }
                        }
                    }
                } else if (typeId == 3) {
                    question.setQuestionType(edufit_com_lms.module.quiz.entity.QuestionType.FILL_BLANK);
                    options.add(QuestionOption.builder()
                            .content(correctVal)
                            .isCorrect(true)
                            .question(question)
                            .build());
                } else if (typeId == 4) {
                    question.setQuestionType(edufit_com_lms.module.quiz.entity.QuestionType.ESSAY);
                    options.add(QuestionOption.builder()
                            .content(correctVal)
                            .isCorrect(true)
                            .question(question)
                            .build());
                }

                question.setOptions(options);
                questionRepository.save(question);
            }
        } catch (Exception e) {
            throw new RuntimeException("Fail to parse Excel file: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void generateQuestionsFromAI(Long quizId, edufit_com_lms.module.quiz.dto.request.GenerateQuizAiRequest request, Long lecturerId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFound("Quiz not found"));
        
        checkQuizOwnership(quiz, lecturerId);
        
        List<QuestionRequest> aiGeneratedQuestions = aiQuizGenerationService.generateQuestions(request.getDocumentText(), request.getNumberOfQuestions());
        
        for (QuestionRequest qReq : aiGeneratedQuestions) {
            createQuestion(quizId, qReq, lecturerId);
        }
    }
}
