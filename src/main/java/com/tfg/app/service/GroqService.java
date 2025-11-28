package com.tfg.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    // CLAVE DIRECTA (solo para desarrollo local, nunca la subas a GitHub)
    private final String apiKey = "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";

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    private final WebClient webClient;

    public GroqService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String analizarGastos(String prompt) {
        try {
            System.out.println("Enviando a Groq con Llama 3.1...");

            Map<String, Object> requestBody = Map.of(
                "model", "llama-3.1-8b-instant",
                "messages", List.of(
                    Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 1200,
                "temperature", 0.7
            );

            Map<String, Object> response = webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return ((String) message.get("content")).trim();
            }

            return "Error: respuesta inesperada de Groq.";

        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                return "Error 401: API Key inválida o revocada. Cambia la clave en GroqService.java";
            }
            return "Error con Groq: " + e.getMessage();
        }
    }

    
}