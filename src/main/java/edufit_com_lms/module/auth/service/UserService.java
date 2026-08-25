package edufit_com_lms.module.auth.service;

import edufit_com_lms.module.auth.dto.request.AdminRegisterRequest;
import edufit_com_lms.module.auth.dto.response.UserResponse;
import edufit_com_lms.module.auth.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponse> findAll(String keyword, Role role, Boolean isActive, Pageable pageable);

    UserResponse updateUser(Long id, AdminRegisterRequest userRequest);

    void deleteUser(Long id);

}
