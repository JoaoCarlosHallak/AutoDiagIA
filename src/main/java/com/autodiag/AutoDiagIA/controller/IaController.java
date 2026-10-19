package com.autodiag.AutoDiagIA.controller;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class IaController {

    private final Client aiClient;

    public IaController() {
        this.aiClient = new Client();
    }

    @GetMapping
    public String gerarResposta(@RequestParam(defaultValue = "Olá, quem é você?") String message) {
        try {
            GenerateContentResponse response = aiClient.models.generateContent(
                "gemini-3.8-flash",
                message,
                null
            );
            return response.text();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao comunicar com a IA: " + e.getMessage(), e);
        }
    }
}