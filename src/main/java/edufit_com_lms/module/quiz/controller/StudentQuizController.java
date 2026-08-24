package edufit_com_lms.module.quiz.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.quiz.dto.request.SubmitQuizRequest;
import edufit_com_lms.module.quiz.dto.response.QuizResponse;
import edufit_com_lms.module.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import edufit_com_lms.module.quiz.service.QuizAttemptService;
import edufit_com_lms.module.quiz.dto.response.QuizAttemptResponse;
import edufit_com_lms.module.quiz.dto.response.QuizReviewResponse;
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
import edufit_com_lms.security.CustomUserDetail;

@RestController
@RequestMapping("/api/v1/student/quizzes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('STUDENT', 'ROLE_STUDENT')")
public class StudentQuizController {
        private final QuizAttemptService quizAttemptService;
        private final QuizService quizService;

        // API: Xem danh sách đề thi hiện có
        @GetMapping
        public ResponseEntity<ApiResponse<Page<QuizResponse>>> getAvailableQuizzes(
                        @RequestParam(required = false) String keyword,
                        @PageableDefault(page = 0, size = 10) Pageable pageable) {
                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "List available quizzes successfully",
                                null,
                                quizService.getQuizzes(keyword, null, pageable),
                                HttpStatus.OK), HttpStatus.OK);
        }

        // API: Xem thông tin và hệ thống câu hỏi của đề
        @GetMapping("/{quizId}/details")
        public ResponseEntity<ApiResponse<QuizResponse>> getQuizForStudent(
                        @PathVariable Long quizId) {
                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Fetch quiz details successfully",
                                null,
                                quizService.findQuizById(quizId),
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
        public ResponseEntity<ApiResponse<List<QuizAttemptResponse>>> getMyHistory() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
                Long studentId = userDetails.getId();

                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Fetch attempt history successfully",
                                null,
                                quizAttemptService.getStudentAttemptHistory(studentId),
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
}
