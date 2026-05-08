package com.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FastApiMlService {

    private final RestTemplate restTemplate;

    @Value("${ml.service.predict-url:http://localhost:8000/predict}")
    private String predictUrl;

    public Map<String, Object> predictUrl(String url) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "url");
        payload.put("url", url);
        return callPredict(payload);
    }

    public Map<String, Object> predictText(String text, String type) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", type);
        payload.put("text", text);
        return callPredict(payload);
    }

    public Map<String, Object> predictDocument(byte[] bytes, String extractedText) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "document");
        payload.put("document", bytes != null ? Base64.getEncoder().encodeToString(bytes) : "");
        payload.put("text", extractedText == null ? "" : extractedText);
        return callPredict(payload);
    }

    public Map<String, Object> predictImage(byte[] bytes, String extractedText) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "image");
        payload.put("image", bytes != null ? Base64.getEncoder().encodeToString(bytes) : "");
        payload.put("text", extractedText == null ? "" : extractedText);
        return callPredict(payload);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callPredict(Map<String, Object> payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    predictUrl,
                    request,
                    Map.class
            );
            if (response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception ex) {
            log.warn("FastAPI ML call failed: {}", ex.getMessage());
        }

        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("prediction", "SUSPICIOUS");
        fallback.put("confidence", 70.0);
        fallback.put("riskLevel", "MEDIUM");
        return fallback;
    }
}
