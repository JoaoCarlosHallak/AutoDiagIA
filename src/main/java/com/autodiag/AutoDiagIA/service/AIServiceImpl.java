package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.dto.LogDTO;
import com.autodiag.AutoDiagIA.dto.SessionDTO;
import com.autodiag.AutoDiagIA.util.PromptBuilder;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AIServiceImpl implements AIService {

    private final Client aiClient;
    private final TelemetryAnalysisPreprocessorService telemetryAnalysisPreprocessorService;

    @Autowired
    public AIServiceImpl(Client aiClient, TelemetryAnalysisPreprocessorService telemetryAnalysisPreprocessorService) {
        this.aiClient = aiClient;
        this.telemetryAnalysisPreprocessorService = telemetryAnalysisPreprocessorService;
    }

    @Override
    public String analyseSession(SessionDTO sessionDTO, List<LogDTO> sessionData) {

        long preparationStart = System.nanoTime();

        String structuredData =
                    telemetryAnalysisPreprocessorService.prepareData(sessionData);

        String prompt =
                    PromptBuilder.promptHandler(
                            structuredData,
                            sessionDTO.getVehicle()
                    );

        long preparationTime =
                    (System.nanoTime() - preparationStart) / 1_000_000;

        System.out.println("\n========== MEDIÇÃO AUTODIAGIA ==========");
        System.out.println("Logs originais: " + sessionData.size());
        System.out.println("Caracteres dos dados: " + structuredData.length());
        System.out.println("Caracteres do prompt: " + prompt.length());
        System.out.println("Tempo de preparação: " + preparationTime + " ms");

        long aiStart = System.nanoTime();

        try {
            System.out.println("[AI] Iniciando chamada ao Gemini...");
            System.out.println("[AI] Modelo: gemini-3.8-flash");

            GenerateContentResponse response =
                    aiClient.models.generateContent(
                            "gemini-3.8-flash",
                            prompt,
                            null
                    );

            long aiTime =
                    (System.nanoTime() - aiStart) / 1_000_000;

            System.out.println("[AI] Resposta recebida!");
            System.out.println("[AI] Tempo: " + aiTime + " ms");
            System.out.println("[AI] Uso de tokens: " + response.usageMetadata());

            return response.text();

        } catch (Exception e) {
            System.err.println("[AI] ERRO NA CHAMADA AO GEMINI:");
            e.printStackTrace();

            throw new RuntimeException(
                    "Erro ao realizar análise com Gemini: " + e.getMessage(), e
            );

        } finally {
            long totalTime =
                    (System.nanoTime() - aiStart) / 1_000_000;

            System.out.println("[AI] Chamada finalizada após " + totalTime + " ms");
            System.out.println("========================================\n");
        }
        }
}