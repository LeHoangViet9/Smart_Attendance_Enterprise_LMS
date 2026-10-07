package edufit_com_lms.module.auth.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.auth.dto.response.AdminStatsResponse;
import edufit_com_lms.module.auth.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
                "Láº¥y thá»‘ng kÃª thÃ nh cÃ´ng",
                null,
                adminService.getDashboardStats(),
                HttpStatus.OK), HttpStatus.OK);
    }

}
