package com.autodiag.AutoDiagIA.service;

import java.time.Instant;
import java.util.List;

import com.autodiag.AutoDiagIA.dto.SessionDTO;
import com.autodiag.AutoDiagIA.session.SessionEndReason;
import com.autodiag.AutoDiagIA.exception.InvalidObdRequestException;
import com.autodiag.AutoDiagIA.exception.InvalidOperationException;
import com.autodiag.AutoDiagIA.exception.InvalidRequestException;
import com.autodiag.AutoDiagIA.exception.ResourceNotFoundException;
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
import com.autodiag.AutoDiagIA.repository.LogRepository;

import jakarta.transaction.Transactional;

@Service 
public class SessionServiceImpl implements SessionService {


    private final SessionRepository sessionRepository;
    private final SessionPidRepository sessionPidRepository;
    private final LogRepository logRepository;
    private final ModelMapper modelMapper;
    private final SessionExecutionService sessionExecutionService;


    @Autowired
    public SessionServiceImpl(SessionRepository sessionRepository, SessionPidRepository sessionPidRepository, LogRepository logRepository, ModelMapper modelMapper, SessionExecutionService sessionExecutionService) {
        this.sessionRepository = sessionRepository;
        this.sessionPidRepository = sessionPidRepository;
        this.logRepository = logRepository;
        this.modelMapper = modelMapper;
        this.sessionExecutionService = sessionExecutionService;
    }

    @Override
    @Transactional
    public SessionDTO create(CreateSessionRequest request) {
        if (request == null) {
            throw new InvalidRequestException("Os dados da sessão não foram informados.");
        }

        if (request.pids() == null || request.pids().isEmpty()) {
            throw new InvalidRequestException(
                    "Informe pelo menos um PID."
            );
        }

        if (request.intervalMs() <= 0 || request.maxDurationMs() <= 0) {
            throw new InvalidRequestException(
                    "Intervalo e duração máxima devem ser positivos."
            );
        }

        List<String> validPids = request.pids().stream()
                .map(this::toPidCode)
                .toList();

        Session session = new Session();
        session.setVehicle(request.vehicle());
        session.setStartReason(request.startReason());
        session.setStatus(SessionStatus.CREATED);
        session.setIntervalMs(request.intervalMs());
        session.setMaxDurationMs(request.maxDurationMs());
        session.setCreatedAt(Instant.now());

        if (request.trigger() != null) {
            if (request.trigger().getValueIndex() == null
                    || request.trigger().getValueIndex() < 0
                    || request.trigger().getOperator() == null
                    || request.trigger().getTriggerValue() == null) {
                throw new InvalidRequestException(
                        "A configuração do gatilho está incompleta."
                );
            }
            session.setTriggerPid(
                    toPidCode(request.trigger().getPid())
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
    @Transactional
    public SessionDTO start(Long id) {

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sessão de id: " + id + " inexistente"
                ));

        if (session.getStatus() != SessionStatus.CREATED) {
            throw new InvalidOperationException(
                    "Não é possível iniciar uma sessão no estado "
                            + session.getStatus()
            );
        }

        session.setStartedAt(Instant.now());
        session.setStatus(SessionStatus.RUNNING);

        SessionDTO sessionDTO = modelMapper.map(sessionRepository.save(session), SessionDTO.class);

        try {
            sessionExecutionService.start(sessionDTO);
        } catch (RuntimeException e) {
            try {
                sessionExecutionService.finish(
                        sessionDTO,
                        SessionEndReason.ERROR
                );
            } catch (RuntimeException finishException) {
                e.addSuppressed(finishException);
            }
            throw e;
        }

        return sessionDTO;
    }


    @Override
    public SessionDTO stop(Long id) {

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sessão de id: " + id + " inexistente"
                ));

        if (session.getStatus() != SessionStatus.RUNNING) {
            throw new InvalidOperationException(
                    "Não é possível encerrar uma sessão no estado "
                            + session.getStatus()
            );
        }

        sessionExecutionService.finish(
                modelMapper.map(session, SessionDTO.class),
                SessionEndReason.USER_STOPPED
        );

        session = sessionRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Sessão de id: " + id + " inexistente"));

        return modelMapper.map(session, SessionDTO.class);
    }

    @Override
    public List<SessionDTO> findAll() {
        return sessionRepository.findAll().stream().map(session -> modelMapper.map(session, SessionDTO.class)).toList();
    }

    @Override
    public SessionDTO findById(Long id){
        return modelMapper.map(
                sessionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sessão de id: " + id + " inexistente")),
                SessionDTO.class);

    }

    @Override
    @Transactional
    public void delete(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sessão de id: " + id + " inexistente"
                ));

        if (session.getStatus() == SessionStatus.RUNNING) {
            throw new InvalidOperationException(
                    "Encerre a aquisição antes de excluir a sessão."
            );
        }

        sessionExecutionService.cancelTask(id);
        sessionPidRepository.deleteAllBySessionId(id);
        logRepository.deleteAllBySessionId(id);
        sessionRepository.delete(session);
    }

    private String toPidCode(String pid) {
        try {
            return ObdPid.fromString(pid).getCode();
        } catch (IllegalArgumentException e) {
            throw new InvalidObdRequestException("PID inválido: " + pid, e);
        }
    }

}

