package edufit_com_lms.module.auth.service.impl;

import edufit_com_lms.common.exception.ConflictException;
import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.common.exception.UnauthorizedException;
import edufit_com_lms.module.auth.dto.request.AdminRegisterRequest;
import edufit_com_lms.module.auth.dto.request.ChangePasswordRequest;
import edufit_com_lms.module.auth.dto.request.LoginRequest;
import edufit_com_lms.module.auth.dto.response.UserResponse;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.auth.repository.UserRepository;
import edufit_com_lms.module.auth.service.AuthService;
import edufit_com_lms.module.auth.service.TokenService;
import edufit_com_lms.security.CustomUserDetail;
import edufit_com_lms.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    @Override
    public UserResponse register(AdminRegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("Email already exists");
        }
        if (userRepository.existsByCode(registerRequest.getCode())) {
            throw new ConflictException("Code already exists");
        }
        String password = registerRequest.getEmail(); // Đặt mật khẩu mặc định là email
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setCode(registerRequest.getCode());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(registerRequest.getRole());
        user.setFullName(registerRequest.getFullName());
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        return UserResponse.builder()
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .code(savedUser.getCode())
                .fullName(savedUser.getFullName())
                .isActive(savedUser.getIsActive())
                .password(password)
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    private String generatePassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";
        String charSet = upper + lower + digits + special;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            int randomIndex = random.nextInt(charSet.length());
            password.append(charSet.charAt(randomIndex));
        }

        return password.toString();
    }

    @Override
    public UserResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFound("Email không tồn tai"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Sai mật khẩu hoặc email");
        }
        if (!user.getIsActive()) {
            throw new UnauthorizedException("Tài khoản không hợp lệ");
        }
        String accessToken = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().toString());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        tokenService.saveRefreshToken(user.getEmail(), refreshToken);
        return UserResponse.builder()
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(user.getFullName())
                .isActive(user.getIsActive())
                .avatarUrl(user.getAvatarUrl())
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .build();
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFound("Can not found email"));
        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new UnauthorizedException("Old password wrong");
        }

        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new UnauthorizedException("New password is not same old password");
        }
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail) {
            CustomUserDetail customUserDetail = (CustomUserDetail) authentication.getPrincipal();
            tokenService.revokeRefreshToken(customUserDetail.getUsername());
            SecurityContextHolder.clearContext();
        }
    }

}
