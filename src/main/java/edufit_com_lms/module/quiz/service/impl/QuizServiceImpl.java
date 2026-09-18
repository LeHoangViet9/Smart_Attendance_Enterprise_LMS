package edufit_com_lms.module.quiz.service.impl;

import edufit_com_lms.common.exception.AppException;
import edufit_com_lms.module.quiz.dto.request.QuizRequest;
import edufit_com_lms.module.quiz.dto.response.QuizResponse;
import edufit_com_lms.module.quiz.entity.Quiz;
import edufit_com_lms.module.quiz.repository.QuizRepository;
import edufit_com_lms.module.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import edufit_com_lms.module.auth.repository.UserRepository;
import edufit_com_lms.module.lms.repository.MajorRepository;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.lms.entity.Major;
import edufit_com_lms.module.quiz.mapper.QuizMapper;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizServiceImpl implements QuizService {
    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;
    private final UserRepository userRepository;
    private final MajorRepository majorRepository;

    @Override
    public QuizResponse createQuiz(QuizRequest request, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại với id: " + creatorId));
                
        Major major = null;
        if (request.getMajorId() != null) {
            major = majorRepository.findById(request.getMajorId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Ngành học không tồn tại với id: " + request.getMajorId()));
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
                .createdBy(creator)
                .major(major)
                .build();

        Quiz savedQuiz = quizRepository.save(quiz);
        return quizMapper.toResponse(savedQuiz);
    }

    @Override
    public QuizResponse updateQuiz(Long quizId, QuizRequest request, Long lecturerId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Quiz không tồn tại với id: " + quizId));

        if (lecturerId != null && (quiz.getCreatedBy() == null || !quiz.getCreatedBy().getUserId().equals(lecturerId))) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền sửa Quiz này vì không phải là người tạo.");
        }

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setTimeLimitMinutes(request.getTimeLimitMinutes());
        quiz.setStartTime(request.getStartTime());
        quiz.setEndTime(request.getEndTime());
        
        if (request.getMajorId() != null) {
            Major major = majorRepository.findById(request.getMajorId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Ngành học không tồn tại với id: " + request.getMajorId()));
            quiz.setMajor(major);
        }

        Quiz updatedQuiz = quizRepository.save(quiz);
        return quizMapper.toResponse(updatedQuiz);
    }

    @Override
    public void deleteQuiz(Long quizId, Long lecturerId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Quiz không tồn tại với id: " + quizId));
                
        if (lecturerId != null && (quiz.getCreatedBy() == null || !quiz.getCreatedBy().getUserId().equals(lecturerId))) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền xóa Quiz này vì không phải là người tạo.");
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
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Quiz không tồn tại với id: " + quizId));
        return quizMapper.toResponse(quiz);
    }
}
