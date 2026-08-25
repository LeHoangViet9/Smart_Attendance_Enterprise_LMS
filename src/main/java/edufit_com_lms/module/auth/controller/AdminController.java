package edufit_com_lms.module.auth.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.auth.dto.response.AdminStatsResponse;
import edufit_com_lms.module.auth.dto.response.UserResponse;
import edufit_com_lms.module.auth.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getDashboardStats() {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Lấy thống kê thành công",
                null,
                adminService.getDashboardStats(),
                HttpStatus.OK), HttpStatus.OK);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Lấy danh sách người dùng thành công",
                null,
                adminService.getAllUsersExcludingAdmins(),
                HttpStatus.OK), HttpStatus.OK);
    }
}
