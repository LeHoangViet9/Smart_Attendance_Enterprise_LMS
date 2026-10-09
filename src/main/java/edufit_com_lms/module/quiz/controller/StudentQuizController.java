package edufit_com_lms.module.quiz.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.auth.entity.StudentProfile;
import edufit_com_lms.module.auth.repository.StudentProfileRepository;
import edufit_com_lms.module.lms.dto.request.PresignedUrlRequest;
import edufit_com_lms.module.lms.dto.response.PresignedUrlResponse;
import edufit_com_lms.module.lms.service.MinioStorageService;
import edufit_com_lms.module.quiz.dto.request.SubmitQuizRequest;
import edufit_com_lms.module.quiz.dto.response.QuizAttemptResponse;
import edufit_com_lms.module.quiz.dto.response.QuizResponse;
import edufit_com_lms.module.quiz.dto.response.QuizReviewResponse;
import edufit_com_lms.module.quiz.service.QuizAttemptService;
import edufit_com_lms.module.quiz.service.QuizService;
import edufit_com_lms.security.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/student/quizzes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@Slf4j
public class StudentQuizController {
        private final QuizAttemptService quizAttemptService;
        private final QuizService quizService;
        private final StudentProfileRepository studentProfileRepository;
        private final MinioStorageService minioStorageService;

        // Method xÃ³a cá» isCorrect Ä‘á»ƒ sinh viÃªn khÃ´ng thá»ƒ dÃ¹ng F12 soi Ä‘Ã¡p Ã¡n
        private void scrubCorrectAnswers(QuizResponse res) {
                if (res != null) {
                        res.setAccessCode(null); // Báº£o máº­t: KhÃ´ng bao giá» tráº£ pass thÃ´ vá» mÃ¡y sinh viÃªn
                        if (res.getQuestions() != null) {
                                res.getQuestions().forEach(q -> {
                                        if (q.getOptions() != null) {
                                                q.getOptions().forEach(opt -> opt.setIsCorrect(null));
                                        }
                                });
                        }
                }
        }

        // API: Xem danh sÃ¡ch Ä‘á» thi hiá»‡n cÃ³
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
                        log.error("Failed to fetch available quizzes for student", e);
                        return new ResponseEntity<>(new ApiResponse<>(
                                        false,
                                        "Unable to fetch available quizzes",
                                        null,
                                        null,
                                        HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }

        // API: Xem thÃ´ng tin vÃ  há»‡ thá»‘ng cÃ¢u há»i cá»§a Ä‘á»
        @GetMapping("/{quizId}/details")
        public ResponseEntity<ApiResponse<QuizResponse>> getQuizForStudent(
                        @PathVariable Long quizId) {
                QuizResponse quizResponse = quizService.findQuizById(quizId);
                
                // Láº¥y ID há»c viÃªn Ä‘á»ƒ lÃ m Seed xÃ¡o trá»™n
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail userDetails) {
                    Long studentId = userDetails.getId();
                    if (quizResponse != null && quizResponse.getQuestions() != null) {
                        // Khá»Ÿi táº¡o Random vá»›i seed = studentId + quizId
                        // Äáº£m báº£o F5 khÃ´ng bá»‹ nháº£y thá»© tá»±, nhÆ°ng sinh viÃªn A sáº½ cÃ³ Ä‘á» khÃ¡c sinh viÃªn B
                        java.util.Random rnd = new java.util.Random(studentId + quizId);
                        
                        // XÃ¡o trá»™n vá»‹ trÃ­ cÃ¢u há»i
                        java.util.Collections.shuffle(quizResponse.getQuestions(), rnd);
                        
                        // XÃ¡o trá»™n vá»‹ trÃ­ Ä‘Ã¡p Ã¡n (options) bÃªn trong tá»«ng cÃ¢u há»i
                        for (var q : quizResponse.getQuestions()) {
                            if (q.getOptions() != null) {
                                java.util.Collections.shuffle(q.getOptions(), rnd);
                            }
                        }
                    }
                }
                
                scrubCorrectAnswers(quizResponse);
                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Fetch quiz details successfully",
                                null,
                                quizResponse,
                                HttpStatus.OK), HttpStatus.OK);
        }

        // API: Sinh viÃªn báº¯t Ä‘áº§u lÃ m bÃ i
        @PostMapping("/{quizId}/attempts")
        public ResponseEntity<ApiResponse<QuizAttemptResponse>> startAttempt(
                        @PathVariable Long quizId,
                        @RequestBody(required = false) edufit_com_lms.module.quiz.dto.request.StartQuizRequest request) {

                // TrÃ­ch xuáº¥t ID há»c viÃªn tá»« Token (SecurityContext)
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
                Long studentId = userDetails.getId();

                String accessCode = request != null ? request.getAccessCode() : null;

                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Start test",
                                null,
                                quizAttemptService.startAttempt(quizId, studentId, accessCode),
                                HttpStatus.CREATED), HttpStatus.CREATED);
        }

        // API: Sinh viÃªn ná»™p bÃ i thi
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

        // API: Lá»‹ch sá»­ Ä‘iá»ƒm cá»§a báº£n thÃ¢n
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

        // API: Xem láº¡i bÃ i thi chi tiáº¿t (Review Result)
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

        // API: LÆ°u nhÃ¡p bÃ i thi (Autosave thá»i gian thá»±c vÃ o Redis)
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
