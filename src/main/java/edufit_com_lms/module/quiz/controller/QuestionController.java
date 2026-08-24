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

@RestController
@RequestMapping("/api/v1/quizzes/{quizId}/questions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'LECTURER', 'ROLE_LECTURER')")
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
                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Add question successfully",
                                null,
                                questionService.createQuestion(quizId, request),
                                HttpStatus.CREATED), HttpStatus.CREATED);
        }

        @PutMapping("/{questionId}")
        public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
                        @PathVariable Long quizId,
                        @PathVariable Long questionId,
                        @RequestBody QuestionRequest request) {
                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Update question successfully",
                                null,
                                questionService.updateQuestion(quizId, questionId, request),
                                HttpStatus.OK), HttpStatus.OK);
        }

        @DeleteMapping("/{questionId}")
        public ResponseEntity<ApiResponse<Void>> deleteQuestion(
                        @PathVariable Long quizId,
                        @PathVariable Long questionId) {
                questionService.deleteQuestion(quizId, questionId);
                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Delete question successfully",
                                null,
                                null,
                                HttpStatus.OK), HttpStatus.OK);
        }

        @GetMapping("/{questionId}")
        public ResponseEntity<ApiResponse<QuestionResponse>> getQuestion(
                        @PathVariable Long quizId, // Not used but standard REST practice for URL
                        @PathVariable Long questionId) {
                return new ResponseEntity<>(new ApiResponse<>(
                                true,
                                "Get question by id successfully",
                                null,
                                questionService.findById(questionId),
                                HttpStatus.OK), HttpStatus.OK);
        }
}
