package edufit_com_lms.module.quiz.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.quiz.dto.request.SubmitQuizRequest;
import edufit_com_lms.module.quiz.dto.response.QuizResponse;
import edufit_com_lms.module.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import edufit_com_lms.module.quiz.service.QuizAttemptService;
import edufit_com_lms.module.quiz.dto.response.QuizAttemptResponse;
import edufit_com_lms.module.quiz.dto.response.QuizReviewResponse;
import edufit_com_lms.module.auth.repository.StudentProfileRepository;
import edufit_com_lms.module.auth.entity.StudentProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import edufit_com_lms.security.CustomUserDetail;
import edufit_com_lms.module.lms.service.MinioStorageService;
import edufit_com_lms.module.lms.dto.request.PresignedUrlRequest;
import edufit_com_lms.module.lms.dto.response.PresignedUrlResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/student/quizzes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentQuizController {
        private final QuizAttemptService quizAttemptService;
        private final QuizService quizService;
        private final StudentProfileRepository studentProfileRepository;
        private final MinioStorageService minioStorageService;

        // Method xóa cờ isCorrect để sinh viên không thể dùng F12 soi đáp án
        private void scrubCorrectAnswers(QuizResponse res) {
                if (res != null && res.getQuestions() != null) {
                        res.getQuestions().forEach(q -> {
                                if (q.getOptions() != null) {
                                        q.getOptions().forEach(opt -> opt.setIsCorrect(null));
                                }
                        });
                }
        }

        // API: Xem danh sách đề thi hiện có
        @GetMapping
        @org.springframework.transaction.annotation.Transactional(readOnly = true)
        public ResponseEntity<ApiResponse<Page<QuizResponse>>> getAvailableQuizzes(
                        @RequestParam(required = false) String keyword,
                        @PageableDefault(page = 0, size = 10) Pageable pageable) {
                try {
                        java.util.UUID majorId = null;
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail userDetails) {
                                StudentProfile profile = studentProfileRepository.findById(userDetails.getId()).orElse(null);
                                if (profile != null && profile.getSchoolClass() != null && profile.getSchoolClass().getMajor() != null) {
                                        majorId = profile.getSchoolClass().getMajor().getId();
                                }
                        }

                        Page<QuizResponse> page = quizService.getQuizzes(keyword, null, majorId, null, pageable);
                        page.forEach(this::scrubCorrectAnswers);
                        return new ResponseEntity<>(new ApiResponse<>(
                                        true,
                                        "List available quizzes successfully",
                                        null,
                                        page,
                                        HttpStatus.OK), HttpStatus.OK);
                } catch (Exception e) {
                        e.printStackTrace();
                        return new ResponseEntity<>(new ApiResponse<>(
                                        false,
                                        "HTTP 500 Debug: " + e.getMessage(),
                                        null,
                                        null,
                                        HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }

        // API: Xem thông tin và hệ thống câu hỏi của đề
        @GetMapping("/{quizId}/details")
        public ResponseEntity<ApiResponse<QuizResponse>> getQuizForStudent(
                        @PathVariable Long quizId) {
                QuizResponse quizResponse = quizService.findQuizById(quizId);
                scrubCorrectAnswers(quizResponse);
                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Fetch quiz details successfully",
                                null,
                                quizResponse,
                                HttpStatus.OK), HttpStatus.OK);
        }

        // API: Sinh viên bắt đầu làm bài
        @PostMapping("/{quizId}/attempts")
        public ResponseEntity<ApiResponse<QuizAttemptResponse>> startAttempt(
                        @PathVariable Long quizId) {

                // Trích xuất ID học viên từ Token (SecurityContext)
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
                Long studentId = userDetails.getId();

                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Start test",
                                null,
                                quizAttemptService.startAttempt(quizId, studentId),
                                HttpStatus.CREATED), HttpStatus.CREATED);
        }

        // API: Sinh viên nộp bài thi
        @PostMapping("/attempts/{attemptId}/submit")
        public ResponseEntity<ApiResponse<QuizAttemptResponse>> submitAttempt(
                        @PathVariable Long attemptId,
                        @RequestBody SubmitQuizRequest submitRequest) {
                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Submit test successfully",
                                null,
                                quizAttemptService.submitAttempt(attemptId, submitRequest),
                                HttpStatus.OK), HttpStatus.OK);
        }

        // API: Lịch sử điểm của bản thân
        @GetMapping("/attempts/history")
        public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<QuizAttemptResponse>>> getMyHistory(
                        @PageableDefault(page = 0, size = 10) Pageable pageable) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
                Long studentId = userDetails.getId();

                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Fetch attempt history successfully",
                                null,
                                quizAttemptService.getStudentAttemptHistory(studentId, pageable),
                                HttpStatus.OK), HttpStatus.OK);
        }

        // API: Xem lại bài thi chi tiết (Review Result)
        @GetMapping("/attempts/{attemptId}/review")
        public ResponseEntity<ApiResponse<QuizReviewResponse>> getAttemptReview(@PathVariable Long attemptId) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
                Long studentId = userDetails.getId();

                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Fetch attempt review successfully",
                                null,
                                quizAttemptService.getAttemptReview(attemptId, studentId),
                                HttpStatus.OK), HttpStatus.OK);
        }

        // API: Lưu nháp bài thi (Autosave thời gian thực vào Redis)
        @PostMapping("/attempts/{attemptId}/autosave")
        public ResponseEntity<ApiResponse<String>> autosaveAttempt(
                        @PathVariable Long attemptId,
                        @RequestBody SubmitQuizRequest submitRequest) {

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
                Long studentId = userDetails.getId();

                quizAttemptService.autosaveAttempt(attemptId, studentId, submitRequest);

                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Autosave successfully",
                                null,
                                "Data synchronized to Redis cache",
                                HttpStatus.OK), HttpStatus.OK);
        }

        // API: Get Presigned URL for Proctoring Image Upload
        @PostMapping("/upload-url")
        public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUploadUrl(
                        @Valid @RequestBody PresignedUrlRequest request) {
                PresignedUrlResponse response = minioStorageService.generatePresignedUploadUrl(request);
                return ResponseEntity.ok(ApiResponse.success("Pre-signed URL generated successfully", response));
        }
}
