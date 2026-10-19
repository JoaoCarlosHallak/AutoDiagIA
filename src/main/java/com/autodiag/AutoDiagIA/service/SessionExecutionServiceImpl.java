package com.autodiag.AutoDiagIA.service;

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

    private final ScheduledExecutorService executor =
            Executors.newScheduledThreadPool(1);

    private final Map<Long, ScheduledFuture<?>> tasks =
            new ConcurrentHashMap<>();

    public SessionExecutionServiceImpl(
            SessionRepository sessionRepository,
            SessionPidRepository sessionPidRepository,
            TelemetryCollector telemetryCollector,
            SessionLogService sessionLogService) {

        this.sessionRepository = sessionRepository;
        this.sessionPidRepository = sessionPidRepository;
        this.telemetryCollector = telemetryCollector;
        this.sessionLogService = sessionLogService;
    }

    public synchronized void start(Long sessionId) {
        if (tasks.containsKey(sessionId)) {
            throw new IllegalStateException(
                    "A sessão já possui uma tarefa de coleta."
            );
        }

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Sessão inexistente: " + sessionId
                ));

        if (session.getStatus() != SessionStatus.RUNNING) {
            throw new IllegalStateException(
                    "A sessão precisa estar RUNNING para iniciar a coleta."
            );
        }

        if (session.getIntervalMs() <= 0
                || session.getMaxDurationMs() <= 0) {
            throw new IllegalStateException(
                    "Intervalo e duração máxima devem ser positivos."
            );
        }

        List<ObdPid> pids = sessionPidRepository
                .findBySession_Id(sessionId)
                .stream()
                .map(SessionPid::getPid)
                .map(ObdPid::fromString)
                .toList();

        if (pids.isEmpty()) {
            throw new IllegalStateException(
                    "A sessão não possui PIDs configurados."
            );
        }

        ScheduledFuture<?> task = executor.scheduleWithFixedDelay(
                () -> executeCycle(sessionId, pids),
                0,
                session.getIntervalMs(),
                TimeUnit.MILLISECONDS
        );

        tasks.put(sessionId, task);
    }

    private void executeCycle(Long sessionId, List<ObdPid> pids) {
        try {
            Session session = sessionRepository.findById(sessionId)
                    .orElse(null);

            if (session == null
                    || session.getStatus() != SessionStatus.RUNNING) {
                cancelTask(sessionId);
                return;
            }

            Instant now = Instant.now();

            if (elapsedMs(session.getStartedAt(), now)
                    >= session.getMaxDurationMs()) {
                finish(sessionId, SessionEndReason.MAX_DURATION_REACHED);
                return;
            }

            List<ObdReading> readings = telemetryCollector.collect(pids);


            session = sessionRepository.findById(sessionId).orElse(null);

            if (session == null
                    || session.getStatus() != SessionStatus.RUNNING) {
                cancelTask(sessionId);
                return;
            }


            if (elapsedMs(session.getStartedAt(), Instant.now())
                    >= session.getMaxDurationMs()) {
                finish(sessionId, SessionEndReason.MAX_DURATION_REACHED);
                return;
            }

            sessionLogService.saveReadings(session, readings);

            if (triggerReached(session, readings)) {
                finish(sessionId, SessionEndReason.TRIGGER_REACHED);
            }

        } catch (IOException | RuntimeException e) {
            System.err.println(
                    "Falha na coleta da sessão " + sessionId
                            + ": " + e.getMessage()
            );

            finish(sessionId, SessionEndReason.ERROR);
        }
    }

    private boolean triggerReached(Session session, List<ObdReading> readings) {

        if (session.getTriggerPid() == null
                || session.getTriggerOperator() == null
                || session.getTriggerValue() == null) {
            return false;
        }

        ObdPid triggerPid;

        try {
            triggerPid = ObdPid.fromString(session.getTriggerPid());
        } catch (IllegalArgumentException e) {
            return false;
        }

        for (ObdReading reading : readings) {
            ObdPid readingPid;

            try {
                readingPid = ObdPid.fromString(reading.pid());
            } catch (IllegalArgumentException e) {
                continue;
            }

            if (readingPid != triggerPid) {
                continue;
            }

            int index = session.getTriggerValueIndex() == null
                    ? 0 : session.getTriggerValueIndex();

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
    public void finish(Long sessionId, SessionEndReason reason) {
        Session session = sessionRepository.findById(sessionId)
                .orElse(null);

        if (session != null
                && session.getStatus() == SessionStatus.RUNNING) {

            session.setFinishedAt(Instant.now());
            session.setEndReason(reason);

            session.setStatus(
                    reason == SessionEndReason.ERROR
                            ? SessionStatus.FAILED
                            : SessionStatus.FINISHED
            );

            sessionRepository.save(session);
        }

        cancelTask(sessionId);
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