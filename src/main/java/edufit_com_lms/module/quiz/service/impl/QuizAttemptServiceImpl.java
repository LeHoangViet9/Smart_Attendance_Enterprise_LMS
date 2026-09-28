package edufit_com_lms.module.quiz.service.impl;

import edufit_com_lms.common.exception.ConflictException;
import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.auth.repository.UserRepository;
import edufit_com_lms.module.quiz.dto.request.SubmitQuizRequest;
import edufit_com_lms.module.quiz.dto.response.QuizAttemptResponse;
import edufit_com_lms.module.quiz.dto.response.QuizReviewResponse;
import edufit_com_lms.module.quiz.entity.Quiz;
import edufit_com_lms.module.quiz.entity.QuizAttempt;
import edufit_com_lms.module.quiz.entity.QuizStatus;
import edufit_com_lms.module.quiz.entity.Question;
import edufit_com_lms.module.quiz.entity.QuestionOption;
import edufit_com_lms.module.quiz.entity.QuestionType;
import edufit_com_lms.module.quiz.entity.StudentAnswer;
import edufit_com_lms.module.quiz.entity.ReviewType;
import edufit_com_lms.module.quiz.dto.request.StudentAnswerRequest;
import edufit_com_lms.module.quiz.repository.QuestionRepository;
import edufit_com_lms.module.quiz.repository.QuestionOptionRepository;
import edufit_com_lms.module.quiz.repository.StudentAnswerRepository;
import edufit_com_lms.module.quiz.mapper.QuizAttemptMapper;
import edufit_com_lms.module.quiz.repository.QuizAttemptRepository;
import edufit_com_lms.module.quiz.repository.QuizRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.concurrent.TimeUnit;
import edufit_com_lms.module.quiz.service.QuizAttemptService;
import edufit_com_lms.module.quiz.service.AIGradingService;
import edufit_com_lms.module.quiz.dto.response.AIGradeSuggestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;

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
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApplicationEventPublisher eventPublisher;
    private final AIGradingService aiGradingService;

    @Override
    public QuizAttemptResponse startAttempt(Long quizId, Long studentId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFound("Can not found quiz"));
        User student = userRepository.findById(studentId).orElseThrow(() -> new ResourceNotFound("Can not found user"));
        List<QuizAttempt> existingAttempts = quizAttemptRepository.findByQuizIdAndStudentUserId(quizId, studentId);

        // Chặn thi lại đối với những bài thi yêu cầu quét mặt (chỉ cho phép thi 1 lần)
        if (Boolean.TRUE.equals(quiz.getRequiresProctoring())) {
            boolean hasCompleted = existingAttempts.stream()
                    .anyMatch(a -> a.getStatus() == edufit_com_lms.module.quiz.entity.QuizStatus.COMPLETED);
            if (hasCompleted) {
                throw new ConflictException("Bài thi này yêu cầu giám sát (quét mặt) nên bạn chỉ được phép thi 1 lần duy nhất!");
            }
        }

        Optional<QuizAttempt> inProgressAttempt = existingAttempts.stream()
                .filter(a -> a.getStatus() == QuizStatus.IN_PROGRESS)
                .findFirst();

        if (inProgressAttempt.isPresent()) {
            QuizAttempt existingAttempt = inProgressAttempt.get();
            // Khôi phục dữ liệu nháp từ Redis và nhả về cho sinh viên thi tiếp tục
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
                e.printStackTrace();
            }
            return res; // Trả về attempt cũ thay vì quăng lỗi
        }
        LocalDateTime now = LocalDateTime.now();
        // TEMP BYPASS: Commented out for testing purposes
        // if (quiz.getStartTime() != null && now.isBefore(quiz.getStartTime())) {
        //     throw new ConflictException("The test has not yet started!");
        // }
        if (quiz.getEndTime() != null && now.isAfter(quiz.getEndTime())) {
            throw new ConflictException("This test has expired!");
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

        // Nếu quiz có giới hạn thời gian, check xem nộp muộn không (du di 1 phút do độ
        // trễ mạng)
        if (timeLimit != null) {
            LocalDateTime deadline = startTime.plusMinutes(timeLimit).plusMinutes(1);
            if (submitTime.isAfter(deadline)) {
                quizAttempt.setStatus(QuizStatus.ABANDONED);
            }
        }

        // Check proctoring image if required
        if (Boolean.TRUE.equals(quizAttempt.getQuiz().getRequiresProctoring())) {
            if (submitRequest.getProctoringImageUrl() == null || submitRequest.getProctoringImageUrl().trim().isEmpty()) {
                throw new ConflictException("Bài thi này yêu cầu xác thực khuôn mặt! Vui lòng cung cấp ảnh đính kèm.");
            }
            quizAttempt.setProctoringImageUrl(submitRequest.getProctoringImageUrl());
        }

        double earnedPoints = 0.0;
        List<StudentAnswer> studentAnswers = new ArrayList<>();

        if (submitRequest.getAnswers() != null && !submitRequest.getAnswers().isEmpty()) {
            for (StudentAnswerRequest answerReq : submitRequest.getAnswers()) {
                Question question = questionRepository.findById(answerReq.getQuestionId())
                        .orElseThrow(() -> new ResourceNotFound("Can not found question"));

                boolean isAwarded = false;
                QuestionOption selectedOption = null;

                if (answerReq.getSelectedOptionId() != null) {
                    selectedOption = questionOptionRepository.findById(answerReq.getSelectedOptionId()).orElse(null);
                }

                if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE
                        || question.getQuestionType() == QuestionType.SINGLE_CHOICE
                        || question.getQuestionType() == QuestionType.TRUE_FALSE) {
                    if (selectedOption != null && Boolean.TRUE.equals(selectedOption.getIsCorrect())) {
                        isAwarded = true;
                        earnedPoints += question.getPoints();
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
                                earnedPoints += question.getPoints();
                            }
                        }
                    }
                }

                StudentAnswer studentAnswer = StudentAnswer.builder()
                        .attempt(quizAttempt)
                        .question(question)
                        .selectedOption(selectedOption)
                        .answerText(answerReq.getAnswerText())
                        .isAwarded(isAwarded)
                        .earnedPoints(isAwarded ? question.getPoints() : 0.0)
                        .build();
                studentAnswers.add(studentAnswer);
            }
            studentAnswerRepository.saveAll(studentAnswers);
        }

        double totalMaxPoints = quizAttempt.getQuiz().getQuestions().stream()
                .mapToDouble(Question::getPoints).sum();

        double finalScore = 0.0;
        if (totalMaxPoints > 0) {
            finalScore = (earnedPoints / totalMaxPoints) * 10.0;
            finalScore = Math.round(finalScore * 100.0) / 100.0;
        }

        quizAttempt.setScore(finalScore);
        quizAttempt.setEndTime(submitTime);
        quizAttempt.setStatus(QuizStatus.COMPLETED);

        // Xóa hoàn toàn bản nháp trên Redis để dọn bề mặt RAM
        stringRedisTemplate.delete("quiz:attempt:" + attemptId);

        QuizAttempt savedAttempt = quizAttemptRepository.save(quizAttempt);

        // Bắn thông báo cho người tạo đề (Giảng viên / Admin)
        if (savedAttempt.getQuiz() != null && savedAttempt.getQuiz().getCreatedBy() != null) {
            eventPublisher.publishEvent(edufit_com_lms.module.notification.event.NotificationEvent.builder()
                    .title("Có sinh viên nộp bài thi")
                    .message("Sinh viên " + savedAttempt.getStudent().getFullName() + " vừa hoàn thành bài thi: " + savedAttempt.getQuiz().getTitle())
                    .type("SYSTEM_LOG")
                    .recipientId(savedAttempt.getQuiz().getCreatedBy().getUserId())
                    .build());
        }

        return quizAttemptMapper.toResponse(savedAttempt);
    }

    @Override
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
            // Lưu và set hạn tự sát sau 1 ngày nếu không xảo bớt bộ nhớ
            stringRedisTemplate.opsForValue().set(key, json, 1, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
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

        if (quizAttempt.getStatus() != QuizStatus.COMPLETED) {
            throw new ConflictException("You can only review completed attempts");
        }

        Quiz quiz = quizAttempt.getQuiz();

        if (studentId != null && quiz.getReviewType() != null) {
            if (quiz.getReviewType() == ReviewType.NEVER) {
                throw new ConflictException("Giáo viên không cho phép xem lại bài thi này.");
            }
            if (quiz.getReviewType() == ReviewType.AFTER_DEADLINE) {
                if (quiz.getEndTime() != null && LocalDateTime.now().isBefore(quiz.getEndTime())) {
                    throw new ConflictException("Chưa đến thời gian xem lại bài (Phải chờ qua hạn kết thúc).");
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
            }

            QuizReviewResponse.ReviewStudentAnswerDto answerDto = null;
            Optional<StudentAnswer> ansOpt = studentAnswers.stream()
                    .filter(a -> a.getQuestion().getId().equals(question.getId())).findFirst();
            if (ansOpt.isPresent()) {
                StudentAnswer ans = ansOpt.get();
                answerDto = QuizReviewResponse.ReviewStudentAnswerDto.builder()
                        .id(ans.getId())
                        .selectedOptionId(ans.getSelectedOption() != null ? ans.getSelectedOption().getId() : null)
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
    public Page<QuizAttemptResponse> getAttemptsByQuizId(Long quizId, Pageable pageable) {
        Page<QuizAttempt> attempts = quizAttemptRepository.findAllByQuizIdOrderByStartTimeDesc(quizId, pageable);
        return attempts.map(quizAttemptMapper::toResponse);
    }

    @Override
    @Transactional
    public QuizAttemptResponse gradeQuizAttempt(Long attemptId, edufit_com_lms.module.quiz.dto.request.GradeEssayRequest request, Long lecturerId) {
        QuizAttempt quizAttempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFound("Can not found quiz attempt"));

        // Verify ownership (Lecturer should own the quiz)
        if (lecturerId != null && !quizAttempt.getQuiz().getCreatedBy().getUserId().equals(lecturerId)) {
            throw new ConflictException("You are not the owner of this quiz");
        }

        List<StudentAnswer> studentAnswers = studentAnswerRepository.findAllByAttemptId(attemptId);

        for (edufit_com_lms.module.quiz.dto.request.GradeEssayRequest.QuestionGrade qg : request.getGrades()) {
            StudentAnswer answer = studentAnswers.stream()
                    .filter(a -> a.getId().equals(qg.getStudentAnswerId()))
                    .findFirst()
                    .orElse(null);

            if (answer != null) {
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
                .mapToDouble(Question::getPoints).sum();

        double finalScore = 0.0;
        if (totalMaxPoints > 0) {
            finalScore = (totalEarned / totalMaxPoints) * 10.0;
            finalScore = Math.round(finalScore * 100.0) / 100.0;
        }

        quizAttempt.setScore(finalScore);
        return quizAttemptMapper.toResponse(quizAttemptRepository.save(quizAttempt));
    }

    @Override
    public AIGradeSuggestionResponse suggestGradeWithAI(Long attemptId, Long answerId, Long lecturerId) {
        QuizAttempt quizAttempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFound("Can not found quiz attempt"));

        if (lecturerId != null && !quizAttempt.getQuiz().getCreatedBy().getUserId().equals(lecturerId)) {
            throw new ConflictException("You are not the owner of this quiz");
        }

        StudentAnswer studentAnswer = studentAnswerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFound("Can not found student answer"));

        if (!studentAnswer.getAttempt().getId().equals(attemptId)) {
            throw new ConflictException("Answer does not belong to this attempt");
        }

        String questionContent = studentAnswer.getQuestion().getContent();
        String answerText = studentAnswer.getAnswerText();
        double maxPoints = studentAnswer.getQuestion().getPoints() != null ? studentAnswer.getQuestion().getPoints() : 0.0;

        return aiGradingService.suggestGrade(questionContent, answerText, maxPoints);
    }
}
