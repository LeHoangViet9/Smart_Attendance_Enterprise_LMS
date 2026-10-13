package edufit_com_lms.module.auth.service;

import edufit_com_lms.module.auth.dto.request.AdminRegisterRequest;
import edufit_com_lms.module.auth.dto.request.ChangePasswordRequest;
import edufit_com_lms.module.auth.dto.request.ForgotPasswordRequest;
import edufit_com_lms.module.auth.dto.request.LoginRequest;
import edufit_com_lms.module.auth.dto.request.RefreshTokenRequest;
import edufit_com_lms.module.auth.dto.request.ResetPasswordRequest;
import edufit_com_lms.module.auth.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(AdminRegisterRequest registerRequest);

    UserResponse login(LoginRequest loginRequest);

    void changePassword(ChangePasswordRequest changePasswordRequest);

    String forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void logout();

    UserResponse refreshToken(RefreshTokenRequest request);
}
