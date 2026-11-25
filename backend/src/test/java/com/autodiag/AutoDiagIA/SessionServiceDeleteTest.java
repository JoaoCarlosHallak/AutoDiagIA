package com.autodiag.AutoDiagIA;

import com.autodiag.AutoDiagIA.entity.Session;
import com.autodiag.AutoDiagIA.exception.InvalidOperationException;
import com.autodiag.AutoDiagIA.repository.LogRepository;
import com.autodiag.AutoDiagIA.repository.SessionPidRepository;
import com.autodiag.AutoDiagIA.repository.SessionRepository;
import com.autodiag.AutoDiagIA.service.SessionExecutionService;
import com.autodiag.AutoDiagIA.service.SessionServiceImpl;
import com.autodiag.AutoDiagIA.session.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionServiceDeleteTest {

    private SessionRepository sessionRepository;
    private SessionPidRepository sessionPidRepository;
    private LogRepository logRepository;
    private SessionExecutionService sessionExecutionService;
    private SessionServiceImpl service;

    @BeforeEach
    void setUp() {
        sessionRepository = mock(SessionRepository.class);
        sessionPidRepository = mock(SessionPidRepository.class);
        logRepository = mock(LogRepository.class);
        sessionExecutionService = mock(SessionExecutionService.class);
        service = new SessionServiceImpl(
                sessionRepository,
                sessionPidRepository,
                logRepository,
                mock(ModelMapper.class),
                sessionExecutionService
        );
    }

    @Test
    void deveExcluirPidsELogsAntesDaSessao() {
        Session session = session(SessionStatus.FINISHED);
        when(sessionRepository.findById(42L)).thenReturn(Optional.of(session));

        service.delete(42L);

        var deletionOrder = inOrder(
                sessionExecutionService,
                sessionPidRepository,
                logRepository,
                sessionRepository
        );
        deletionOrder.verify(sessionExecutionService).cancelTask(42L);
        deletionOrder.verify(sessionPidRepository).deleteAllBySessionId(42L);
        deletionOrder.verify(logRepository).deleteAllBySessionId(42L);
        deletionOrder.verify(sessionRepository).delete(session);
    }

    @Test
    void naoDeveExcluirSessaoEmExecucao() {
        when(sessionRepository.findById(42L))
                .thenReturn(Optional.of(session(SessionStatus.RUNNING)));

        assertThrows(InvalidOperationException.class, () -> service.delete(42L));

        verify(sessionPidRepository, never()).deleteAllBySessionId(42L);
        verify(logRepository, never()).deleteAllBySessionId(42L);
        verify(sessionRepository, never()).delete(org.mockito.ArgumentMatchers.any(Session.class));
        verify(sessionExecutionService, never()).cancelTask(42L);
    }

    private Session session(SessionStatus status) {
        Session session = new Session();
        session.setStatus(status);
        return session;
    }
}
