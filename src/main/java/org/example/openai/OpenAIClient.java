package org.example.openai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class OpenAIClient {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    public Map<String, Object> analyzeText(String text) {
        // Тіло запиту
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Аналізуй цей текст: \"" + text + "\". Визнач тональність (positive/neutral/negative), критичність (1-5), і як можна вирішити проблему. Поверни у форматі JSON {\"sentiment\":\"...\",\"criticality\":X,\"solution\":\"...\"}");

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", new Map[]{message});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        Map<String, Object> response = restTemplate.postForObject(OPENAI_URL, entity, Map.class);

        // Розбираємо відповідь
        Map<String, Object> result = new HashMap<>();
        try {
            Map choice = ((Map)((java.util.List)response.get("choices")).get(0));
            String content = (String)((Map)choice.get("message")).get("content");

            // Контент очікуємо у JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            result = mapper.readValue(content, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("sentiment", "NEUTRAL");
            result.put("criticality", 1);
            result.put("solution", "Немає даних");
        }

        return result;
    }
}
