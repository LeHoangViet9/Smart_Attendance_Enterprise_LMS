package edufit_com_lms.module.quiz.service.impl;

import edufit_com_lms.common.exception.AppException;
import edufit_com_lms.module.auth.entity.Role;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.auth.repository.UserRepository;
import edufit_com_lms.module.lms.entity.Major;
import edufit_com_lms.module.lms.entity.SchoolClass;
import edufit_com_lms.module.lms.repository.MajorRepository;
import edufit_com_lms.module.lms.repository.SchoolClassRepository;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizServiceImpl implements QuizService {
    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;
    private final UserRepository userRepository;
    private final MajorRepository majorRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    @Override
    public QuizResponse createQuiz(QuizRequest request, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User does not exist with id: " + creatorId));

        Major major = resolveMajorForCreate(request, creator);

        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .accessCode(request.getAccessCode())
                .requiresProctoring(Boolean.TRUE.equals(request.getRequiresProctoring()))
                .createdBy(creator)
                .major(major)
                .build();

        if (request.getClassIds() != null && !request.getClassIds().isEmpty()) {
            List<SchoolClass> classes = schoolClassRepository.findAllById(request.getClassIds());
            quiz.setClasses(classes);
        }

        return quizMapper.toResponse(quizRepository.save(quiz));
    }

    @Override
    public QuizResponse updateQuiz(Long quizId, QuizRequest request, Long lecturerId) {
        Quiz quiz = findQuizOrThrow(quizId);
        validateOwner(quiz, lecturerId, "update");

        if (quizAttemptRepository.existsByQuizIdAndStatus(quizId, QuizStatus.IN_PROGRESS)) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot update quiz while students are taking it.");
        }

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setTimeLimitMinutes(request.getTimeLimitMinutes());
        quiz.setStartTime(request.getStartTime());
        quiz.setEndTime(request.getEndTime());
        quiz.setAccessCode(request.getAccessCode());
        quiz.setRequiresProctoring(Boolean.TRUE.equals(request.getRequiresProctoring()));

        if (request.getMajorId() != null) {
            quiz.setMajor(findMajorOrThrow(request.getMajorId()));
        }

        if (request.getClassIds() != null) {
            List<SchoolClass> classes = schoolClassRepository.findAllById(request.getClassIds());
            quiz.setClasses(classes);
        }

        return quizMapper.toResponse(quizRepository.save(quiz));
    }

    @Override
    public void deleteQuiz(Long quizId, Long lecturerId) {
        Quiz quiz = findQuizOrThrow(quizId);
        validateOwner(quiz, lecturerId, "delete");
        quizRepository.deleteById(quizId);
    }

    @Override
    public Page<QuizResponse> getQuizzes(String keyword, String searchBy, UUID majorId, Long lecturerId, Pageable pageable) {
        if (lecturerId != null) {
            User user = userRepository.findById(lecturerId).orElse(null);
            if (user != null && user.getLecturerProfile() != null && user.getLecturerProfile().getMajor() != null) {
                majorId = user.getLecturerProfile().getMajor().getId();
            }
        }

        Page<Quiz> quizPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            quizPage = quizRepository.searchByKeyword(keyword.trim(), majorId, pageable);
        } else if (majorId != null) {
            quizPage = quizRepository.searchByKeyword("", majorId, pageable);
        } else {
            quizPage = quizRepository.findAll(pageable);
        }

        return quizPage.map(quizMapper::toResponse);
    }

    @Override
    public QuizResponse findQuizById(Long quizId) {
        return quizMapper.toResponse(findQuizOrThrow(quizId));
    }

    private Major resolveMajorForCreate(QuizRequest request, User creator) {
        if (request.getMajorId() != null) {
            return findMajorOrThrow(request.getMajorId());
        }
        if (creator.getRole() == Role.LECTURER && creator.getLecturerProfile() != null) {
            return creator.getLecturerProfile().getMajor();
        }
        return null;
    }

    private Major findMajorOrThrow(UUID majorId) {
        return majorRepository.findById(majorId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Major does not exist with id: " + majorId));
    }

    private Quiz findQuizOrThrow(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Quiz does not exist with id: " + quizId));
    }

    private void validateOwner(Quiz quiz, Long lecturerId, String action) {
        if (lecturerId != null && (quiz.getCreatedBy() == null || !quiz.getCreatedBy().getUserId().equals(lecturerId))) {
            throw new AppException(HttpStatus.FORBIDDEN, "You do not have permission to " + action + " this quiz.");
        }
    }
}
