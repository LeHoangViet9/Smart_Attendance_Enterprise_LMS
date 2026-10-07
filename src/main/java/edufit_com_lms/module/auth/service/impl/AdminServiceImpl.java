package edufit_com_lms.module.auth.service.impl;

import edufit_com_lms.module.auth.dto.response.AdminStatsResponse;
import edufit_com_lms.module.auth.mapper.UserMapper;
import edufit_com_lms.module.auth.repository.UserRepository;
import edufit_com_lms.module.auth.service.AdminService;
import edufit_com_lms.module.lms.repository.CourseRepository;
import edufit_com_lms.module.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final CourseRepository courseRepository;
    private final UserMapper userMapper;

    @Override
    public AdminStatsResponse getDashboardStats() {
        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalQuizzes(quizRepository.count())
                .totalCourses(courseRepository.count())
                .totalSubmissions(0) // omitted from requested 3 fields
                .build();
    }

}
