package edufit_com_lms.module.attendance.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edufit_com_lms.common.exception.AppException;
import edufit_com_lms.common.exception.BadRequestException;
import edufit_com_lms.common.exception.ResourceNotFound;
import edufit_com_lms.module.attendance.dto.response.FaceOnboardingResponse;
import edufit_com_lms.module.auth.entity.User;
import edufit_com_lms.module.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentProfileServiceImpl {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.service.url}")
    private String aiServiceBaseUrl;

    public FaceOnboardingResponse onboardFace(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFound("User not found"));

        try {
            String aiServiceUrl = aiServiceBaseUrl + "/api/v1/ai/extract-vector";

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
                throw new AppException(HttpStatus.BAD_GATEWAY, "AI Service connection error");
            }

            JsonNode responseBody = aiResponse.getBody();
            if (!responseBody.get("success").asBoolean()) {
                throw new AppException(HttpStatus.BAD_GATEWAY, responseBody.get("message").asText());
            }

            JsonNode descriptorNode = responseBody.get("descriptor");
            String descriptorJson = objectMapper.writeValueAsString(descriptorNode);
            
            // Save to database
            user.setFaceEmbedding(descriptorJson);
            userRepository.save(user);

            log.info("Face onboarding successful for user: {}", email);

            return FaceOnboardingResponse.builder()
                    .message("Khuon mat da duoc dang ky thanh cong")
                    .avatarUrl(user.getAvatarUrl())
                    .isVectorGenerated(true)
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error saving face descriptor for user: {}", email, e);
            throw new AppException(HttpStatus.BAD_GATEWAY, "Error saving biometric data: " + e.getMessage());
        }
    }
}
