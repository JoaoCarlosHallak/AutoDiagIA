package com.autodiag.AutoDiagIA.service;

import java.time.Instant;
import java.util.List;

import com.autodiag.AutoDiagIA.dto.SessionDTO;
import com.autodiag.AutoDiagIA.session.SessionEndReason;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autodiag.AutoDiagIA.dto.CreateSessionRequest;
import com.autodiag.AutoDiagIA.entity.Session;
import com.autodiag.AutoDiagIA.entity.SessionPid;
import com.autodiag.AutoDiagIA.obd.ObdPid;
import com.autodiag.AutoDiagIA.session.SessionStatus;
import com.autodiag.AutoDiagIA.repository.SessionPidRepository;
import com.autodiag.AutoDiagIA.repository.SessionRepository;

import jakarta.transaction.Transactional;

@Service 
public class SessionServiceImpl implements SessionService {


    private final SessionRepository sessionRepository;
    private final SessionPidRepository sessionPidRepository;
    private final ModelMapper modelMapper;
    private final SessionExecutionService sessionExecutionService;


    @Autowired 
    public SessionServiceImpl(SessionRepository sessionRepository, SessionPidRepository sessionPidRepository, ModelMapper modelMapper, SessionExecutionService sessionExecutionService) {
        this.sessionRepository = sessionRepository;
        this.sessionPidRepository = sessionPidRepository;
        this.modelMapper = modelMapper;
        this.sessionExecutionService = sessionExecutionService;

    }

    @Override
    @Transactional
    public SessionDTO create(CreateSessionRequest request) {

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
                    ObdPid.fromString(request.trigger().getPid()).getCode()
            );
            session.setTriggerOperator(request.trigger().getOperator());
            session.setTriggerValue(request.trigger().getTriggerValue());
            session.setTriggerValueIndex(request.trigger().getValueIndex());
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

        return modelMapper.map(savedSession, SessionDTO.class);
    }

    @Override
    public SessionDTO start(Long id) {

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Sessão de id: " + id + " inexistente"
                ));

        if (session.getStatus() != SessionStatus.CREATED) {
            throw new IllegalStateException(
                    "Não é possível iniciar uma sessão no estado "
                            + session.getStatus()
            );
        }

        session.setStartedAt(Instant.now());
        session.setStatus(SessionStatus.RUNNING);

        sessionRepository.save(session);

        try {
            sessionExecutionService.start(session.getId());
        } catch (RuntimeException e) {
            sessionExecutionService.finish(
                    session.getId(),
                    SessionEndReason.ERROR
            );
            throw e;
        }

        return modelMapper.map(session, SessionDTO.class);
    }


    @Override
    public SessionDTO stop(Long id) {

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Sessão de id: " + id + " inexistente"
                ));

        if (session.getStatus() != SessionStatus.RUNNING) {
            throw new IllegalStateException(
                    "Não é possível encerrar uma sessão no estado "
                            + session.getStatus()
            );
        }

        sessionExecutionService.finish(
                id,
                SessionEndReason.USER_STOPPED
        );

        session = sessionRepository.findById(id).orElseThrow();

        return modelMapper.map(session, SessionDTO.class);
    }

    @Override
    public List<SessionDTO> findAll() {
        return sessionRepository.findAll().stream().map(session -> modelMapper.map(session, SessionDTO.class)).toList();
    }

    @Override
    public SessionDTO findById(Long id){
        return modelMapper.map(
                sessionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Sessão de id: " + id + " inexistente")),
                SessionDTO.class);

    }
}

