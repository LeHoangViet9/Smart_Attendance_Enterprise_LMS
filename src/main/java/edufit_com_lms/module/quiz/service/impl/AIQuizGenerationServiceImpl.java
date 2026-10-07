package edufit_com_lms.module.quiz.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edufit_com_lms.module.quiz.dto.request.QuestionRequest;
import edufit_com_lms.module.quiz.service.AIQuizGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIQuizGenerationServiceImpl implements AIQuizGenerationService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<QuestionRequest> generateQuestions(String documentText, int numberOfQuestions) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty() || documentText == null || documentText.trim().isEmpty()) {
            log.error("API Key or document text is empty.");
            throw new RuntimeException("Cannot generate questions: missing configuration or empty document.");
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = String.format(
                    "You are an expert teacher. Based on the following document, generate exactly %d multiple-choice questions.\n" +
                    "Document:\n%s\n\n" +
                    "Requirements:\n" +
                    "- Each question MUST have exactly 4 options.\n" +
                    "- Only ONE option can be correct.\n" +
                    "- Format the response as a JSON ARRAY of objects.\n" +
                    "- Each object must match this exact structure: \n" +
                    "  {\"content\": \"Question text here\", \"points\": 1.0, \"questionType\": \"SINGLE_CHOICE\", \"options\": [{\"content\": \"Option A text\", \"isCorrect\": true}, {\"content\": \"Option B text\", \"isCorrect\": false}, {\"content\": \"Option C text\", \"isCorrect\": false}, {\"content\": \"Option D text\", \"isCorrect\": false}]}\n" +
                    "- Do NOT wrap the JSON array in any markdown formatting (like ```json), just output the raw JSON array starting with '[' and ending with ']'.",
                    numberOfQuestions, documentText
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
            List<QuestionRequest> questions = objectMapper.readValue(responseText.trim(), new TypeReference<List<QuestionRequest>>() {});
            return questions;

        } catch (Exception e) {
            log.error("Error calling Gemini API for question generation: ", e);
            throw new RuntimeException("Failed to generate questions from AI: " + e.getMessage());
        }
    }
}
