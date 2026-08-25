package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.lms.dto.request.GradeSubmissionRequest;
import edufit_com_lms.module.lms.dto.response.SubmissionResponse;
import edufit_com_lms.module.lms.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/{id}/grade")
    public ResponseEntity<ApiResponse<SubmissionResponse>> gradeSubmission(
            @PathVariable("id") UUID submissionId,
            @Valid @RequestBody GradeSubmissionRequest request) {
        SubmissionResponse response = submissionService.gradeSubmission(submissionId, request);
        return ResponseEntity.ok(ApiResponse.success("Submission graded successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmissionById(@PathVariable("id") UUID submissionId) {
        SubmissionResponse response = submissionService.getSubmissionById(submissionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
