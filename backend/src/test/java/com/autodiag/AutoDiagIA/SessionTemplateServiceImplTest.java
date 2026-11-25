package com.autodiag.AutoDiagIA;

import com.autodiag.AutoDiagIA.dto.SessionTemplateDTO;
import com.autodiag.AutoDiagIA.dto.SessionTemplateRequest;
import com.autodiag.AutoDiagIA.entity.SessionTemplate;
import com.autodiag.AutoDiagIA.exception.InvalidRequestException;
import com.autodiag.AutoDiagIA.repository.SessionTemplateRepository;
import com.autodiag.AutoDiagIA.service.SessionTemplateServiceImpl;
import com.autodiag.AutoDiagIA.session.TriggerConfig;
import com.autodiag.AutoDiagIA.session.TriggerOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionTemplateServiceImplTest {

    private SessionTemplateRepository repository;
    private SessionTemplateServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(SessionTemplateRepository.class);
        service = new SessionTemplateServiceImpl(repository);
        when(repository.save(any(SessionTemplate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void deveSalvarModeloSimplesComPidsNormalizados() {
        SessionTemplateDTO result = service.create(new SessionTemplateRequest(
                "  Marcha lenta ",
                "Avaliar motor em marcha lenta.",
                List.of("0c", "0D", "0C"),
                false,
                null,
                null,
                null
        ));

        assertEquals("Marcha lenta", result.name());
        assertEquals(List.of("0C", "0D"), result.pids());
        assertFalse(result.includeSessionSettings());
        assertNull(result.intervalMs());
        assertNull(result.trigger());
        verify(repository).save(any(SessionTemplate.class));
    }

    @Test
    void deveSalvarConfiguracaoCompletaComTriggerOpcional() {
        SessionTemplateDTO result = service.create(new SessionTemplateRequest(
                "Temperatura alta",
                "Monitorar temperatura do motor.",
                List.of("05", "0C"),
                true,
                1500L,
                600000L,
                new TriggerConfig("05", 0, TriggerOperator.GTE, 105.0)
        ));

        assertTrue(result.includeSessionSettings());
        assertEquals(1500L, result.intervalMs());
        assertEquals(600000L, result.maxDurationMs());
        assertEquals("05", result.trigger().getPid());
        assertEquals(105.0, result.trigger().getTriggerValue());
    }

    @Test
    void deveRejeitarPidInvalidoSemPersistir() {
        assertThrows(InvalidRequestException.class, () -> service.create(
                new SessionTemplateRequest(
                        "Modelo inválido",
                        "Objetivo",
                        List.of("FF"),
                        false,
                        null,
                        null,
                        null
                )
        ));
    }
}
