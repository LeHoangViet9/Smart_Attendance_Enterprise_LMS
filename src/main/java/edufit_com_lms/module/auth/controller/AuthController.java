package edufit_com_lms.module.auth.controller;

import edufit_com_lms.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edufit_com_lms.module.auth.dto.request.AdminRegisterRequest;
import edufit_com_lms.module.auth.dto.request.LoginRequest;
import edufit_com_lms.module.auth.dto.response.UserResponse;
import edufit_com_lms.module.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody AdminRegisterRequest request) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Cấp tài khoản thành công",
                null,
                authService.register(request),
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest request) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Đăng nhập thành công",
                null,
                authService.login(request),
                HttpStatus.OK
        ),HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        authService.logout();
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Đăng xuất thành công",
                null,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }
}
