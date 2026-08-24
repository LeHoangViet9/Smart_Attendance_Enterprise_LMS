package edufit_com_lms.module.quiz.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.quiz.dto.request.QuizRequest;
import edufit_com_lms.module.quiz.dto.response.QuizResponse;
import edufit_com_lms.module.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'LECTURER', 'ROLE_LECTURER')")
public class QuizController {
    private final QuizService quizService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<QuizResponse>>> getQuizess(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "List quizzes successfully",
                null,
                quizService.getQuizzes(keyword, sortBy, pageable),
                HttpStatus.OK), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(@RequestBody QuizRequest request) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Add quiz successfully",
                null,
                quizService.createQuiz(request),
                HttpStatus.CREATED), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizResponse>> updateQuiz(@PathVariable Long id,
            @RequestBody QuizRequest request) {
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Update quiz successfully",
                null,
                quizService.updateQuiz(id, request),
                HttpStatus.OK), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizResponse>> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return new ResponseEntity<>(new ApiResponse<>(
                true,
                "Delete quiz successfully",
                null,
                null,
                HttpStatus.OK), HttpStatus.OK);
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
}
