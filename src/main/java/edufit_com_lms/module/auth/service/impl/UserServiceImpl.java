package edufit_com_lms.module.auth.service.impl;

import edufit_com_lms.common.exception.ConflictException;
import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.module.auth.dto.request.AdminRegisterRequest;
import edufit_com_lms.module.auth.dto.response.UserResponse;
import edufit_com_lms.module.auth.entity.Role;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.auth.mapper.UserMapper;
import edufit_com_lms.module.auth.repository.UserRepository;
import edufit_com_lms.module.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserResponse> findAll(String keyword, Role role, Boolean isActive, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(keyword, role, isActive, pageable);
        return users.map(userMapper::toUserResponse);
    }

    @Override
    public UserResponse updateUser(Long id, AdminRegisterRequest userRequest) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFound("Can not found user"));
        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()
                && !userRepository.findByEmail(userRequest.getEmail()).get().getUserId().equals(id)) {
            throw new ConflictException("Email already exists");
        }
        if (userRepository.findByCode(userRequest.getCode()).isPresent()
                && !userRepository.findByCode(userRequest.getCode()).get().getUserId().equals(id)) {
            throw new ConflictException("Code already exists");
        }
        user.setEmail(userRequest.getEmail());
        user.setCode(userRequest.getCode());
        user.setRole(userRequest.getRole());
        user.setFullName(userRequest.getFullName());
        User savedUser = userRepository.save(user);
        return UserResponse.builder()
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .code(savedUser.getCode())
                .fullName(savedUser.getFullName())
                .isActive(savedUser.getIsActive())
                .password(null)
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    // Removed generatePassword

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFound("Can not found user"));
        user.setIsActive(false);
        userRepository.save(user);
    }
}
