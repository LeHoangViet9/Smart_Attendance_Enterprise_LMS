package edufit_com_lms.module.quiz.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.quiz.dto.request.QuestionRequest;
import edufit_com_lms.module.quiz.dto.response.QuestionResponse;
import edufit_com_lms.module.quiz.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import edufit_com_lms.security.CustomUserDetail;
import edufit_com_lms.module.auth.entity.Role;

@RestController
@RequestMapping("/api/v1/quizzes/{quizId}/questions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
public class QuestionController {
        private final QuestionService questionService;

        @GetMapping
        public ResponseEntity<ApiResponse<Page<QuestionResponse>>> getQuestions(
                        @PathVariable Long quizId,
                        @RequestParam(required = false) String keyword,
                        @PageableDefault(page = 0, size = 10) Pageable pageable) {
                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "List questions successfully",
                                null,
                                questionService.findAllQuestions(quizId, keyword, pageable),
                                HttpStatus.OK), HttpStatus.OK);
        }

        @PostMapping
        public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
                        @PathVariable Long quizId,
                        @RequestBody QuestionRequest request) {
                Long lecturerId = getLecturerIdOrNull();
                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Add question successfully",
                                null,
                                questionService.createQuestion(quizId, request, lecturerId),
                                HttpStatus.CREATED), HttpStatus.CREATED);
        }

        @PutMapping("/{questionId}")
        public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
                        @PathVariable Long quizId,
                        @PathVariable Long questionId,
                        @RequestBody QuestionRequest request) {
                Long lecturerId = getLecturerIdOrNull();
                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Update question successfully",
                                null,
                                questionService.updateQuestion(quizId, questionId, request, lecturerId),
                                HttpStatus.OK), HttpStatus.OK);
        }

        @DeleteMapping("/{questionId}")
        public ResponseEntity<ApiResponse<Void>> deleteQuestion(
                        @PathVariable Long quizId,
                        @PathVariable Long questionId) {
                Long lecturerId = getLecturerIdOrNull();
                questionService.deleteQuestion(quizId, questionId, lecturerId);
                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Delete question successfully",
                                null,
                                null,
                                HttpStatus.OK), HttpStatus.OK);
        }

        @GetMapping("/{questionId}")
        public ResponseEntity<ApiResponse<QuestionResponse>> getQuestion(
                        @PathVariable Long quizId,
                        @PathVariable Long questionId) {
                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Get question by id successfully",
                                null,
                                questionService.findById(quizId, questionId),
                                HttpStatus.OK), HttpStatus.OK);
        }

        @PostMapping("/import")
        public ResponseEntity<ApiResponse<Void>> importQuestionsFromExcel(
                        @PathVariable Long quizId,
                        @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
                Long lecturerId = getLecturerIdOrNull();
                questionService.importQuestionsFromExcel(quizId, file, lecturerId);
                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Import questions successfully",
                                null,
                                null,
                                HttpStatus.OK), HttpStatus.OK);
        }

        private Long getLecturerIdOrNull() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail userDetails) {
                        if (userDetails.getRole() == Role.LECTURER) {
                                return userDetails.getId();
                        }
                }
                return null;
        }
}
