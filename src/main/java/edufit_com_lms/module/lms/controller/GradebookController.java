package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.lms.dto.request.UpdateGradebookRequest;
import edufit_com_lms.module.lms.dto.response.GradebookItemResponse;
import edufit_com_lms.module.lms.service.GradebookService;
import edufit_com_lms.security.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<GradebookItemResponse>>> getGradebook(@PathVariable UUID classId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long lecturerId = ((CustomUserDetail) authentication.getPrincipal()).getId();
        
        List<GradebookItemResponse> response = gradebookService.getGradebookForClass(classId, lecturerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/classes/{classId}/sync")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<GradebookItemResponse>>> syncGradebook(@PathVariable UUID classId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long lecturerId = ((CustomUserDetail) authentication.getPrincipal()).getId();
        
        List<GradebookItemResponse> response = gradebookService.syncGradebookForClass(classId, lecturerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{gradebookId}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateGradebook(
            @PathVariable UUID gradebookId,
            @Valid @RequestBody UpdateGradebookRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long lecturerId = ((CustomUserDetail) authentication.getPrincipal()).getId();
        
        gradebookService.updateGradebook(gradebookId, request, lecturerId);
        return ResponseEntity.ok(ApiResponse.success("Luu diem thanh cong"));
    }

    @GetMapping("/my-grades")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<GradebookItemResponse>>> getMyGrades() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long studentId = ((CustomUserDetail) authentication.getPrincipal()).getId();
        
        List<GradebookItemResponse> response = gradebookService.getMyGrades(studentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/classes/{classId}/export")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<byte[]> exportGradebook(@PathVariable UUID classId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long lecturerId = ((CustomUserDetail) authentication.getPrincipal()).getId();
        byte[] data = gradebookService.exportGradebookForClass(classId, lecturerId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "gradebook-" + classId + ".xlsx");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
