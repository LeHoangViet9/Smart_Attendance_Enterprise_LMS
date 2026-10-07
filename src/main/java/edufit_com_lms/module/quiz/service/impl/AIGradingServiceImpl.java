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
                    .feedback("ÄÃ¢y lÃ  pháº£n há»“i giáº£ láº­p tá»« AI. BÃ i lÃ m khÃ¡ tá»‘t tuy nhiÃªn cáº§n triá»ƒn khai Ã½ sÃ¢u hÆ¡n.")
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
                            "Return ONLY a valid JSON object in this exact format, with no markdown formatting or backticks: {\"points\": number, \"feedback\": \"string\"}",
                    questionContent, studentAnswer, maxPoints, maxPoints
            );

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String fullUrl = geminiApiUrl + geminiApiKey;
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String responseText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            
            // Clean up backticks if model ignored instruction
            if (responseText.startsWith("```json")) {
                responseText = responseText.substring(7);
            }
            if (responseText.startsWith("```")) {
                responseText = responseText.substring(3);
            }
            if (responseText.endsWith("```")) {
                responseText = responseText.substring(0, responseText.length() - 3);
            }

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
                    .points(maxPoints / 2)
                    .feedback("Lá»—i káº¿t ná»‘i AI: " + e.getMessage() + ". Vui lÃ²ng cháº¥m Ä‘iá»ƒm thá»§ cÃ´ng.")
                    .build();
        }
    }
}
