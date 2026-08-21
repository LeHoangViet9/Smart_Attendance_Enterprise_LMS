package edufit_com_lms.module.auth.service;

import edufit_com_lms.module.auth.dto.request.AdminRegisterRequest;
import edufit_com_lms.module.auth.dto.request.LoginRequest;
import edufit_com_lms.module.auth.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(AdminRegisterRequest registerRequest);

    UserResponse login(LoginRequest loginRequest);

    void logout();
}
