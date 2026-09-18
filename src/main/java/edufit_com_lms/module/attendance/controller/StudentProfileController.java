package edufit_com_lms.module.attendance.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import java.util.concurrent.CompletableFuture;

import edufit_com_lms.module.attendance.dto.response.FaceOnboardingResponse;
import edufit_com_lms.module.attendance.service.impl.StudentProfileServiceImpl;
import lombok.RequiredArgsConstructor;
import edufit_com_lms.security.CustomUserDetail;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileServiceImpl profileService;

    @PostMapping("/onboarding-face")
    @PreAuthorize("hasRole('STUDENT')")
    public CompletableFuture<ResponseEntity<FaceOnboardingResponse>> onboardFace(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();

        return CompletableFuture.supplyAsync(() -> {
            FaceOnboardingResponse response = profileService.onboardFace(userDetails.getUsername(), file);
            return ResponseEntity.ok(response);
        });
    }
}
