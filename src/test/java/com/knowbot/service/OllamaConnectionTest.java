package com.knowbot.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OllamaConnectionTest {

    @Test
    void ollamaServerIsAvailable() {
        OllamaService service = new OllamaService();

        assertTrue(
                service.isAvailable(),
                "Ollama is not available. Open Ollama and try again."
        );
    }

    @Test
    void javaReceivesAnAiAnswer() throws Exception {
        OllamaService service = new OllamaService();

        assertTrue(
                service.isAvailable(),
                "Start Ollama before running this test."
        );

        String answer = service.chat(
                "Reply with exactly this sentence: KnowBot Java connection works."
        );

        System.out.println("Ollama answer: " + answer);

        assertNotNull(answer);
        assertFalse(answer.isBlank(), "The AI answer should not be empty.");
    }
}
