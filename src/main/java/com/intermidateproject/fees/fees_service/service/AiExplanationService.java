package com.intermidateproject.fees.fees_service.service;

import com.intermidateproject.fees.fees_service.anomaly.Anomaly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.json.JsonMapper;
import java.nio.charset.StandardCharsets;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class AiExplanationService {

    private static final Logger log = LoggerFactory.getLogger(AiExplanationService.class);
    private static final int MAX_ATTEMPTS = 2;

    private final AnomalyExplanationService cannedService;
    private final JsonMapper jsonMapper;
    private final String apiKey;
    private final String model;
    private final RestClient restClient;

    public AiExplanationService(AnomalyExplanationService cannedService,
                                JsonMapper jsonMapper,
                                @Value("${gemini.api-key:}") String apiKey,
                                @Value("${gemini.model:gemini-3.8-flash}") String model) {
        this.cannedService = cannedService;
        this.jsonMapper = jsonMapper;
        this.apiKey = apiKey;
        this.model = model;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(25));

        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .requestFactory(factory)
                .build();

        log.info("Gemini API key configured: {} (model: {})",
                apiKey != null && !apiKey.isBlank(), model);
    }

    public String explain(Anomaly anomaly) {
        if (apiKey == null || apiKey.isBlank()) {
            return cannedService.explainWithDetails(anomaly);
        }
        try {
            String text = callWithRetry(buildPrompt(anomaly));
            if (text == null || text.isBlank()) {
                return cannedService.explainWithDetails(anomaly);
            }
            return text.trim();
        } catch (Exception e) {
            log.warn("AI explanation failed, using canned text: {}", e.getMessage());
            return cannedService.explainWithDetails(anomaly);
        }
    }

    private String callWithRetry(String prompt) throws InterruptedException {
        for (int attempt = 1; ; attempt++) {
            try {
                return callGemini(prompt);
            } catch (RestClientResponseException e) {
                HttpStatusCode status = e.getStatusCode();
                boolean retryable = status.value() == 503 || status.value() == 429;
                if (!retryable || attempt >= MAX_ATTEMPTS) {
                    throw e;
                }
                log.info("Gemini returned {}, retrying (attempt {}/{})",
                        status.value(), attempt + 1, MAX_ATTEMPTS);
                Thread.sleep(1500);
            }
        }
    }

    private String buildPrompt(Anomaly anomaly) {
        return """
                You are helping a university finance officer who is not technical.
                A fees system flagged this anomaly:

                Type: %s
                Invoice ID: %d
                Details: %s

                In 3 to 4 plain sentences: say what happened, why it matters,
                and the next step to check. Do not use jargon or bullet points.
                Do not invent facts beyond the details given.
                """.formatted(anomaly.getType(), anomaly.getInvoiceId(), anomaly.getDetails());
    }
    private String callGemini(String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "input", prompt,
                "generation_config", Map.of(
                        "max_output_tokens", 500,
                        "thinking_level", "low"));

        // Read raw bytes: works whatever content type the server labels the body with
        byte[] bytes = restClient.post()
                .uri("/v1beta/interactions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-goog-api-key", apiKey)
                .body(body)
                .retrieve()
                .body(byte[].class);

        if (bytes == null || bytes.length == 0) {
            return null;
        }
        String raw = new String(bytes, StandardCharsets.UTF_8);

        try {
            Map<?, ?> response = jsonMapper.readValue(raw, Map.class);
            String text = extractText(response);
            if (text == null) {
                log.warn("No text found in Gemini response. Start of body: {}", preview(raw));
            }
            return text;
        } catch (Exception e) {
            log.warn("Gemini response was not parseable JSON. Start of body: {}", preview(raw));
            throw e;
        }
    }

    private String preview(String raw) {
        return raw.length() > 400 ? raw.substring(0, 400) : raw;
    }

    private String extractText(Map<?, ?> response) {
        Object direct = response.get("output_text");
        if (direct instanceof String s && !s.isBlank()) {
            return s;
        }
        if (!(response.get("steps") instanceof List<?> steps)) {
            return null;
        }
        for (int i = steps.size() - 1; i >= 0; i--) {
            if (!(steps.get(i) instanceof Map<?, ?> step)) {
                continue;
            }
            Object content = step.get("content");
            if (content instanceof String s && !s.isBlank()) {
                return s;
            }
            if (content instanceof List<?> parts) {
                StringBuilder sb = new StringBuilder();
                for (Object part : parts) {
                    if (part instanceof Map<?, ?> p && p.get("text") instanceof String t) {
                        sb.append(t);
                    }
                }
                if (!sb.isEmpty()) {
                    return sb.toString();
                }
            }
        }
        return null;
    }
}