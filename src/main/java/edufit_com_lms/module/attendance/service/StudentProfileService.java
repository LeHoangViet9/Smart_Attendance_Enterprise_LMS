package edufit_com_lms.module.attendance.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import edufit_com_lms.module.attendance.dto.response.FaceOnboardingResponse;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.module.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentProfileService {

    private final UserRepository userRepository;

    public FaceOnboardingResponse onboardFace(String email, MultipartFile faceImage) {
        if (faceImage == null || faceImage.isEmpty()) {
            throw new RuntimeException("Face image is required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFound("User not found"));

        // 1. Upload to MinIO / S3 and get the URL
        String dummyAvatarUrl = "http://localhost:9000/lms-media-bucket/" + UUID.randomUUID().toString() + ".jpg";

        // 2. Call AI Module to extract 512-dimensional vector string
        String dummyVectorStr = "[0.0, 0.0, 0.0]"; // MUST BE LENGTH 512 LATER

        // 3. Save to database
        user.setAvatarUrl(dummyAvatarUrl);
        user.setFaceEmbedding(dummyVectorStr);
        userRepository.save(user);

        log.info("Face onboarding successful for user: {}", email);

        return FaceOnboardingResponse.builder()
                .message("Khuôn mặt đã được đăng ký thành công!")
                .avatarUrl(dummyAvatarUrl)
                .isVectorGenerated(true)
                .build();
    }
}
