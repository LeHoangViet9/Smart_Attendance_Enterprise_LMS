package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.lms.dto.request.CreateLessonRequest;
import edufit_com_lms.module.lms.dto.request.UpdateLessonRequest;
import edufit_com_lms.module.lms.dto.response.LessonResponse;
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

    // 6. LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch bÃƒÂ i giÃ¡ÂºÂ£ng cÃ¡Â»Â§a mÃ¡Â»â„¢t khÃƒÂ³a hÃ¡Â»Âc (cÃƒÂ³ phÃƒÂ¢n trang)
    @GetMapping("/{id}/lessons")
    public ResponseEntity<ApiResponse<Page<LessonResponse>>> getLessonsByCourseId(
            @PathVariable("id") UUID courseId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<LessonResponse> responses = courseService.getPaginatedLessonsByCourseId(courseId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 7. GiÃ¡ÂºÂ£ng viÃƒÂªn / Admin: ThÃƒÂªm bÃƒÂ i giÃ¡ÂºÂ£ng vÃƒÂ o khÃƒÂ³a hÃ¡Â»Âc
    @PostMapping("/{id}/lessons")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LessonResponse>> addLesson(
            @PathVariable("id") UUID courseId,
            @Valid @RequestBody CreateLessonRequest request) {
        LessonResponse response = courseService.addLesson(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lesson added successfully", response));
    }

    // 8. GiÃ¡ÂºÂ£ng viÃƒÂªn / Admin: CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t bÃƒÂ i giÃ¡ÂºÂ£ng
    @PutMapping("/lessons/{LessonId}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LessonResponse>> updateLesson(
            @PathVariable UUID LessonId,
            @Valid @RequestBody UpdateLessonRequest request) {
        LessonResponse response = courseService.updateLesson(LessonId, request);
        return ResponseEntity.ok(ApiResponse.success("Lesson updated successfully", response));
    }

    // 9. GiÃ¡ÂºÂ£ng viÃƒÂªn / Admin: XÃƒÂ³a bÃƒÂ i giÃ¡ÂºÂ£ng
    @DeleteMapping("/lessons/{LessonId}")
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable UUID LessonId) {
        courseService.deleteLesson(LessonId);
        return ResponseEntity.ok(ApiResponse.success("Lesson deleted successfully", null));
    }
}
