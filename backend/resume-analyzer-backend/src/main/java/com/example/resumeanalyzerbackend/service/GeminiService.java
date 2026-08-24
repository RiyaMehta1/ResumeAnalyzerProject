package com.example.resumeanalyzerbackend.service;

import com.example.resumeanalyzerbackend.dto.AnalysisResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiService {

    @Value("${app.groq.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public AnalysisResponse analyzeResume(String resumeText, String jobDescription) {
        String trimmedResume = resumeText.length() > 3000
                ? resumeText.substring(0, 3000)
                : resumeText;

        String trimmedJD = jobDescription.length() > 2000
                ? jobDescription.substring(0, 2000)
                : jobDescription;

        String prompt = "You are an ATS (Applicant Tracking System) expert.\n\n"
                + "Analyze the resume against the job description below.\n\n"
                + "Return ONLY a valid JSON object with exactly these 4 fields. "
                + "No extra text, no markdown, no code blocks, no explanation:\n"
                + "{\n"
                + "  \"atsScore\": <integer 0-100>,\n"
                + "  \"matchedSkills\": [\"skill1\", \"skill2\"],\n"
                + "  \"missingSkills\": [\"skill1\", \"skill2\"],\n"
                + "  \"suggestions\": [\"suggestion1\", \"suggestion2\", \"suggestion3\"]\n"
                + "}\n\n"
                + "JOB DESCRIPTION:\n" + trimmedJD + "\n\n"
                + "RESUME TEXT:\n" + trimmedResume;

        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", "llama-3.3-70b-versatile");
            body.put("temperature", 0.1);
            body.put("max_tokens", 2048);

            ArrayNode messages = body.putArray("messages");
            ObjectNode message = messages.addObject();
            message.put("role", "user");
            message.put("content", prompt);

            String requestBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Groq HTTP status: " + response.statusCode());
            System.out.println("Groq raw response: " + response.body());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Groq API error " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");

            if (contentNode.isMissingNode() || contentNode.isNull()) {
                throw new RuntimeException("Groq response missing message content");
            }

            String text = contentNode.asText().trim();
            System.out.println("Groq text: " + text);

            if (text.startsWith("```")) {
                text = text.replaceAll("(?s)^```[a-zA-Z]*\\n?", "")
                        .replaceAll("```\\s*$", "")
                        .trim();
            }

            JsonNode result = objectMapper.readTree(text);

            int atsScore = result.path("atsScore").asInt(0);

            List<String> matchedSkills = new ArrayList<>();
            result.path("matchedSkills").forEach(n -> matchedSkills.add(n.asText()));

            List<String> missingSkills = new ArrayList<>();
            result.path("missingSkills").forEach(n -> missingSkills.add(n.asText()));

            List<String> suggestions = new ArrayList<>();
            result.path("suggestions").forEach(n -> suggestions.add(n.asText()));

            return AnalysisResponse.builder()
                    .atsScore(atsScore)
                    .matchedSkills(matchedSkills)
                    .missingSkills(missingSkills)
                    .suggestions(suggestions)
                    .build();

        } catch (Exception e) {
            System.err.println("Groq call failed: " + e.getMessage());
            throw new RuntimeException("Failed to analyze resume with Groq: " + e.getMessage(), e);
        }
    }
}