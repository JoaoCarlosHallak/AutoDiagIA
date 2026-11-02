package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.dto.SessionDTO;
import com.autodiag.AutoDiagIA.entity.Session;
import com.autodiag.AutoDiagIA.entity.SessionPid;
import com.autodiag.AutoDiagIA.exception.ObdResponseException;
import com.autodiag.AutoDiagIA.obd.ObdClient;
import com.autodiag.AutoDiagIA.obd.ObdPid;
import com.autodiag.AutoDiagIA.obd.ObdReading;
import com.autodiag.AutoDiagIA.repository.SessionPidRepository;
import com.autodiag.AutoDiagIA.repository.SessionRepository;
import com.autodiag.AutoDiagIA.session.SessionEndReason;
import com.autodiag.AutoDiagIA.session.SessionStatus;
import com.autodiag.AutoDiagIA.session.TriggerOperator;
import com.autodiag.AutoDiagIA.telemetry.TelemetryCollector;

import jakarta.annotation.PreDestroy;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class SessionExecutionServiceImpl implements SessionExecutionService {

    private final SessionRepository sessionRepository;
    private final SessionPidRepository sessionPidRepository;
    private final TelemetryCollector telemetryCollector;
    private final SessionLogService sessionLogService;
    private final ModelMapper modelMapper;

    private final ScheduledExecutorService executor =
            Executors.newScheduledThreadPool(1);

    private final Map<Long, ScheduledFuture<?>> tasks =
            new ConcurrentHashMap<>();

    public SessionExecutionServiceImpl(
            SessionRepository sessionRepository,
            SessionPidRepository sessionPidRepository,
            TelemetryCollector telemetryCollector,
            SessionLogService sessionLogService, ModelMapper modelMapper) {

        this.sessionRepository = sessionRepository;
        this.sessionPidRepository = sessionPidRepository;
        this.telemetryCollector = telemetryCollector;
        this.sessionLogService = sessionLogService;
        this.modelMapper = modelMapper;
    }

    public synchronized void start(SessionDTO session) {
        if (tasks.containsKey(session.getSessionId())) {
            throw new IllegalStateException(
                    "A sessão já possui uma tarefa de coleta."
            );
        }

        List<ObdPid> pids = sessionPidRepository
                .findBySession_Id(session.getSessionId())
                .stream()
                .map(SessionPid::getPid)
                .map(ObdPid::fromString)
                .toList();


        ScheduledFuture<?> task = executor.scheduleWithFixedDelay(
                () -> executeCycle(session, pids),
                0,
                session.getIntervalMs(),
                TimeUnit.MILLISECONDS
        );

        tasks.put(session.getSessionId(), task);
    }

    private void executeCycle(SessionDTO sessionDTO, List<ObdPid> pids) {
        Long sessionId = sessionDTO.getSessionId();
        try {

            Instant now = Instant.now();

            if (elapsedMs(sessionDTO.getStartedAt(), now)
                    >= sessionDTO.getMaxDurationMs()) {
                finish(sessionDTO, SessionEndReason.MAX_DURATION_REACHED);
                return;
            }

            List<ObdReading> readings = telemetryCollector.collect(pids);


            Session session = modelMapper.map(sessionDTO, Session.class);
            sessionLogService.saveReadings(session, readings);

            if (triggerReached(session, readings)) {
                finish(sessionDTO, SessionEndReason.TRIGGER_REACHED);
            }

        } catch (IOException | RuntimeException e) {
            System.err.println(
                    "Falha na coleta da sessão " + sessionId
                            + ": " + e.getMessage()
            );

            finish(sessionDTO, SessionEndReason.ERROR);
        }
    }

    private boolean triggerReached(Session session, List<ObdReading> readings) {
        for (ObdReading reading : readings) {

            int index = session.getTriggerValueIndex();

            if (index < 0 || index >= reading.values().size()) {
                return false;
            }

            Double actual = reading.values().get(index).value();

            if (actual == null) {
                return false;
            }

            double expected = session.getTriggerValue();

            return switch (session.getTriggerOperator()) {
                case GT -> actual > expected;
                case GTE -> actual >= expected;
                case LT -> actual < expected;
                case LTE -> actual <= expected;
                case EQ -> Double.compare(actual, expected) == 0;
            };
        }


        return false;
    }

    @Transactional
    public void finish(SessionDTO sessionDTO, SessionEndReason reason) {
        Session session = sessionRepository.findById(sessionDTO.getSessionId())
                .orElse(null);

        session.setFinishedAt(Instant.now());
        session.setEndReason(reason);

        session.setStatus(
                reason == SessionEndReason.ERROR
                        ? SessionStatus.FAILED
                        : SessionStatus.FINISHED
            );

            sessionRepository.save(session);
            cancelTask(session.getId());
    }

    private long elapsedMs(Instant start, Instant now) {
        if (start == null) {
            return 0;
        }

        return java.time.Duration.between(start, now).toMillis();
    }

    public void cancelTask(Long sessionId) {
        ScheduledFuture<?> task = tasks.remove(sessionId);

        if (task != null) {
            task.cancel(false);
        }
    }

    @PreDestroy
    public void shutdown() {
        tasks.values().forEach(task -> task.cancel(false));
        tasks.clear();
        executor.shutdownNow();
    }
}