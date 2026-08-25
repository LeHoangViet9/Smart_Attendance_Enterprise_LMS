package edufit_com_lms.module.auth.service.impl;

import edufit_com_lms.module.auth.dto.response.AdminStatsResponse;
import edufit_com_lms.module.auth.dto.response.UserResponse;
import edufit_com_lms.module.auth.entity.Role;
import edufit_com_lms.module.auth.repository.UserRepository;
import edufit_com_lms.module.auth.service.AdminService;
import edufit_com_lms.module.quiz.repository.QuizRepository;
// Assuming QuizRepository, etc exists
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final QuizRepository quizRepository;

    @Override
    public AdminStatsResponse getDashboardStats() {
        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalQuizzes(quizRepository.count()) // from quiz module
                .totalCourses(0) // hardcode for now as requested
                .totalSubmissions(0) // omitted from requested 3 fields
                .build();
    }

    @Override
    public List<UserResponse> getAllUsersExcludingAdmins() {
        return userRepository.findByRoleNot(Role.ADMIN).stream().map(user -> UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .code(user.getCode())
                .address(user.getAddress())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build()).collect(Collectors.toList());
    }
}
