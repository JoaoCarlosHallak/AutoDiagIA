package com.autodiag.AutoDiagIA.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autodiag.AutoDiagIA.dto.CreateSessionRequest;
import com.autodiag.AutoDiagIA.entity.Session;
import com.autodiag.AutoDiagIA.entity.SessionPid;
import com.autodiag.AutoDiagIA.obd.ObdPid;
import com.autodiag.session.SessionStatus;
import com.repository.SessionPidRepository;
import com.repository.SessionRepository;

import jakarta.transaction.Transactional;

@Service 
public class SessionServiceImpl implements SessionService {


    private final SessionRepository sessionRepository;
    private final SessionPidRepository sessionPidRepository;

    @Autowired 
    public SessionServiceImpl(SessionRepository sessionRepository, SessionPidRepository sessionPidRepository) {
        this.sessionRepository = sessionRepository;
        this.sessionPidRepository = sessionPidRepository;
    }

    @Transactional
    public Session create(CreateSessionRequest request) {

        if (request.pids() == null || request.pids().isEmpty()) {
            throw new IllegalArgumentException(
                    "Informe pelo menos um PID."
            );
        }

        if (request.intervalMs() <= 0 || request.maxDurationMs() <= 0) {
            throw new IllegalArgumentException(
                    "Intervalo e duração máxima devem ser positivos."
            );
        }

        List<String> validPids = request.pids().stream()
                .map(pid -> ObdPid.fromString(pid).getCode()).toList();

        Session session = new Session();
        session.setVehicle(request.vehicle());
        session.setStartReason(request.startReason());
        session.setStatus(SessionStatus.CREATED);
        session.setAnalysisMode(request.analysisMode());
        session.setIntervalMs(request.intervalMs());
        session.setMaxDurationMs(request.maxDurationMs());
        session.setCreatedAt(Instant.now());

        if (request.trigger() != null) {
            session.setTriggerPid(
                    ObdPid.fromString(request.trigger().pid()).getCode()
            );
            session.setTriggerOperator(request.trigger().operator());
            session.setTriggerValue(request.trigger().triggerValue());
            session.setTriggerValueIndex(request.trigger().valueIndex());
        }

        Session savedSession = sessionRepository.save(session);

        List<SessionPid> sessionPids = validPids.stream()
                .map(pid -> {
                    SessionPid sessionPid = new SessionPid();
                    sessionPid.setSession(savedSession);
                    sessionPid.setPid(pid);
                    return sessionPid;
                })
                .toList();

        sessionPidRepository.saveAll(sessionPids);

        return savedSession;
    }
}