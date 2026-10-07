package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.auth.entity.LecturerProfile;
import edufit_com_lms.module.auth.entity.Role;
import edufit_com_lms.module.auth.repository.LecturerProfileRepository;
import edufit_com_lms.module.auth.repository.StudentProfileRepository;
import edufit_com_lms.module.lms.dto.request.CreateCourseRequest;
import edufit_com_lms.module.lms.dto.request.PresignedUrlRequest;
import edufit_com_lms.module.lms.dto.request.UpdateCourseRequest;
import edufit_com_lms.module.lms.dto.response.CourseResponse;
import edufit_com_lms.module.lms.dto.response.PresignedUrlResponse;
import edufit_com_lms.module.lms.entity.ClassEnrollment;
import edufit_com_lms.module.lms.repository.ClassEnrollmentRepository;
import edufit_com_lms.module.lms.repository.MajorRepository;
import edufit_com_lms.module.lms.service.CourseService;
import edufit_com_lms.module.lms.service.MinioStorageService;
import edufit_com_lms.security.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final StudentProfileRepository studentProfileRepository;
    private final LecturerProfileRepository lecturerProfileRepository;
    private final MajorRepository majorRepository;
    private final MinioStorageService minioStorageService;
    private final ClassEnrollmentRepository classEnrollmentRepository;

    // 1. LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ cÃƒÂ¡c khÃƒÂ³a hÃ¡Â»Âc
    @GetMapping
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getAllCourses(
            @RequestParam(required = false) Long lecturerId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail) {
            CustomUserDetail user = (CustomUserDetail) authentication.getPrincipal();
            if (user.getRole() == Role.STUDENT) {
                List<ClassEnrollment> enrollments = classEnrollmentRepository.findByStudentUserId(user.getId());
                List<UUID> courseIds = enrollments.stream()
                        .filter(e -> e.getSchoolClass() != null && e.getSchoolClass().getCourse() != null)
                        .map(e -> e.getSchoolClass().getCourse().getId())
                        .distinct()
                        .collect(Collectors.toList());
                        
                if (!courseIds.isEmpty()) {
                    return ResponseEntity.ok(ApiResponse
                            .success(courseService.getPaginatedCoursesByIds(courseIds, keyword, pageable)));
                } else {
                    return ResponseEntity.ok(ApiResponse.success(Page.empty(pageable)));
                }
            } else if (user.getRole() == Role.LECTURER) {
                LecturerProfile profile = lecturerProfileRepository.findById(user.getId()).orElse(null);
                if (profile != null && profile.getMajor() != null) {
                    return ResponseEntity.ok(ApiResponse
                            .success(courseService.getPaginatedCoursesByMajorId(profile.getMajor().getId(), keyword, pageable)));
                } else {
                    return ResponseEntity.ok(ApiResponse.success(Page.empty(pageable)));
                }
            }
        }

        Page<CourseResponse> responses = courseService.getPaginatedCourses(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 2. LÃ¡ÂºÂ¥y chi tiÃ¡ÂºÂ¿t khÃƒÂ³a hÃ¡Â»Âc kÃƒÂ¨m danh sÃƒÂ¡ch bÃƒÂ i giÃ¡ÂºÂ£ng
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable UUID id) {
        CourseResponse response = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 3. GiÃ¡ÂºÂ£ng viÃƒÂªn / Admin: TÃ¡ÂºÂ¡o khÃƒÂ³a hÃ¡Â»Âc mÃ¡Â»â€ºi
    @PostMapping
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            @RequestParam(required = false) Long lecturerId) {

        Long effectiveLecturerId = lecturerId;
        if (effectiveLecturerId == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail) {
                CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
                effectiveLecturerId = userDetails.getId();
            }
        }

        CourseResponse response = courseService.createCourse(request, effectiveLecturerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created successfully", response));
    }

    // 4. GiÃ¡ÂºÂ£ng viÃƒÂªn / Admin: CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t khÃƒÂ³a hÃ¡Â»Âc
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCourseRequest request) {
        CourseResponse response = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", response));
    }

    // 5. GiÃ¡ÂºÂ£ng viÃƒÂªn / Admin: XÃƒÂ³a khÃƒÂ³a hÃ¡Â»Âc
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully", null));
    }



    // 10. LÃ¡ÂºÂ¥y Presigned URL Ã„â€˜Ã¡Â»Æ’ upload file (video, document, thumbnail)
    @PostMapping("/upload-url")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUploadUrl(
            @Valid @RequestBody PresignedUrlRequest request) {
        PresignedUrlResponse response = minioStorageService.generatePresignedUploadUrl(request);
        return ResponseEntity.ok(ApiResponse.success("Pre-signed URL generated successfully", response));
    }

    // 11. LÃ¡ÂºÂ¥y Presigned URL Ã„â€˜Ã¡Â»Æ’ download file private
    @GetMapping("/download-url")
    public ResponseEntity<ApiResponse<String>> getPresignedDownloadUrl(
            @RequestParam String objectKey,
            @RequestParam(defaultValue = "60") int expiryMinutes) {
        String downloadUrl = minioStorageService.generatePresignedDownloadUrl(objectKey, expiryMinutes);
        return ResponseEntity.ok(ApiResponse.success("Download URL generated successfully", downloadUrl));
    }
}
