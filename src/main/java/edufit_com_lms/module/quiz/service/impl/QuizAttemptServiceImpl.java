package edufit_com_lms.module.quiz.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edufit_com_lms.common.exception.ConflictException;
import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.auth.repository.UserRepository;
import edufit_com_lms.module.quiz.dto.request.StudentAnswerRequest;
import edufit_com_lms.module.quiz.dto.request.SubmitQuizRequest;
import edufit_com_lms.module.quiz.dto.response.AIGradeSuggestionResponse;
import edufit_com_lms.module.quiz.dto.response.QuizAttemptResponse;
import edufit_com_lms.module.quiz.dto.response.QuizReviewResponse;
import edufit_com_lms.module.quiz.entity.*;
import edufit_com_lms.module.quiz.mapper.QuizAttemptMapper;
import edufit_com_lms.module.quiz.repository.*;
import edufit_com_lms.module.quiz.service.AIGradingService;
import edufit_com_lms.module.quiz.service.QuizAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizAttemptServiceImpl implements QuizAttemptService {
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizAttemptMapper quizAttemptMapper;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final AIGradingService aiGradingService;
    @Autowired
    @Lazy
    private QuizAttemptService quizAttemptService;

    @Transactional
    @Override
    public QuizAttemptResponse startAttempt(Long quizId, Long studentId, String accessCode) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFound("Can not found quiz"));
        User student = userRepository.findById(studentId).orElseThrow(() -> new ResourceNotFound("Can not found user"));

        // Kiá»ƒm tra thá»i gian cá»§a bÃ i thi trÆ°á»›c tiÃªn
        LocalDateTime now = LocalDateTime.now();
        if (quiz.getStartTime() != null && now.isBefore(quiz.getStartTime())) {
            throw new ConflictException("The test has not yet started!");
        }
        if (quiz.getEndTime() != null && now.isAfter(quiz.getEndTime())) {
            throw new ConflictException("This test has expired!");
        }

        List<QuizAttempt> existingAttempts = quizAttemptRepository.findByQuizIdAndStudentUserId(quizId, studentId);

        Optional<QuizAttempt> inProgressAttempt = existingAttempts.stream()
                .filter(a -> a.getStatus() == QuizStatus.IN_PROGRESS)
                .findFirst();

        if (inProgressAttempt.isPresent()) {
            QuizAttempt existingAttempt = inProgressAttempt.get();
            // KhÃ´i phá»¥c dá»¯ liá»‡u nhÃ¡p tá»« Redis vÃ  nháº£ vá» cho sinh viÃªn thi
            // tiáº¿p tá»¥c
            QuizAttemptResponse res = quizAttemptMapper.toResponse(existingAttempt);
            try {
                String cachedData = stringRedisTemplate.opsForValue()
                        .get("quiz:attempt:" + existingAttempt.getId());
                if (cachedData != null) {
                    List<StudentAnswerRequest> cachedAnswers = objectMapper.readValue(cachedData,
                            new TypeReference<>() {
                            });
                    res.setCachedAnswers(cachedAnswers);
                }
            } catch (Exception e) {
                log.error("Lá»—i khi Ä‘á»c dá»¯ liá»‡u nhÃ¡p tá»« Redis cho bÃ i thi: " + existingAttempt.getId(), e);
            }
            return res; // Tráº£ vá» attempt cÅ© thay vÃ¬ quÄƒng lá»—i
        }

        // Kiá»ƒm tra máº­t kháº©u (náº¿u Ä‘á» thi yÃªu cáº§u)
        if (quiz.getAccessCode() != null && !quiz.getAccessCode().trim().isEmpty()) {
            if (accessCode == null || !accessCode.equals(quiz.getAccessCode())) {
                throw new ConflictException("Máº­t kháº©u bÃ i thi khÃ´ng chÃ­nh xÃ¡c");
            }
        }

        // Kiá»ƒm tra sá»‘ láº§n lÃ m tá»‘i Ä‘a (náº¿u cÃ³)
        if (quiz.getMaxAttempts() != null) {
            long attemptsCount = existingAttempts.stream()
                    .filter(a -> a.getStatus() == QuizStatus.COMPLETED)
                    .count();
            if (attemptsCount >= quiz.getMaxAttempts()) {
                throw new ConflictException("Báº¡n Ä‘Ã£ vÆ°á»£t quÃ¡ sá»‘ láº§n lÃ m tá»‘i Ä‘a cho bÃ i thi nÃ y!");
            }
        }

        // Cháº·n thi láº¡i Ä‘á»‘i vá»›i nhá»¯ng bÃ i thi yÃªu cáº§u quÃ©t máº·t (chá»‰
        // cho phÃ©p thi 1 láº§n)
        if (Boolean.TRUE.equals(quiz.getRequiresProctoring())) {
            boolean hasCompleted = existingAttempts.stream()
                    .anyMatch(a -> a.getStatus() == edufit_com_lms.module.quiz.entity.QuizStatus.COMPLETED);
            if (hasCompleted) {
                throw new ConflictException(
                        "BÃ i thi nÃ y yÃªu cáº§u giÃ¡m sÃ¡t (quÃ©t máº·t) nÃªn báº¡n chá»‰ Ä‘Æ°á»£c phÃ©p thi 1 láº§n duy nháº¥t!");
            }
        }

        QuizAttempt quizAttempt = QuizAttempt.builder()
                .quiz(quiz)
                .student(student)
                .startTime(LocalDateTime.now())
                .status(QuizStatus.IN_PROGRESS)
                .build();
        return quizAttemptMapper.toResponse(quizAttemptRepository.save(quizAttempt));
    }

    @Override
    @Transactional
    public QuizAttemptResponse submitAttempt(Long attemptId, SubmitQuizRequest submitRequest) {
        QuizAttempt quizAttempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFound("Can not found quiz attempt"));

        if (quizAttempt.getStatus() != QuizStatus.IN_PROGRESS) {
            throw new ConflictException("Attempt is already submitted or abandoned");
        }

        LocalDateTime startTime = quizAttempt.getStartTime();
        Integer timeLimit = quizAttempt.getQuiz().getTimeLimitMinutes();
        LocalDateTime submitTime = LocalDateTime.now();

        // Kiá»ƒm tra xem bÃ i thi Ä‘Ã£ qua thá»i gian tuyá»‡t Ä‘á»‘i cá»§a há»‡
        // thá»‘ng chÆ°a
        if (quizAttempt.getQuiz().getEndTime() != null && submitTime.isAfter(quizAttempt.getQuiz().getEndTime())) {
            log.warn("Ná»™p bÃ i trá»… (quÃ¡ thá»i gian káº¿t thÃºc) - BÃ i thi ID: {}", attemptId);
        }

        // Náº¿u quiz cÃ³ giá»›i háº¡n thá»i gian, check xem ná»™p muá»™n khÃ´ng
        if (timeLimit != null) {
            LocalDateTime deadline = startTime.plusMinutes(timeLimit);
            if (submitTime.isAfter(deadline)) {
                throw new ConflictException("Ná»™p bÃ i quÃ¡ háº¡n lÃ m bÃ i (vÆ°á»£t quÃ¡ thá»i gian quy Ä‘á»‹nh).");
            }
        }

        // Check proctoring image if required
        if (Boolean.TRUE.equals(quizAttempt.getQuiz().getRequiresProctoring())) {
            if (submitRequest.getProctoringImageUrl() == null
                    || submitRequest.getProctoringImageUrl().trim().isEmpty()) {
                throw new ConflictException(
                        "BÃ i thi nÃ y yÃªu cáº§u xÃ¡c thá»±c khuÃ´n máº·t! Vui lÃ²ng cung cáº¥p áº£nh Ä‘Ã­nh kÃ¨m.");
            }
            quizAttempt.setProctoringImageUrl(submitRequest.getProctoringImageUrl());
        }

        double earnedPoints = 0.0;
        List<StudentAnswer> studentAnswers = new ArrayList<>();
        Set<Long> processedQuestionIds = new HashSet<>();

        if (submitRequest.getAnswers() != null && !submitRequest.getAnswers().isEmpty()) {
            for (StudentAnswerRequest answerReq : submitRequest.getAnswers()) {
                // TrÃ¡nh tÃ¬nh tráº¡ng spam 1 cÃ¢u tráº£ lá»i nhiá»u láº§n Ä‘á»ƒ buff Ä‘iá»ƒm
                // áº£o
                if (processedQuestionIds.contains(answerReq.getQuestionId())) {
                    continue;
                }
                processedQuestionIds.add(answerReq.getQuestionId());

                Question question = questionRepository.findById(answerReq.getQuestionId())
                        .orElse(null);

                if (question == null) {
                    log.warn("CÃ¢u há»i {} khÃ´ng cÃ²n tá»“n táº¡i, bá» qua.", answerReq.getQuestionId());
                    continue;
                }

                // Cháº·n Ä‘Ã¡nh trÃ¡o cÃ¢u há»i tá»« bÃ i thi khÃ¡c
                if (!question.getQuiz().getId().equals(quizAttempt.getQuiz().getId())) {
                    log.warn("Cáº£nh bÃ¡o: CÃ¢u há»i {} khÃ´ng thuá»™c vá» bÃ i thi hiá»‡n táº¡i!", question.getId());
                    continue; // Bá» qua thay vÃ¬ lÃ m há»ng toÃ n bá»™ request
                }

                boolean isAwarded = false;
                QuestionOption selectedOption = null;

                // Validate selectedOption for single choice / true-false
                if (answerReq.getSelectedOptionId() != null) {
                    selectedOption = questionOptionRepository.findById(answerReq.getSelectedOptionId())
                            .orElse(null);
                    // Ensure the option belongs to the current question
                    if (selectedOption != null && !selectedOption.getQuestion().getId().equals(question.getId())) {
                        selectedOption = null;
                    }
                }

                double qPoints = question.getPoints() != null ? question.getPoints() : 0.0;

                // MULTIPLE_CHOICE handling
                if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                    List<Long> providedIds = answerReq.getSelectedOptionIds();
                    if (providedIds != null && !providedIds.isEmpty()) {
                        Set<Long> questionOptionIds = question.getOptions().stream()
                                .map(QuestionOption::getId)
                                .collect(java.util.stream.Collectors.toSet());

                        // Only keep valid options
                        providedIds = providedIds.stream().filter(questionOptionIds::contains)
                                .collect(java.util.stream.Collectors.toList());

                        Set<Long> correctOptionIds = question.getOptions().stream()
                                .filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect()))
                                .map(QuestionOption::getId)
                                .collect(java.util.stream.Collectors.toSet());
                        if (!providedIds.isEmpty() && new HashSet<>(providedIds).equals(correctOptionIds)) {
                            isAwarded = true;
                            earnedPoints += qPoints;
                        }
                    }
                } else if (question.getQuestionType() == QuestionType.SINGLE_CHOICE
                        || question.getQuestionType() == QuestionType.TRUE_FALSE) {
                    if (selectedOption != null && Boolean.TRUE.equals(selectedOption.getIsCorrect())) {
                        isAwarded = true;
                        earnedPoints += qPoints;
                    }
                } else if (question.getQuestionType() == QuestionType.FILL_BLANK) {
                    if (answerReq.getAnswerText() != null && !answerReq.getAnswerText().isEmpty()) {
                        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                            boolean matched = question.getOptions().stream()
                                    .anyMatch(opt -> Boolean.TRUE.equals(opt.getIsCorrect())
                                            && opt.getContent().trim()
                                                    .equalsIgnoreCase(answerReq.getAnswerText().trim()));
                            if (matched) {
                                isAwarded = true;
                                earnedPoints += qPoints;
                            }
                        }
                    }
                } else if (question.getQuestionType() == QuestionType.ESSAY) {
                    // CÃ¢u tá»± luáº­n sáº½ Ä‘Æ°á»£c giáº£ng viÃªn cháº¥m thá»§ cÃ´ng sau
                    isAwarded = false;
                }

                StudentAnswer studentAnswer = StudentAnswer.builder()
                        .attempt(quizAttempt)
                        .question(question)
                        .selectedOption(selectedOption)
                        .selectedOptionIds(answerReq.getSelectedOptionIds() != null
                                ? new java.util.HashSet<>(answerReq.getSelectedOptionIds())
                                : null)
                        .answerText(answerReq.getAnswerText())
                        .isAwarded(isAwarded)
                        .earnedPoints(isAwarded ? qPoints : 0.0)
                        .build();
                studentAnswers.add(studentAnswer);
            }
        }

        // Auto-fill cÃ¡c cÃ¢u há»i bá»‹ sinh viÃªn bá» trá»‘ng Ä‘á»ƒ cÃ³ record
        // cháº¥m Ä‘iá»ƒm (nháº¥t lÃ  cÃ¢u ESSAY)
        if (quizAttempt.getQuiz().getQuestions() != null) {
            for (Question question : quizAttempt.getQuiz().getQuestions()) {
                if (!processedQuestionIds.contains(question.getId())) {
                    StudentAnswer blankAnswer = StudentAnswer.builder()
                            .attempt(quizAttempt)
                            .question(question)
                            .isAwarded(false)
                            .earnedPoints(0.0)
                            .build();
                    studentAnswers.add(blankAnswer);
                }
            }
        }

        if (!studentAnswers.isEmpty()) {
            studentAnswerRepository.saveAll(studentAnswers);
        }

        double totalMaxPoints = quizAttempt.getQuiz().getQuestions().stream()
                .mapToDouble(q -> q.getPoints() != null ? q.getPoints() : 0.0).sum();

        double finalScore = 0.0;
        if (totalMaxPoints > 0) {
            finalScore = (earnedPoints / totalMaxPoints) * 10.0;
            finalScore = Math.round(finalScore * 100.0) / 100.0;
        }

        quizAttempt.setScore(finalScore);
        quizAttempt.setEndTime(submitTime);
        quizAttempt.setStatus(QuizStatus.COMPLETED);

        // XÃ³a hoÃ n toÃ n báº£n nhÃ¡p trÃªn Redis Ä‘á»ƒ dá»n bá» máº·t RAM
        stringRedisTemplate.delete("quiz:attempt:" + attemptId);

        QuizAttempt savedAttempt = quizAttemptRepository.save(quizAttempt);

        // Báº¯n thÃ´ng bÃ¡o cho ngÆ°á»i táº¡o Ä‘á» (Giáº£ng viÃªn / Admin)
        if (savedAttempt.getQuiz() != null && savedAttempt.getQuiz().getCreatedBy() != null) {
            eventPublisher.publishEvent(edufit_com_lms.module.notification.event.NotificationEvent.builder()
                    .title("CÃ³ sinh viÃªn ná»™p bÃ i thi")
                    .message("Sinh viÃªn " + savedAttempt.getStudent().getFullName() + " vá»«a hoÃ n thÃ nh bÃ i thi: "
                            + savedAttempt.getQuiz().getTitle())
                    .type("SYSTEM_LOG")
                    .recipientId(savedAttempt.getQuiz().getCreatedBy().getUserId())
                    .build());
        }

        // Tá»± Ä‘á»™ng Gá»­i Cáº£nh BÃ¡o cho sinh viÃªn náº¿u Ä‘iá»ƒm thi dÆ°á»›i 5.0
        // (Automated Warning Alert)
        if (finalScore < 5.0) {
            eventPublisher.publishEvent(edufit_com_lms.module.notification.event.NotificationEvent.builder()
                    .title("Cáº£nh bÃ¡o há»c vá»¥: Äiá»ƒm thi tháº¥p")
                    .message("Báº¡n vá»«a Ä‘áº¡t " + finalScore + " Ä‘iá»ƒm trong bÃ i thi "
                            + savedAttempt.getQuiz().getTitle() + ". Vui lÃ²ng Ã´n táº­p láº¡i kiáº¿n thá»©c!")
                    .type("WARNING")
                    .recipientId(savedAttempt.getStudent().getUserId())
                    .build());
        }

        return quizAttemptMapper.toResponse(savedAttempt);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizAttemptResponse> getStudentAttemptHistory(Long studentId, Pageable pageable) {
        Page<QuizAttempt> attempts = quizAttemptRepository.findAllByStudentUserIdOrderByStartTimeDesc(studentId,
                pageable);
        return attempts.map(quizAttemptMapper::toResponse);
    }

    @Override
    public void autosaveAttempt(Long attemptId, Long studentId, SubmitQuizRequest submitRequest) {
        QuizAttempt quizAttempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFound("Can not found quiz attempt"));
        if (!quizAttempt.getStudent().getUserId().equals(studentId)) {
            throw new ConflictException("You are not the owner of this attempt");
        }
        if (quizAttempt.getStatus() != QuizStatus.IN_PROGRESS) {
            throw new ConflictException("Cannot autosave, attempt is not in progress");
        }
        try {
            String key = "quiz:attempt:" + attemptId;
            String json = objectMapper.writeValueAsString(submitRequest.getAnswers());
            // LÆ°u vÃ  set háº¡n tá»± sÃ¡t sau 1 ngÃ y náº¿u khÃ´ng xáº£ bá»›t bá»™ nhá»›
            stringRedisTemplate.opsForValue().set(key, json, 1, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            log.error("Lá»—i khi serialize cÃ¢u tráº£ lá»i Ä‘á»ƒ lÆ°u nhÃ¡p Redis cho bÃ i thi: " + attemptId, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public QuizReviewResponse getAttemptReview(Long attemptId, Long studentId) {
        QuizAttempt quizAttempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFound("Can not found quiz attempt"));

        // If studentId is null, it means it's accessed by Lecturer/Admin for grading
        if (studentId != null && !quizAttempt.getStudent().getUserId().equals(studentId)) {
            throw new ConflictException("You are not the owner of this attempt");
        }

        if (studentId != null) {
            // Há»c sinh chá»‰ Ä‘Æ°á»£c xem láº¡i khi Ä‘Ã£ ná»™p bÃ i (COMPLETED)
            if (quizAttempt.getStatus() != QuizStatus.COMPLETED) {
                throw new ConflictException("You can only review completed attempts");
            }
        } else {
            // Giáº£ng viÃªn khÃ´ng Ä‘Æ°á»£c cháº¥m bÃ i thi Ä‘ang lÃ m (IN_PROGRESS)
            if (quizAttempt.getStatus() == QuizStatus.IN_PROGRESS) {
                throw new ConflictException(
                        "BÃ i thi nÃ y sinh viÃªn váº«n Ä‘ang lÃ m (In Progress). ChÆ°a thá»ƒ cháº¥m Ä‘iá»ƒm.");
            }
        }

        Quiz quiz = quizAttempt.getQuiz();

        if (studentId != null && quiz.getReviewType() != null) {
            if (quiz.getReviewType() == ReviewType.NEVER) {
                throw new ConflictException("GiÃ¡o viÃªn khÃ´ng cho phÃ©p xem láº¡i bÃ i thi nÃ y.");
            }
            if (quiz.getReviewType() == ReviewType.AFTER_DEADLINE) {
                if (quiz.getEndTime() != null && LocalDateTime.now().isBefore(quiz.getEndTime())) {
                    throw new ConflictException(
                            "ChÆ°a Ä‘áº¿n thá»i gian xem láº¡i bÃ i (Pháº£i chá» qua háº¡n káº¿t thÃºc).");
                }
            }
        }

        List<StudentAnswer> studentAnswers = studentAnswerRepository.findAllByAttemptId(attemptId);

        List<QuizReviewResponse.ReviewQuestionDto> questionDtos = new ArrayList<>();

        for (Question question : quiz.getQuestions()) {
            List<QuizReviewResponse.ReviewOptionDto> optionDtos = new ArrayList<>();
            if (question.getOptions() != null) {
                for (QuestionOption opt : question.getOptions()) {
                    optionDtos.add(QuizReviewResponse.ReviewOptionDto.builder()
                            .id(opt.getId())
                            .content(opt.getContent())
                            .isCorrect(opt.getIsCorrect())
                            .build());
                }
                // XÃ¡o trá»™n vá»‹ trÃ­ Ä‘Ã¡p Ã¡n (options) theo cÃ¹ng má»™t seed
                java.util.Collections.shuffle(optionDtos,
                        new java.util.Random(quizAttempt.getStudent().getUserId() + quiz.getId()));
            }

            QuizReviewResponse.ReviewStudentAnswerDto answerDto = null;
            Optional<StudentAnswer> ansOpt = studentAnswers.stream()
                    .filter(a -> a.getQuestion().getId().equals(question.getId())).findFirst();
            if (ansOpt.isPresent()) {
                StudentAnswer ans = ansOpt.get();
                answerDto = QuizReviewResponse.ReviewStudentAnswerDto.builder()
                        .id(ans.getId())
                        .selectedOptionId(ans.getSelectedOption() != null ? ans.getSelectedOption().getId() : null)
                        .selectedOptionIds(
                                ans.getSelectedOptionIds() != null ? new ArrayList<>(ans.getSelectedOptionIds()) : null)
                        .answerText(ans.getAnswerText())
                        .isAwarded(ans.getIsAwarded())
                        .earnedPoints(ans.getEarnedPoints())
                        .feedback(ans.getFeedback())
                        .build();
            }

            questionDtos.add(QuizReviewResponse.ReviewQuestionDto.builder()
                    .id(question.getId())
                    .content(question.getContent())
                    .questionType(question.getQuestionType())
                    .points(question.getPoints())
                    .options(optionDtos)
                    .studentAnswer(answerDto)
                    .build());
        }

        // XÃ¡o trá»™n vá»‹ trÃ­ cÃ¢u há»i theo cÃ¹ng má»™t seed Ä‘á»ƒ khá»›p vá»›i
        // lÃºc thi
        java.util.Collections.shuffle(questionDtos,
                new java.util.Random(quizAttempt.getStudent().getUserId() + quiz.getId()));

        return QuizReviewResponse.builder()
                .attemptId(quizAttempt.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .startTime(quizAttempt.getStartTime())
                .endTime(quizAttempt.getEndTime())
                .score((quiz.getShowScore() != null && !quiz.getShowScore()) ? null : quizAttempt.getScore())
                .status(quizAttempt.getStatus())
                .proctoringImageUrl(quizAttempt.getProctoringImageUrl())
                .questions(questionDtos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizAttemptResponse> getAttemptsByQuizId(Long quizId, Pageable pageable) {
        Page<QuizAttempt> attempts = quizAttemptRepository.findAllByQuizIdOrderByStartTimeDesc(quizId, pageable);
        return attempts.map(quizAttemptMapper::toResponse);
    }

    @Override
    @Transactional
    public QuizAttemptResponse gradeQuizAttempt(Long attemptId,
            edufit_com_lms.module.quiz.dto.request.GradeEssayRequest request, Long lecturerId) {
        QuizAttempt quizAttempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFound("Can not found quiz attempt"));

        // Verify ownership (Lecturer should own the quiz)
        if (lecturerId != null) {
            User creator = quizAttempt.getQuiz().getCreatedBy();
            if (creator == null || !creator.getUserId().equals(lecturerId)) {
                throw new ConflictException("You are not the owner of this quiz");
            }
        }

        List<StudentAnswer> studentAnswers = studentAnswerRepository.findAllByAttemptId(attemptId);

        for (edufit_com_lms.module.quiz.dto.request.GradeEssayRequest.QuestionGrade qg : request.getGrades()) {
            StudentAnswer answer = studentAnswers.stream()
                    .filter(a -> a.getId().equals(qg.getStudentAnswerId()))
                    .findFirst()
                    .orElse(null);

            if (answer != null) {
                // Validate points do not exceed max points for the question
                double maxPoints = answer.getQuestion().getPoints() != null ? answer.getQuestion().getPoints() : 0.0;
                if (qg.getPoints() > maxPoints) {
                    throw new ConflictException("Äiá»ƒm cháº¥m (" + qg.getPoints()
                            + ") khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ Ä‘iá»ƒm tá»‘i Ä‘a cá»§a cÃ¢u há»i (" + maxPoints + ")");
                }
                if (qg.getPoints() < 0) {
                    throw new ConflictException("Äiá»ƒm cháº¥m khÃ´ng Ä‘Æ°á»£c phÃ©p Ã¢m");
                }

                answer.setEarnedPoints(qg.getPoints());
                answer.setFeedback(qg.getFeedback());
                answer.setIsAwarded(qg.getPoints() > 0);
            }
        }

        studentAnswerRepository.saveAll(studentAnswers);

        // Recalculate score
        double totalEarned = studentAnswers.stream()
                .mapToDouble(a -> a.getEarnedPoints() != null ? a.getEarnedPoints() : 0.0)
                .sum();

        double totalMaxPoints = quizAttempt.getQuiz().getQuestions().stream()
                .mapToDouble(q -> q.getPoints() != null ? q.getPoints() : 0.0).sum();

        double finalScore = 0.0;
        if (totalMaxPoints > 0) {
            finalScore = (totalEarned / totalMaxPoints) * 10.0;
            finalScore = Math.round(finalScore * 100.0) / 100.0;
        }

        quizAttempt.setScore(finalScore);

        QuizAttempt savedAttempt = quizAttemptRepository.save(quizAttempt);

        // Báº¯n thÃ´ng bÃ¡o cáº£nh bÃ¡o há»c vá»¥ náº¿u Ä‘iá»ƒm tháº¥p sau khi GV
        // cháº¥m tá»± luáº­n
        if (finalScore < 5.0) {
            eventPublisher.publishEvent(edufit_com_lms.module.notification.event.NotificationEvent.builder()
                    .title("Cáº£nh bÃ¡o há»c vá»¥: Äiá»ƒm thi tháº¥p")
                    .message("Giáº£ng viÃªn vá»«a cháº¥m Ä‘iá»ƒm bÃ i thi " + savedAttempt.getQuiz().getTitle()
                            + ". Báº¡n chá»‰ Ä‘áº¡t " + finalScore
                            + " Ä‘iá»ƒm. Vui lÃ²ng Ã´n táº­p láº¡i kiáº¿n thá»©c!")
                    .type("WARNING")
                    .recipientId(savedAttempt.getStudent().getUserId())
                    .build());
        }

        return quizAttemptMapper.toResponse(savedAttempt);
    }

    // ----------
    // Timeout handling for IN_PROGRESS attempts
    // ----------
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void abandonStaleAttempts() {
        List<QuizAttempt> inProgressAttempts = quizAttemptRepository.findByStatus(QuizStatus.IN_PROGRESS);
        if (inProgressAttempts == null || inProgressAttempts.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();

        for (QuizAttempt attempt : inProgressAttempts) {
            Integer timeLimit = attempt.getQuiz().getTimeLimitMinutes();
            LocalDateTime deadline = null;
            if (timeLimit != null) {
                deadline = attempt.getStartTime().plusMinutes(timeLimit);
            } else if (attempt.getQuiz().getEndTime() != null) {
                deadline = attempt.getQuiz().getEndTime();
            }
            if (deadline != null && now.isAfter(deadline)) {
                log.info("Attempt id {} is timing out, auto-submitting...", attempt.getId());
                try {
                    String cachedData = stringRedisTemplate.opsForValue().get("quiz:attempt:" + attempt.getId());
                    List<StudentAnswerRequest> cachedAnswers = new ArrayList<>();
                    if (cachedData != null) {
                        cachedAnswers = objectMapper.readValue(cachedData, new TypeReference<>() {
                        });
                    }

                    SubmitQuizRequest autoSubmitReq = new SubmitQuizRequest();
                    autoSubmitReq.setQuizAttemptId(attempt.getId());
                    autoSubmitReq.setAnswers(cachedAnswers);
                    if (Boolean.TRUE.equals(attempt.getQuiz().getRequiresProctoring())) {
                        autoSubmitReq.setProctoringImageUrl(
                                attempt.getProctoringImageUrl() != null ? attempt.getProctoringImageUrl()
                                        : "TIMEOUT_NO_IMAGE");
                    }

                    // Gá»i hÃ m submitAttempt Ä‘á»ƒ tá»± Ä‘á»™ng ná»™p bÃ i vÃ  cháº¥m Ä‘iá»ƒm
                    quizAttemptService.submitAttempt(attempt.getId(), autoSubmitReq);
                    log.info("Attempt id {} auto-submitted successfully.", attempt.getId());

                    if (attempt.getQuiz() != null && attempt.getQuiz().getCreatedBy() != null) {
                        eventPublisher.publishEvent(edufit_com_lms.module.notification.event.NotificationEvent.builder()
                                .title("Há»‡ thá»‘ng tá»± Ä‘á»™ng thu bÃ i")
                                .message("Attempt id " + attempt.getId()
                                        + " Ä‘Ã£ tá»± Ä‘á»™ng ná»™p do háº¿t thá»i gian lÃ m bÃ i.")
                                .type("SYSTEM_LOG")
                                .recipientId(attempt.getQuiz().getCreatedBy().getUserId())
                                .build());
                    }
                } catch (Exception e) {
                    log.error("Lá»—i khi auto-submit attempt {}. ÄÃ¡nh dáº¥u ABANDONED.", attempt.getId(), e);
                    attempt.setStatus(QuizStatus.ABANDONED);
                    attempt.setEndTime(now);
                    quizAttemptRepository.save(attempt);
                } // end catch
            } // end if (deadline != null)
        } // end for
    } // end abandonStaleAttempts

    @Override
    @Transactional(readOnly = true)
    public AIGradeSuggestionResponse suggestGradeWithAI(Long attemptId, Long answerId, Long lecturerId) {
        QuizAttempt quizAttempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFound("Can not found quiz attempt"));

        if (lecturerId != null) {
            User creator = quizAttempt.getQuiz().getCreatedBy();
            if (creator == null || !creator.getUserId().equals(lecturerId)) {
                throw new ConflictException("You are not the owner of this quiz");
            }
        }

        StudentAnswer studentAnswer = studentAnswerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFound("Can not found student answer"));

        if (!studentAnswer.getAttempt().getId().equals(attemptId)) {
            throw new ConflictException("Answer does not belong to this attempt");
        }

        String questionContent = studentAnswer.getQuestion().getContent();
        String answerText = studentAnswer.getAnswerText();
        double maxPoints = studentAnswer.getQuestion().getPoints() != null ? studentAnswer.getQuestion().getPoints()
                : 0.0;

        return aiGradingService.suggestGrade(questionContent, answerText, maxPoints);
    }

    @Override
    @Transactional
    public void regradeQuiz(Long quizId, Long lecturerId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFound("Can not found quiz"));

        // Verify ownership
        if (lecturerId != null
                && (quiz.getCreatedBy() == null || !quiz.getCreatedBy().getUserId().equals(lecturerId))) {
            throw new ConflictException("You are not the owner of this quiz");
        }

        List<QuizAttempt> completedAttempts = quizAttemptRepository.findByQuizId(quizId).stream()
                .filter(a -> a.getStatus() == QuizStatus.COMPLETED)
                .collect(java.util.stream.Collectors.toList());

        for (QuizAttempt attempt : completedAttempts) {
            List<StudentAnswer> studentAnswers = studentAnswerRepository.findAllByAttemptId(attempt.getId());
            double earnedPoints = 0.0;

            for (StudentAnswer ans : studentAnswers) {
                Question question = ans.getQuestion();
                if (question == null)
                    continue;

                double qPoints = question.getPoints() != null ? question.getPoints() : 0.0;
                boolean isAwarded = false;

                if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                    if (ans.getSelectedOptionIds() != null && !ans.getSelectedOptionIds().isEmpty()) {
                        Set<Long> correctOptionIds = question.getOptions().stream()
                                .filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect()))
                                .map(QuestionOption::getId)
                                .collect(java.util.stream.Collectors.toSet());
                        if (new java.util.HashSet<>(ans.getSelectedOptionIds()).equals(correctOptionIds)) {
                            isAwarded = true;
                        }
                    }
                } else if (question.getQuestionType() == QuestionType.SINGLE_CHOICE
                        || question.getQuestionType() == QuestionType.TRUE_FALSE) {
                    if (ans.getSelectedOption() != null) {
                        QuestionOption currentOption = questionOptionRepository
                                .findById(ans.getSelectedOption().getId()).orElse(null);
                        if (currentOption != null && Boolean.TRUE.equals(currentOption.getIsCorrect())) {
                            isAwarded = true;
                        }
                    }
                } else if (question.getQuestionType() == QuestionType.FILL_BLANK) {
                    if (ans.getAnswerText() != null && !ans.getAnswerText().isEmpty()) {
                        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                            boolean matched = question.getOptions().stream()
                                    .anyMatch(opt -> Boolean.TRUE.equals(opt.getIsCorrect())
                                            && opt.getContent().trim().equalsIgnoreCase(ans.getAnswerText().trim()));
                            if (matched) {
                                isAwarded = true;
                            }
                        }
                    }
                } else if (question.getQuestionType() == QuestionType.ESSAY) {
                    // Giá»¯ nguyÃªn Ä‘iá»ƒm cÅ© vÃ¬ Ä‘Ã£ Ä‘Æ°á»£c giáº£ng viÃªn cháº¥m thá»§ cÃ´ng
                    isAwarded = ans.getIsAwarded() != null ? ans.getIsAwarded() : false;
                    earnedPoints += ans.getEarnedPoints() != null ? ans.getEarnedPoints() : 0.0;
                    continue;
                }

                ans.setIsAwarded(isAwarded);
                ans.setEarnedPoints(isAwarded ? qPoints : 0.0);
                earnedPoints += ans.getEarnedPoints();
            }

            studentAnswerRepository.saveAll(studentAnswers);

            double totalMaxPoints = quiz.getQuestions().stream()
                    .mapToDouble(q -> q.getPoints() != null ? q.getPoints() : 0.0).sum();

            double finalScore = 0.0;
            if (totalMaxPoints > 0) {
                finalScore = (earnedPoints / totalMaxPoints) * 10.0;
                finalScore = Math.round(finalScore * 100.0) / 100.0;
            }
            attempt.setScore(finalScore);
        }
        quizAttemptRepository.saveAll(completedAttempts);
        log.info("Regraded quiz {} successfully for {} attempts", quizId, completedAttempts.size());
    }

    @Override
    @Transactional(readOnly = true)
    public edufit_com_lms.module.quiz.dto.response.QuizAnalyticsResponse getQuizAnalytics(Long quizId,
            Long lecturerId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFound("Can not found quiz"));

        if (lecturerId != null
                && (quiz.getCreatedBy() == null || !quiz.getCreatedBy().getUserId().equals(lecturerId))) {
            throw new ConflictException("You are not the owner of this quiz");
        }

        List<QuizAttempt> completedAttempts = quizAttemptRepository.findByQuizId(quizId).stream()
                .filter(a -> a.getStatus() == QuizStatus.COMPLETED)
                .collect(java.util.stream.Collectors.toList());

        int totalAttempts = completedAttempts.size();
        if (totalAttempts == 0) {
            return edufit_com_lms.module.quiz.dto.response.QuizAnalyticsResponse.builder()
                    .quizId(quizId)
                    .quizTitle(quiz.getTitle())
                    .totalAttempts(0)
                    .scoreDistribution(new java.util.HashMap<>())
                    .hardestQuestions(new java.util.HashMap<>())
                    .build();
        }

        double sumScore = 0;
        double maxScore = -1;
        double minScore = 11;
        int passed = 0;

        java.util.Map<String, Integer> distribution = new java.util.LinkedHashMap<>();
        distribution.put("0.0 - 2.0", 0);
        distribution.put("2.1 - 4.0", 0);
        distribution.put("4.1 - 6.0", 0);
        distribution.put("6.1 - 8.0", 0);
        distribution.put("8.1 - 10.0", 0);

        java.util.Map<Long, Integer> wrongAnswerCountMap = new java.util.HashMap<>();
        java.util.Map<Long, String> questionContentMap = new java.util.HashMap<>();

        for (QuizAttempt attempt : completedAttempts) {
            double score = attempt.getScore() != null ? attempt.getScore() : 0.0;
            sumScore += score;
            if (score > maxScore)
                maxScore = score;
            if (score < minScore)
                minScore = score;
            if (score >= 5.0)
                passed++;

            if (score <= 2.0)
                distribution.put("0.0 - 2.0", distribution.get("0.0 - 2.0") + 1);
            else if (score <= 4.0)
                distribution.put("2.1 - 4.0", distribution.get("2.1 - 4.0") + 1);
            else if (score <= 6.0)
                distribution.put("4.1 - 6.0", distribution.get("4.1 - 6.0") + 1);
            else if (score <= 8.0)
                distribution.put("6.1 - 8.0", distribution.get("6.1 - 8.0") + 1);
            else
                distribution.put("8.1 - 10.0", distribution.get("8.1 - 10.0") + 1);

            List<StudentAnswer> answers = studentAnswerRepository.findAllByAttemptId(attempt.getId());
            for (StudentAnswer ans : answers) {
                if (ans.getQuestion() != null) {
                    if (ans.getIsAwarded() == null || !ans.getIsAwarded()) {
                        wrongAnswerCountMap.put(ans.getQuestion().getId(),
                                wrongAnswerCountMap.getOrDefault(ans.getQuestion().getId(), 0) + 1);
                        questionContentMap.putIfAbsent(ans.getQuestion().getId(), ans.getQuestion().getContent());
                    }
                }
            }
        }

        double averageScore = Math.round((sumScore / totalAttempts) * 100.0) / 100.0;
        double passRate = Math.round(((double) passed / totalAttempts) * 10000.0) / 100.0;

        java.util.Map<String, Integer> hardestQuestions = wrongAnswerCountMap.entrySet().stream()
                .sorted(java.util.Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(java.util.stream.Collectors.toMap(
                        e -> questionContentMap.get(e.getKey()),
                        java.util.Map.Entry::getValue,
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new));

        return edufit_com_lms.module.quiz.dto.response.QuizAnalyticsResponse.builder()
                .quizId(quizId)
                .quizTitle(quiz.getTitle())
                .totalAttempts(totalAttempts)
                .averageScore(averageScore)
                .highestScore(maxScore == -1 ? 0.0 : maxScore)
                .lowestScore(minScore == 11 ? 0.0 : minScore)
                .passedCount(passed)
                .failedCount(totalAttempts - passed)
                .passRate(passRate)
                .scoreDistribution(distribution)
                .hardestQuestions(hardestQuestions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportQuizScoresToExcel(Long quizId, Long lecturerId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFound("Can not found quiz"));

        if (lecturerId != null
                && (quiz.getCreatedBy() == null || !quiz.getCreatedBy().getUserId().equals(lecturerId))) {
            throw new ConflictException("You are not the owner of this quiz");
        }

        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);

        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("BangDiem");

            // Create Header Font
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.BLUE.getIndex());

            // Create Header Style
            org.apache.poi.ss.usermodel.CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // Row for Header
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);

            // Header labels
            String[] columns = { "STT", "Há» vÃ  TÃªn", "Email", "Thá»i gian báº¯t Ä‘áº§u", "Thá»i gian ná»™p",
                    "Tráº¡ng thÃ¡i", "Äiá»ƒm sá»‘" };
            for (int i = 0; i < columns.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                    .ofPattern("dd/MM/yyyy HH:mm:ss");

            int rowIdx = 1;
            for (QuizAttempt attempt : attempts) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(
                        attempt.getStudent().getFullName() != null ? attempt.getStudent().getFullName() : "N/A");
                row.createCell(2).setCellValue(
                        attempt.getStudent().getEmail() != null ? attempt.getStudent().getEmail() : "N/A");

                String startTimeStr = attempt.getStartTime() != null ? attempt.getStartTime().format(formatter) : "";
                row.createCell(3).setCellValue(startTimeStr);

                String endTimeStr = attempt.getEndTime() != null ? attempt.getEndTime().format(formatter) : "";
                row.createCell(4).setCellValue(endTimeStr);

                row.createCell(5).setCellValue(attempt.getStatus() != null ? attempt.getStatus().name() : "");

                double score = attempt.getScore() != null ? attempt.getScore() : 0.0;
                row.createCell(6).setCellValue(score);
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (java.io.IOException e) {
            log.error("Lá»—i khi táº¡o file Excel báº£ng Ä‘iá»ƒm bÃ i thi {}", quizId, e);
            throw new RuntimeException("Lá»—i há»‡ thá»‘ng khi xuáº¥t file Excel");
        }
    }
}
