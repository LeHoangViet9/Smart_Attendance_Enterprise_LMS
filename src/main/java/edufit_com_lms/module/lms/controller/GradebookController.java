package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.lms.dto.request.UpdateGradeRequest;
import edufit_com_lms.module.lms.dto.response.GradebookResponse;
import edufit_com_lms.module.lms.service.GradebookService;
import edufit_com_lms.security.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gradebooks")
@RequiredArgsConstructor
public class GradebookController {

    private final GradebookService gradebookService;

    @GetMapping("/classes/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<ApiResponse<List<GradebookResponse>>> getGradesByClass(@PathVariable UUID classId) {
        // In a real app, you would add an @PreAuthorize check here to ensure the lecturer owns the class.
        List<GradebookResponse> responses = gradebookService.getGradebooksByClass(classId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{gradebookId}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ApiResponse<GradebookResponse>> updateGrade(
            @PathVariable UUID gradebookId,
            @Valid @RequestBody UpdateGradeRequest request) {
        // Custom security checks (e.g. @securityService.isTeacherOfGradebook) can be added here
        GradebookResponse response = gradebookService.updateGrade(gradebookId, request);
        return ResponseEntity.ok(ApiResponse.success("Grade updated successfully", response));
    }

    @GetMapping("/my-grades")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<GradebookResponse>>> getMyGrades() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail) {
            CustomUserDetail user = (CustomUserDetail) authentication.getPrincipal();
            List<GradebookResponse> responses = gradebookService.getGradebooksByStudent(user.getId());
            return ResponseEntity.ok(ApiResponse.success(responses));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("User not found", org.springframework.http.HttpStatus.BAD_REQUEST));
    }

    @GetMapping("/classes/{classId}/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportExcel(@PathVariable UUID classId) {
        byte[] excelBytes = gradebookService.exportGradebookToExcel(classId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=BangDiem_" + classId + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
