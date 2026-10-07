package edufit_com_lms.module.quiz.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edufit_com_lms.module.quiz.dto.response.AIGradeSuggestionResponse;
import edufit_com_lms.module.quiz.service.AIGradingService;
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
public class AIGradingServiceImpl implements AIGradingService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AIGradeSuggestionResponse suggestGrade(String questionContent, String studentAnswer, double maxPoints) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty() || studentAnswer == null || studentAnswer.trim().isEmpty()) {
            // Mock response if API key is not configured or answer is empty
            log.info("Gemini API Key is not configured or answer is empty. Returning mock response.");
            return AIGradeSuggestionResponse.builder()
                    .points(Math.round(maxPoints * 0.8 * 10.0) / 10.0)
                    .feedback("This is a simulated AI feedback. The answer is quite good but needs deeper analysis.")
                    .build();
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = String.format(
                    "You are a strict and fair teacher grading a student's essay. \n" +
                            "Question: %s\n" +
                            "Student's Answer: %s\n" +
                            "Maximum Points: %s\n" +
                            "Evaluate the student's answer. Give a score from 0 to %s and provide feedback in Vietnamese. " +
                            "Return a JSON object in this exact format: {\"points\": number, \"feedback\": \"string\"}",
                    questionContent, studentAnswer, maxPoints, maxPoints
            );

                        Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("parts", List.of(part));
            requestBody.put("contents", List.of(contentMap));

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("responseMimeType", "application/json");
            requestBody.put("generationConfig", generationConfig);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String fullUrl = geminiApiUrl + geminiApiKey;
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String responseText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            
            // Native JSON mode guarantees clean JSON output

            JsonNode resultNode = objectMapper.readTree(responseText.trim());
            double points = resultNode.path("points").asDouble();
            String feedback = resultNode.path("feedback").asText();

            return AIGradeSuggestionResponse.builder()
                    .points(points)
                    .feedback(feedback)
                    .build();

        } catch (Exception e) {
            log.error("Error calling Gemini API for grading: ", e);
            return AIGradeSuggestionResponse.builder()
                    .points(0.0)
                    .feedback("AI Connection Error: " + e.getMessage() + ". Please grade this submission manually.")
                    .build();
        }
    }
}
