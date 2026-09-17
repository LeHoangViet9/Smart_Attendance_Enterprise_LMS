package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.lms.dto.request.CreateAssignmentRequest;
import edufit_com_lms.module.lms.dto.request.PresignedUrlRequest;
import edufit_com_lms.module.lms.dto.request.SubmitAssignmentRequest;
import edufit_com_lms.module.lms.dto.request.UpdateAssignmentRequest;
import edufit_com_lms.module.lms.dto.response.AssignmentResponse;
import edufit_com_lms.module.lms.dto.response.PresignedUrlResponse;
import edufit_com_lms.module.lms.dto.response.SubmissionResponse;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import edufit_com_lms.security.CustomUserDetail;
import edufit_com_lms.module.auth.entity.Role;
import edufit_com_lms.module.auth.repository.StudentProfileRepository;
import edufit_com_lms.module.auth.repository.LecturerProfileRepository;
import edufit_com_lms.module.lms.repository.MajorRepository;
import edufit_com_lms.module.lms.repository.SchoolClassRepository;
import edufit_com_lms.module.lms.repository.ClassEnrollmentRepository;
import edufit_com_lms.module.lms.entity.SchoolClass;
import edufit_com_lms.module.lms.entity.ClassEnrollment;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final MinioStorageService minioStorageService;
    private final StudentProfileRepository studentProfileRepository;
    private final LecturerProfileRepository lecturerProfileRepository;
    private final MajorRepository majorRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;

    private Long getLecturerIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail userDetails) {
            if (userDetails.getRole() == Role.LECTURER) {
                return userDetails.getId();
            }
        }
        return null;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(
            @Valid @RequestBody CreateAssignmentRequest request) {
        Long lecturerId = getLecturerIdOrNull();
        AssignmentResponse response = assignmentService.createAssignment(request, lecturerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Assignment created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AssignmentResponse>>> getAllAssignments(
            @RequestParam(required = false) UUID classId,
            @PageableDefault(size = 10) Pageable pageable) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail) {
            CustomUserDetail user = (CustomUserDetail) authentication.getPrincipal();

            if (user.getRole() == Role.STUDENT) {
                List<UUID> classIds = classEnrollmentRepository.findByStudentUserId(user.getId()).stream()
                        .map(ClassEnrollment::getSchoolClass)
                        .filter(java.util.Objects::nonNull)
                        .map(SchoolClass::getId)
                        .collect(Collectors.toList());
                
                if (classIds.isEmpty()) {
                    return ResponseEntity.ok(ApiResponse.success(Page.empty(pageable)));
                } else {
                    return ResponseEntity.ok(ApiResponse.success(assignmentService
                            .getPaginatedAssignmentsByClassIdIn(classIds, pageable)));
                }
            } else if (user.getRole() == Role.LECTURER) {
                List<UUID> classIds = schoolClassRepository.findByLecturer_UserId(user.getId()).stream()
                        .map(SchoolClass::getId)
                        .collect(Collectors.toList());
                if (classIds.isEmpty()) {
                    return ResponseEntity.ok(ApiResponse.success(Page.empty(pageable)));
                }
                return ResponseEntity.ok(ApiResponse
                        .success(assignmentService.getPaginatedAssignmentsByClassIdIn(classIds, pageable)));
            }
        }

        Page<AssignmentResponse> responses = (classId != null)
                ? assignmentService.getPaginatedAssignmentsByClassId(classId, pageable)
                : assignmentService.getPaginatedAssignments(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignmentById(@PathVariable UUID id) {
        AssignmentResponse response = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<ApiResponse<Page<AssignmentResponse>>> getAssignmentsByClass(
            @PathVariable UUID classId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<AssignmentResponse> responses = assignmentService.getPaginatedAssignmentsByClassId(classId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> updateAssignment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAssignmentRequest request) {
        Long lecturerId = getLecturerIdOrNull();
        AssignmentResponse response = assignmentService.updateAssignment(id, request, lecturerId);
        return ResponseEntity.ok(ApiResponse.success("Assignment updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable UUID id) {
        Long lecturerId = getLecturerIdOrNull();
        assignmentService.deleteAssignment(id, lecturerId);
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
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitAssignment(
            @PathVariable("id") UUID assignmentId,
            @Valid @RequestBody SubmitAssignmentRequest request) {
        SubmissionResponse response = submissionService.submitAssignment(assignmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Assignment submitted successfully", response));
    }

    @GetMapping("/{id}/submissions")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<SubmissionResponse>>> getSubmissionsForAssignment(
            @PathVariable("id") UUID assignmentId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<SubmissionResponse> responses = submissionService.getPaginatedSubmissionsByAssignment(assignmentId,
                pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}/submission")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getStudentSubmission(
            @PathVariable("id") UUID assignmentId,
            @RequestParam Long studentId) {
        SubmissionResponse response = submissionService.getSubmissionByAssignmentAndStudent(assignmentId, studentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
