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
    private final TelemetryAnalysisPreprocessorService  telemetryAnalysisPreprocessorService;

    @Autowired
    public AIServiceImpl(Client aiClient, TelemetryAnalysisPreprocessorService telemetryAnalysisPreprocessorService) {
        this.aiClient = aiClient;
        this.telemetryAnalysisPreprocessorService = telemetryAnalysisPreprocessorService;
    }

    @Override
    public String analyseSession(SessionDTO sessionDTO, List<LogDTO> sessionData) {

        String structuredData = telemetryAnalysisPreprocessorService.prepareData(sessionData);
        String prompt = PromptBuilder.promptHandler(structuredData, sessionDTO.getVehicle());

        try {
            GenerateContentResponse response = aiClient.models.generateContent(
                "gemini-3.8-flash",
                prompt,
                null
            );
            return response.text();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao comunicar com a IA: " + e.getMessage(), e);
        }
    }
}