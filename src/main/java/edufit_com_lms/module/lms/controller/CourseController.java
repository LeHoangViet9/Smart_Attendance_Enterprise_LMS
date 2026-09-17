package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.lms.dto.request.CreateCourseRequest;
import edufit_com_lms.module.lms.dto.request.CreateLessionRequest;
import edufit_com_lms.module.lms.dto.request.UpdateCourseRequest;
import edufit_com_lms.module.lms.dto.request.UpdateLessionRequest;
import edufit_com_lms.module.lms.dto.response.CourseResponse;
import edufit_com_lms.module.lms.dto.response.LessionResponse;
import edufit_com_lms.module.lms.dto.request.PresignedUrlRequest;
import edufit_com_lms.module.lms.dto.response.PresignedUrlResponse;
import edufit_com_lms.module.lms.service.CourseService;
import edufit_com_lms.module.lms.service.MinioStorageService;
import edufit_com_lms.security.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import edufit_com_lms.module.auth.entity.Role;
import edufit_com_lms.module.auth.entity.StudentProfile;
import edufit_com_lms.module.auth.entity.LecturerProfile;
import edufit_com_lms.module.auth.repository.StudentProfileRepository;
import edufit_com_lms.module.auth.repository.LecturerProfileRepository;
import edufit_com_lms.module.lms.repository.MajorRepository;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final StudentProfileRepository studentProfileRepository;
    private final LecturerProfileRepository lecturerProfileRepository;
    private final MajorRepository majorRepository;
    private final MinioStorageService minioStorageService;

    // 1. Lấy danh sách tất cả các khóa học
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
                StudentProfile profile = studentProfileRepository.findById(user.getId()).orElse(null);
                if (profile != null && profile.getSchoolClass() != null
                        && profile.getSchoolClass().getMajor() != null) {
                    UUID majorId = profile.getSchoolClass().getMajor().getId();
                    return ResponseEntity.ok(ApiResponse
                            .success(courseService.getPaginatedCoursesByMajorId(majorId, keyword, pageable)));
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

    // 2. Lấy chi tiết khóa học kèm danh sách bài giảng
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable UUID id) {
        CourseResponse response = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 3. Giảng viên / Admin: Tạo khóa học mới
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

    // 4. Giảng viên / Admin: Cập nhật khóa học
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCourseRequest request) {
        CourseResponse response = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", response));
    }

    // 5. Giảng viên / Admin: Xóa khóa học
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully", null));
    }

    // 6. Lấy danh sách bài giảng của một khóa học (có phân trang)
    @GetMapping("/{id}/lessions")
    public ResponseEntity<ApiResponse<Page<LessionResponse>>> getLessonsByCourseId(
            @PathVariable("id") UUID courseId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<LessionResponse> responses = courseService.getPaginatedLessionsByCourseId(courseId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 7. Giảng viên / Admin: Thêm bài giảng vào khóa học
    @PostMapping("/{id}/lessions")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LessionResponse>> addLesson(
            @PathVariable("id") UUID courseId,
            @Valid @RequestBody CreateLessionRequest request) {
        LessionResponse response = courseService.addLession(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lession added successfully", response));
    }

    // 8. Giảng viên / Admin: Cập nhật bài giảng
    @PutMapping("/lessions/{lessionId}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LessionResponse>> updateLesson(
            @PathVariable UUID lessionId,
            @Valid @RequestBody UpdateLessionRequest request) {
        LessionResponse response = courseService.updateLession(lessionId, request);
        return ResponseEntity.ok(ApiResponse.success("Lession updated successfully", response));
    }

    // 9. Giảng viên / Admin: Xóa bài giảng
    @DeleteMapping("/lessions/{lessionId}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable UUID lessionId) {
        courseService.deleteLession(lessionId);
        return ResponseEntity.ok(ApiResponse.success("Lession deleted successfully", null));
    }

    // 10. Lấy Presigned URL để upload file (video, document, thumbnail)
    @PostMapping("/upload-url")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUploadUrl(
            @Valid @RequestBody PresignedUrlRequest request) {
        PresignedUrlResponse response = minioStorageService.generatePresignedUploadUrl(request);
        return ResponseEntity.ok(ApiResponse.success("Pre-signed URL generated successfully", response));
    }

    // 11. Lấy Presigned URL để download file private
    @GetMapping("/download-url")
    public ResponseEntity<ApiResponse<String>> getPresignedDownloadUrl(
            @RequestParam String objectKey,
            @RequestParam(defaultValue = "60") int expiryMinutes) {
        String downloadUrl = minioStorageService.generatePresignedDownloadUrl(objectKey, expiryMinutes);
        return ResponseEntity.ok(ApiResponse.success("Download URL generated successfully", downloadUrl));
    }
}
