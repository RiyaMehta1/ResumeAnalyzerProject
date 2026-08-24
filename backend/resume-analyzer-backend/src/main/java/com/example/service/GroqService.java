package com.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GroqService {

    @Value("${app.groq.api-key}")
    private String groqApiKey;

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String analyzeResume(String resumeText, String jobDescription) throws Exception {
        String prompt = String.format(
            "Analyze the following resume against the job description.\n\n" +
            "RESUME:\n%s\n\n" +
            "JOB DESCRIPTION:\n%s\n\n" +
            "Provide analysis in this JSON format only:\n" +
            "{\n" +
            "  \"atsScore\": <number 0-100>,\n" +
            "  \"matchedSkills\": [<list of matched skills>],\n" +
            "  \"missingSkills\": [<list of missing skills>],\n" +
            "  \"suggestions\": [<list of improvement suggestions>]\n" +
            "}",
            resumeText, jobDescription
        );

        String requestBody = objectMapper.writeValueAsString(
            new Object() {
                public final String model = "mixtral-8x7b-32768";
                public final Object[] messages = new Object[]{
                    new Object() {
                        public final String role = "user";
                        public final String content = prompt;
                    }
                };
                public final int max_tokens = 1024;
            }
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(GROQ_API_URL))
            .header("Authorization", "Bearer " + groqApiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Groq API error: " + response.body());
        }

        JsonNode responseJson = objectMapper.readTree(response.body());
        return responseJson.get("choices").get(0).get("message").get("content").asText();
    }
}
