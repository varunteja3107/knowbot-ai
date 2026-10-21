package com.knowbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class OllamaService {

    private static final String API_URL =
            "http://localhost:11434/api/chat";

    private static final String MODEL = "llama3.2:1b";

    private final HttpClient client;
    private final ObjectMapper mapper;

    public OllamaService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.mapper = new ObjectMapper();
    }

    public String chat(String prompt) throws IOException, InterruptedException {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Please enter a question.");
        }

        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", MODEL);
        requestBody.put("stream", false);

        ArrayNode messages = requestBody.putArray("messages");
        ObjectNode message = messages.addObject();
        message.put("role", "user");
        message.put("content", prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofMinutes(3))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        mapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(
                    "Ollama returned HTTP " + response.statusCode()
                            + ": " + response.body());
        }

        JsonNode root = mapper.readTree(response.body());
        JsonNode content = root.path("message").path("content");

        if (content.isMissingNode() || content.isNull()) {
            throw new IOException("Ollama returned no answer.");
        }

        return content.asText();
    }

    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/tags"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception exception) {
            return false;
        }
    }
}
