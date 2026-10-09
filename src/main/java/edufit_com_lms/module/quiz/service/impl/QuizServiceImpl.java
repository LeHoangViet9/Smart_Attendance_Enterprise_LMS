package edufit_com_lms.module.quiz.service.impl;

import edufit_com_lms.common.exception.AppException;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.auth.repository.UserRepository;
import edufit_com_lms.module.lms.entity.Major;
import edufit_com_lms.module.lms.repository.MajorRepository;
import edufit_com_lms.module.quiz.dto.request.QuizRequest;
import edufit_com_lms.module.quiz.dto.response.QuizResponse;
import edufit_com_lms.module.quiz.entity.Quiz;
import edufit_com_lms.module.quiz.entity.QuizStatus;
import edufit_com_lms.module.quiz.mapper.QuizMapper;
import edufit_com_lms.module.quiz.repository.QuizAttemptRepository;
import edufit_com_lms.module.quiz.repository.QuizRepository;
import edufit_com_lms.module.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizServiceImpl implements QuizService {
    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;
    private final UserRepository userRepository;
    private final MajorRepository majorRepository;
    private final edufit_com_lms.module.lms.repository.SchoolClassRepository schoolClassRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    @Override
    public QuizResponse createQuiz(QuizRequest request, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "NgÆ°á»i dÃ¹ng khÃ´ng tá»“n táº¡i vá»›i id: " + creatorId));
                
        Major major = null;
        if (request.getMajorId() != null) {
            major = majorRepository.findById(request.getMajorId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "NgÃ nh há»c khÃ´ng tá»“n táº¡i vá»›i id: " + request.getMajorId()));
        } else {
            if (creator.getRole() == edufit_com_lms.module.auth.entity.Role.LECTURER && creator.getLecturerProfile() != null) {
                major = creator.getLecturerProfile().getMajor();
            }
        }

        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .accessCode(request.getAccessCode())
                .requiresProctoring(request.getRequiresProctoring() != null ? request.getRequiresProctoring() : false)
                .createdBy(creator)
                .major(major)
                .build();

        if (request.getClassIds() != null && !request.getClassIds().isEmpty()) {
            java.util.List<edufit_com_lms.module.lms.entity.SchoolClass> classes = schoolClassRepository.findAllById(request.getClassIds());
            quiz.setClasses(classes);
        }

        Quiz savedQuiz = quizRepository.save(quiz);
        return quizMapper.toResponse(savedQuiz);
    }

    @Override
    public QuizResponse updateQuiz(Long quizId, QuizRequest request, Long lecturerId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Quiz khÃ´ng tá»“n táº¡i vá»›i id: " + quizId));

        if (lecturerId != null && (quiz.getCreatedBy() == null || !quiz.getCreatedBy().getUserId().equals(lecturerId))) {
            throw new AppException(HttpStatus.FORBIDDEN, "Báº¡n khÃ´ng cÃ³ quyá»n sá»­a Quiz nÃ y vÃ¬ khÃ´ng pháº£i lÃ  ngÆ°á»i táº¡o.");
        }

        if (quizAttemptRepository.existsByQuizIdAndStatus(quizId, QuizStatus.IN_PROGRESS)) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot update quiz while students are taking it.");
        }

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setTimeLimitMinutes(request.getTimeLimitMinutes());
        quiz.setStartTime(request.getStartTime());
        quiz.setEndTime(request.getEndTime());
        quiz.setAccessCode(request.getAccessCode());
        quiz.setRequiresProctoring(request.getRequiresProctoring() != null ? request.getRequiresProctoring() : false);
        
        if (request.getMajorId() != null) {
            Major major = majorRepository.findById(request.getMajorId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "NgÃ nh há»c khÃ´ng tá»“n táº¡i vá»›i id: " + request.getMajorId()));
            quiz.setMajor(major);
        }

        if (request.getClassIds() != null) {
            java.util.List<edufit_com_lms.module.lms.entity.SchoolClass> classes = schoolClassRepository.findAllById(request.getClassIds());
            quiz.setClasses(classes);
        }

        Quiz updatedQuiz = quizRepository.save(quiz);
        return quizMapper.toResponse(updatedQuiz);
    }

    @Override
    public void deleteQuiz(Long quizId, Long lecturerId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Quiz khÃ´ng tá»“n táº¡i vá»›i id: " + quizId));
                
        if (lecturerId != null && (quiz.getCreatedBy() == null || !quiz.getCreatedBy().getUserId().equals(lecturerId))) {
            throw new AppException(HttpStatus.FORBIDDEN, "Báº¡n khÃ´ng cÃ³ quyá»n xÃ³a Quiz nÃ y vÃ¬ khÃ´ng pháº£i lÃ  ngÆ°á»i táº¡o.");
        }
        
        quizRepository.deleteById(quizId);
    }

    @Override
    public Page<QuizResponse> getQuizzes(String keyword, String searchBy, java.util.UUID majorId, Long lecturerId, Pageable pageable) {
        if (lecturerId != null) {
            User user = userRepository.findById(lecturerId).orElse(null);
            if (user != null && user.getLecturerProfile() != null && user.getLecturerProfile().getMajor() != null) {
                majorId = user.getLecturerProfile().getMajor().getId();
            }
        }

        Page<Quiz> quizPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            quizPage = quizRepository.searchByKeyword(kw, majorId, pageable);
        } else {
            if (majorId != null) {
                // Since there is no findByMajor_Id, use searchByKeyword with empty keyword
                quizPage = quizRepository.searchByKeyword("", majorId, pageable);
            } else {
                quizPage = quizRepository.findAll(pageable);
            }
        }

        return quizPage.map(quizMapper::toResponse);
    }

    @Override
    public QuizResponse findQuizById(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Quiz khÃ´ng tá»“n táº¡i vá»›i id: " + quizId));
        return quizMapper.toResponse(quiz);
    }
}
