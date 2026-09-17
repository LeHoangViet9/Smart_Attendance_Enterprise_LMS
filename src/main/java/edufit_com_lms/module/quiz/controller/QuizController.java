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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import edufit_com_lms.security.CustomUserDetail;
import edufit_com_lms.module.auth.entity.Role;
import edufit_com_lms.module.auth.repository.LecturerProfileRepository;
import edufit_com_lms.module.auth.entity.LecturerProfile;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
public class QuizController {
    private final QuizService quizService;
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
}
