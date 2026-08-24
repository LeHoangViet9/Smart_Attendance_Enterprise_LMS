package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.lms.dto.*;
import edufit_com_lms.module.lms.service.AssignmentService;
import edufit_com_lms.module.lms.service.MinioStorageService;
import edufit_com_lms.module.lms.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final MinioStorageService minioStorageService;

    @PostMapping
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(
            @Valid @RequestBody CreateAssignmentRequest request) {
        AssignmentResponse response = assignmentService.createAssignment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Assignment created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignmentById(@PathVariable UUID id) {
        AssignmentResponse response = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getAssignmentsByClass(@PathVariable UUID classId) {
        List<AssignmentResponse> responses = assignmentService.getAssignmentsByClassId(classId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> updateAssignment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAssignmentRequest request) {
        AssignmentResponse response = assignmentService.updateAssignment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Assignment updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable UUID id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok(ApiResponse.success("Assignment deleted successfully", null));
    }

    @PostMapping("/upload-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUploadUrl(
            @Valid @RequestBody PresignedUrlRequest request) {
        PresignedUrlResponse response = minioStorageService.generatePresignedUploadUrl(request);
        return ResponseEntity.ok(ApiResponse.success("Pre-signed URL generated successfully", response));
    }

    @GetMapping("/download-url")
    public ResponseEntity<ApiResponse<String>> getPresignedDownloadUrl(
            @RequestParam String objectKey,
            @RequestParam(defaultValue = "60") int expiryMinutes) {
        String downloadUrl = minioStorageService.generatePresignedDownloadUrl(objectKey, expiryMinutes);
        return ResponseEntity.ok(ApiResponse.success("Download URL generated successfully", downloadUrl));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitAssignment(
            @PathVariable("id") UUID assignmentId,
            @Valid @RequestBody SubmitAssignmentRequest request) {
        SubmissionResponse response = submissionService.submitAssignment(assignmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Assignment submitted successfully", response));
    }

    @GetMapping("/{id}/submissions")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getSubmissionsForAssignment(@PathVariable("id") UUID assignmentId) {
        List<SubmissionResponse> responses = submissionService.getSubmissionsByAssignment(assignmentId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}/submission")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getStudentSubmission(
            @PathVariable("id") UUID assignmentId,
            @RequestParam UUID studentId) {
        SubmissionResponse response = submissionService.getSubmissionByAssignmentAndStudent(assignmentId, studentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
