package com.autodiag.AutoDiagIA;

import com.autodiag.AutoDiagIA.dto.ApiError;
import com.autodiag.AutoDiagIA.exception.GlobalExceptionHandler;
import com.autodiag.AutoDiagIA.exception.InvalidRequestException;
import com.autodiag.AutoDiagIA.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveRetornarBadRequestParaRequisicaoInvalida() {
        HttpServletRequest request = request("/session");

        ResponseEntity<ApiError> response = handler.handleInvalidRequest(
                new InvalidRequestException("PID inválido."),
                request
        );

        assertEquals(400, response.getStatusCode().value());
        assertEquals("PID inválido.", response.getBody().message());
        assertEquals("/session", response.getBody().path());
    }

    @Test
    void deveRetornarNotFoundParaRecursoInexistente() {
        ResponseEntity<ApiError> response = handler.handleNotFound(
                new ResourceNotFoundException("Sessão inexistente."),
                request("/session/42")
        );

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Sessão inexistente.", response.getBody().message());
    }

    @Test
    void naoDeveExporDetalhesDeErroInterno() {
        ResponseEntity<ApiError> response = handler.handleUnexpectedException(
                new IllegalStateException("detalhe interno"),
                request("/session")
        );

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Ocorreu um erro interno.", response.getBody().message());
    }

    private MockHttpServletRequest request(String path) {
        return new MockHttpServletRequest("GET", path);
    }
}
