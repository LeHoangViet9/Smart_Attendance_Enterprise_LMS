package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.lms.dto.request.CreateCourseRequest;
import edufit_com_lms.module.lms.dto.request.CreateLessionRequest;
import edufit_com_lms.module.lms.dto.request.UpdateCourseRequest;
import edufit_com_lms.module.lms.dto.request.UpdateLessionRequest;
import edufit_com_lms.module.lms.dto.response.CourseResponse;
import edufit_com_lms.module.lms.dto.response.LessionResponse;
import edufit_com_lms.module.lms.service.CourseService;
import edufit_com_lms.security.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // 1. Lấy danh sách tất cả các khóa học
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses(
            @RequestParam(required = false) Long lecturerId) {
        List<CourseResponse> responses = (lecturerId != null)
                ? courseService.getCoursesByLecturer(lecturerId)
                : courseService.getAllCourses();
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
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCourseRequest request) {
        CourseResponse response = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", response));
    }

    // 5. Giảng viên / Admin: Xóa khóa học
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully", null));
    }

    // 6. Lấy danh sách bài giảng của một khóa học
    @GetMapping("/{id}/lessions")
    public ResponseEntity<ApiResponse<List<LessionResponse>>> getLessionsByCourseId(@PathVariable("id") UUID courseId) {
        List<LessionResponse> responses = courseService.getLessionsByCourseId(courseId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 7. Giảng viên / Admin: Thêm bài giảng vào khóa học
    @PostMapping("/{id}/lessions")
    public ResponseEntity<ApiResponse<LessionResponse>> addLession(
            @PathVariable("id") UUID courseId,
            @Valid @RequestBody CreateLessionRequest request) {
        LessionResponse response = courseService.addLession(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lession added successfully", response));
    }

    // 8. Giảng viên / Admin: Cập nhật bài giảng
    @PutMapping("/lessions/{lessionId}")
    public ResponseEntity<ApiResponse<LessionResponse>> updateLession(
            @PathVariable UUID lessionId,
            @Valid @RequestBody UpdateLessionRequest request) {
        LessionResponse response = courseService.updateLession(lessionId, request);
        return ResponseEntity.ok(ApiResponse.success("Lession updated successfully", response));
    }

    // 9. Giảng viên / Admin: Xóa bài giảng
    @DeleteMapping("/lessions/{lessionId}")
    public ResponseEntity<ApiResponse<Void>> deleteLession(@PathVariable UUID lessionId) {
        courseService.deleteLession(lessionId);
        return ResponseEntity.ok(ApiResponse.success("Lession deleted successfully", null));
    }
}
