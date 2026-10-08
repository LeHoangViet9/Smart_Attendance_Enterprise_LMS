package edufit_com_lms.module.lms.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edufit_com_lms.module.lms.dto.request.ChatbotRequest;
import edufit_com_lms.module.lms.dto.response.ChatbotResponse;
import edufit_com_lms.module.lms.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatbotResponse askTutor(ChatbotRequest request, Long studentId) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty() || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ChatbotResponse.builder()
                    .reply("Xin chào, hiện tại hệ thống AI Tutor đang bảo trì hoặc chưa cấu hình API Key. Bạn vui lòng quay lại sau nhé!")
                    .build();
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = String.format(
                    "You are a helpful, friendly AI Tutor acting as a teaching assistant for university students. " +
                    "Your name is Educo Tutor. " +
                    "Answer the student's question clearly and concisely. " +
                    "If the question is not related to studying, gently remind them that you are an academic tutor. " +
                    "Student's question: %s",
                    request.getMessage()
            );

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("parts", List.of(part));
            requestBody.put("contents", List.of(contentMap));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String fullUrl = geminiApiUrl + geminiApiKey;
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String responseText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            return ChatbotResponse.builder()
                    .reply(responseText)
                    .build();

        } catch (Exception e) {
            log.error("Error calling Gemini API for chatbot: ", e);
            return ChatbotResponse.builder()
                    .reply("Xin lỗi, mình đang gặp một chút sự cố kết nối (" + e.getMessage() + "). Bạn có thể thử lại sau nhé!")
                    .build();
        }
    }
}
