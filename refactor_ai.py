import json

file_path = 'src/main/java/edufit_com_lms/module/quiz/service/impl/AIGradingServiceImpl.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update the prompt to not require manual JSON formatting constraints
content = content.replace(
    'Return ONLY a valid JSON object in this exact format, with no markdown formatting or backticks: {\\"points\\": number, \\"feedback\\": \\"string\\"}',
    'Return a JSON object in this exact format: {\\"points\\": number, \\"feedback\\": \\"string\\"}'
)

# 2. Add generationConfig to the request payload
replacement = '''            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("parts", List.of(part));
            requestBody.put("contents", List.of(contentMap));

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("responseMimeType", "application/json");
            requestBody.put("generationConfig", generationConfig);'''

# Find the place to replace
start_idx = content.find('Map<String, Object> requestBody = new HashMap<>();')
end_idx = content.find('HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);')

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + replacement + '\n\n            ' + content[end_idx:]

# 3. Remove manual stripping
strip_code = '''            // Clean up backticks if model ignored instruction
            if (responseText.startsWith("```json")) {
                responseText = responseText.substring(7);
            }
            if (responseText.startsWith("```")) {
                responseText = responseText.substring(3);
            }
            if (responseText.endsWith("```")) {
                responseText = responseText.substring(0, responseText.length() - 3);
            }'''
content = content.replace(strip_code, '            // Native JSON mode guarantees clean JSON output')

# 4. Change default fallback score from maxPoints / 2 to 0
content = content.replace('.points(maxPoints / 2)', '.points(0.0)')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
