package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.dto.LogDTO;
import com.autodiag.AutoDiagIA.dto.SessionDTO;
import com.autodiag.AutoDiagIA.exception.AiServiceException;
import com.autodiag.AutoDiagIA.util.PromptBuilder;
import com.google.genai.Client;
import com.google.genai.errors.ApiException;
import com.google.genai.types.GenerateContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AIServiceImpl implements AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIServiceImpl.class);
    private static final String MODEL = "gemini-3.8-flash";

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

        logger.info(
                "Gemini request prepared: sessionId={} model={} logs={} dataChars={} promptChars={} preparationMs={}",
                sessionDTO.getSessionId(),
                MODEL,
                sessionData.size(),
                structuredData.length(),
                prompt.length(),
                preparationTime
        );

        long aiStart = System.nanoTime();

        try {
            GenerateContentResponse response =
                    aiClient.models.generateContent(
                            MODEL,
                            prompt,
                            null
                    );

            long callTime = (System.nanoTime() - aiStart) / 1_000_000;
            String text = response.text();

            if (text == null || text.isBlank()) {
                int candidateCount = response.candidates()
                        .map(List::size)
                        .orElse(0);
                logger.warn(
                        "Gemini returned no usable text: sessionId={} model={} callMs={} candidates={} promptFeedbackPresent={} usage={}",
                        sessionDTO.getSessionId(),
                        MODEL,
                        callTime,
                        candidateCount,
                        response.promptFeedback().isPresent(),
                        response.usageMetadata().orElse(null)
                );
                throw new IllegalStateException(
                        "Gemini returned no usable text in the response."
                );
            }

            logger.info(
                    "Gemini response received: sessionId={} model={} modelVersion={} responseId={} callMs={} usage={}",
                    sessionDTO.getSessionId(),
                    MODEL,
                    response.modelVersion().orElse("unknown"),
                    response.responseId().orElse("unknown"),
                    callTime,
                    response.usageMetadata().orElse(null)
            );
            return text;

        } catch (RuntimeException e) {
            long callTime = (System.nanoTime() - aiStart) / 1_000_000;
            if (e instanceof ApiException apiException) {
                logger.error(
                        "Gemini API request failed: sessionId={} model={} callMs={} httpStatus={} status={} message={}",
                        sessionDTO.getSessionId(),
                        MODEL,
                        callTime,
                        apiException.code(),
                        apiException.status(),
                        apiException.message(),
                        e
                );
            } else {
                logger.error(
                        "Gemini request failed: sessionId={} model={} callMs={} exceptionType={} message={}",
                        sessionDTO.getSessionId(),
                        MODEL,
                        callTime,
                        e.getClass().getName(),
                        e.getMessage(),
                        e
                );
            }
            throw new AiServiceException(
                    "Falha ao solicitar análise ao serviço de IA.", e
            );
        }
    }
}