package com.autodiag.AutoDiagIA.exception;

import com.autodiag.AutoDiagIA.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiError> handleInvalidRequest(InvalidRequestException exception, HttpServletRequest request) {
        return apiError(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        return apiError(HttpStatus.BAD_REQUEST, messageOrDefault(exception.getMessage(), "Requisição inválida."), request
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return apiError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiError> handleInvalidOperation(InvalidOperationException exception, HttpServletRequest request) {
        return apiError(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(Elm327CommunicationException.class)
    public ResponseEntity<ApiError> handleElm327Communication(Elm327CommunicationException exception, HttpServletRequest request) {
        return apiError(HttpStatus.SERVICE_UNAVAILABLE, "Não foi possível comunicar com o adaptador OBD.", request
        );
    }

    @ExceptionHandler(ObdResponseException.class)
    public ResponseEntity<ApiError> handleObdResponse(ObdResponseException exception, HttpServletRequest request) {
        return apiError(HttpStatus.BAD_GATEWAY, "A ECU devolveu uma resposta OBD inválida.", request
        );
    }

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ApiError> handleAiService(AiServiceException exception, HttpServletRequest request) {
        return apiError(HttpStatus.BAD_GATEWAY, "Não foi possível obter a análise do serviço de IA.", request
        );
    }

    @ExceptionHandler(InvalidTelemetryDataException.class)
    public ResponseEntity<ApiError> handleInvalidTelemetryData(InvalidTelemetryDataException exception, HttpServletRequest request) {
        return internalError(exception, request);
    }





    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiError> handleMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException exception,
            HttpServletRequest request) {
        return apiError(
                HttpStatus.NOT_ACCEPTABLE,
                "O formato de resposta solicitado não é suportado.",
                request
        );
    }


    private ResponseEntity<ApiError> internalError(Exception exception, HttpServletRequest request) {
        logger.error("Erro não tratado ao processar {} {}", request.getMethod(), request.getRequestURI(), exception);
        return apiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception exception,
            HttpServletRequest request) {

        return internalError(exception, request);
    }

    private ResponseEntity<ApiError> apiError(
            HttpStatus status,
            String detail,
            HttpServletRequest request) {
        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                detail,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    private String messageOrDefault(String message, String defaultMessage) {
        return message == null || message.isBlank() ? defaultMessage : message;
    }
}
