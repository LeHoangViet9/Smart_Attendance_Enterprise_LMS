package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.lms.dto.response.LecturerStatsResponse;
import edufit_com_lms.module.lms.service.LecturerService;
import edufit_com_lms.security.CustomUserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lecturer")
@RequiredArgsConstructor
public class LecturerController {

    private final LecturerService lecturerService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<LecturerStatsResponse>> getLecturerStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long lecturerId = null;
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail) {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            lecturerId = userDetails.getId();
        }

        if (lecturerId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lecturer ID not found from context", HttpStatus.BAD_REQUEST));
        }

        LecturerStatsResponse response = lecturerService.getLecturerStats(lecturerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
