package edufit_com_lms.module.quiz.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.auth.entity.LecturerProfile;
import edufit_com_lms.module.auth.entity.Role;
import edufit_com_lms.module.auth.repository.LecturerProfileRepository;
import edufit_com_lms.module.quiz.dto.request.GradeEssayRequest;
import edufit_com_lms.module.quiz.dto.request.QuizRequest;
import edufit_com_lms.module.quiz.dto.response.AIGradeSuggestionResponse;
import edufit_com_lms.module.quiz.dto.response.QuizAttemptResponse;
import edufit_com_lms.module.quiz.dto.response.QuizResponse;
import edufit_com_lms.module.quiz.dto.response.QuizReviewResponse;
import edufit_com_lms.module.quiz.service.QuizAttemptService;
import edufit_com_lms.module.quiz.service.QuizService;
import edufit_com_lms.security.CustomUserDetail;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
public class QuizController {
    private final QuizService quizService;
    private final QuizAttemptService quizAttemptService;
    private final LecturerProfileRepository lecturerProfileRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<QuizResponse>>> getQuizess(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        
        Long lecturerId = null;
        UUID majorId = null;
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail userDetails) {
            if (userDetails.getRole() == Role.LECTURER) {
                lecturerId = userDetails.getId();
                LecturerProfile profile = lecturerProfileRepository.findById(lecturerId).orElse(null);
                if (profile != null && profile.getMajor() != null) {
                    majorId = profile.getMajor().getId();
                } else {
                    return ResponseEntity.ok(ApiResponse.success(Page.empty(pageable)));
                }
            }
        }

        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "List quizzes successfully",
                null,
                quizService.getQuizzes(keyword, sortBy, majorId, lecturerId, pageable),
                HttpStatus.OK), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(@RequestBody QuizRequest request) {
        Long creatorId = getUserId();
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Add quiz successfully",
                null,
                quizService.createQuiz(request, creatorId),
                HttpStatus.CREATED), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizResponse>> updateQuiz(@PathVariable Long id,
            @RequestBody QuizRequest request) {
        Long lecturerId = getLecturerIdOrNull();
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Update quiz successfully",
                null,
                quizService.updateQuiz(id, request, lecturerId),
                HttpStatus.OK), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizResponse>> deleteQuiz(@PathVariable Long id) {
        Long lecturerId = getLecturerIdOrNull();
        quizService.deleteQuiz(id, lecturerId);
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Delete quiz successfully",
                null,
                null,
                HttpStatus.OK), HttpStatus.OK);
    }

    private Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail userDetails) {
            return userDetails.getId();
        }
        return null;
    }
    
    private Long getLecturerIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail userDetails) {
            if (userDetails.getRole() == Role.LECTURER) {
                return userDetails.getId();
            }
        }
        return null; // ADMIN passes null to bypass ownership check
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuiz(@PathVariable Long id) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Get quiz by id successfully",
                null,
                quizService.findQuizById(id),
                HttpStatus.OK), HttpStatus.OK);
    }

    @GetMapping("/{id}/attempts")
    public ResponseEntity<ApiResponse<Page<QuizAttemptResponse>>> getQuizAttempts(
            @PathVariable Long id,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Fetched quiz attempts successfully",
                quizAttemptService.getAttemptsByQuizId(id, pageable)));
    }

    @GetMapping("/attempts/{attemptId}/grading")
    public ResponseEntity<ApiResponse<QuizReviewResponse>> getAttemptForGrading(@PathVariable Long attemptId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Fetched attempt details for grading",
                quizAttemptService.getAttemptReview(attemptId, null))); // null studentId bypasses ownership check
    }

    @PutMapping("/attempts/{attemptId}/grade")
    public ResponseEntity<ApiResponse<QuizAttemptResponse>> gradeAttempt(
            @PathVariable Long attemptId,
            @RequestBody GradeEssayRequest request) {
        Long lecturerId = getLecturerIdOrNull();
        return ResponseEntity.ok(ApiResponse.success(
                "Graded attempt successfully",
                quizAttemptService.gradeQuizAttempt(attemptId, request, lecturerId)));
    }

    @PostMapping("/attempts/{attemptId}/answers/{answerId}/ai-suggest")
    public ResponseEntity<ApiResponse<AIGradeSuggestionResponse>> suggestGradeWithAI(
            @PathVariable Long attemptId,
            @PathVariable Long answerId) {
        Long lecturerId = getLecturerIdOrNull();
        return ResponseEntity.ok(ApiResponse.success(
                "AI generated suggestion successfully",
                quizAttemptService.suggestGradeWithAI(attemptId, answerId, lecturerId)));
    }

    @GetMapping("/{id}/analytics")
    public ResponseEntity<ApiResponse<edufit_com_lms.module.quiz.dto.response.QuizAnalyticsResponse>> getQuizAnalytics(
            @PathVariable Long id) {
        Long lecturerId = getLecturerIdOrNull();
        return ResponseEntity.ok(ApiResponse.success(
                "Fetch quiz analytics successfully",
                quizAttemptService.getQuizAnalytics(id, lecturerId)));
    }

    @GetMapping("/{id}/export-scores")
    public ResponseEntity<byte[]> exportQuizScoresToExcel(@PathVariable Long id) {
        Long lecturerId = getLecturerIdOrNull();
        byte[] data = quizAttemptService.exportQuizScoresToExcel(id, lecturerId);
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "BangDiem_Quiz_" + id + ".xlsx");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
    @PostMapping("/{id}/batch-grade-async")
    public ResponseEntity<ApiResponse<String>> batchGradeQuizWithAIAsync(@PathVariable Long id) {
        Long lecturerId = getLecturerIdOrNull();
        quizAttemptService.batchGradeQuizWithAIAsync(id, lecturerId);
        return ResponseEntity.ok(ApiResponse.success(
                "Đã bắt đầu chấm điểm tự luận tự động bằng AI trong nền. Hệ thống sẽ thông báo khi hoàn tất.",
                null));
    }
}
