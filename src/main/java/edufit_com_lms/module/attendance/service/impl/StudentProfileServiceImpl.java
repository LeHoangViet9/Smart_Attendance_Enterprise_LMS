package edufit_com_lms.module.attendance.service.impl;

import org.springframework.stereotype.Service;
import edufit_com_lms.module.attendance.dto.response.FaceOnboardingResponse;
import edufit_com_lms.module.auth.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.module.auth.repository.UserRepository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentProfileServiceImpl {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FaceOnboardingResponse onboardFace(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Image file is required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFound("User not found"));

        try {
            // Call Python AI Microservice
            RestTemplate restTemplate = new RestTemplate();
            String aiServiceUrl = "http://localhost:8000/api/v1/ai/extract-vector";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename() != null ? file.getOriginalFilename() : "face.jpg";
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<JsonNode> aiResponse = restTemplate.postForEntity(aiServiceUrl, requestEntity, JsonNode.class);

            if (!aiResponse.getStatusCode().is2xxSuccessful() || aiResponse.getBody() == null) {
                throw new RuntimeException("Lỗi kết nối tới AI Service");
            }

            JsonNode responseBody = aiResponse.getBody();
            if (!responseBody.get("success").asBoolean()) {
                throw new RuntimeException(responseBody.get("message").asText());
            }

            JsonNode descriptorNode = responseBody.get("descriptor");
            String descriptorJson = objectMapper.writeValueAsString(descriptorNode);
            
            // Save to database
            user.setFaceEmbedding(descriptorJson);
            userRepository.save(user);

            log.info("Face onboarding successful for user: {}", email);

            return FaceOnboardingResponse.builder()
                    .message("Khuôn mặt đã được đăng ký thành công!")
                    .avatarUrl(user.getAvatarUrl())
                    .isVectorGenerated(true)
                    .build();
        } catch (Exception e) {
            log.error("Error saving face descriptor for user: {}", email, e);
            throw new RuntimeException("Lỗi lưu dữ liệu sinh trắc học: " + e.getMessage());
        }
    }
}
