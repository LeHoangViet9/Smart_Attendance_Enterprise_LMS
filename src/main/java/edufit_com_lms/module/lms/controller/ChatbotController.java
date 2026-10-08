package edufit_com_lms.module.lms.controller;

import edufit_com_lms.common.response.ApiResponse;
import edufit_com_lms.module.lms.dto.request.ChatbotRequest;
import edufit_com_lms.module.lms.dto.response.ChatbotResponse;
import edufit_com_lms.module.lms.service.ChatbotService;
import edufit_com_lms.security.CustomUserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<ChatbotResponse>> askTutor(@RequestBody ChatbotRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long studentId = null;
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail) {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            studentId = userDetails.getId();
        }

        ChatbotResponse response = chatbotService.askTutor(request, studentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
