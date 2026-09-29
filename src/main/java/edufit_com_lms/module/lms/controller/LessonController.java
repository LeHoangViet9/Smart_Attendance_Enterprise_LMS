package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.lms.dto.request.CreateLessionRequest;
import edufit_com_lms.module.lms.dto.request.UpdateLessionRequest;
import edufit_com_lms.module.lms.dto.response.LessionResponse;
import edufit_com_lms.module.lms.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class LessonController {

    private final CourseService courseService;

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
}
