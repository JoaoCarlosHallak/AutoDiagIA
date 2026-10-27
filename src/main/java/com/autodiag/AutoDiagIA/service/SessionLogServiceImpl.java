package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.dto.LogDTO;
import com.autodiag.AutoDiagIA.dto.SessionDTO;
import com.autodiag.AutoDiagIA.entity.Log;
import com.autodiag.AutoDiagIA.entity.Session;
import com.autodiag.AutoDiagIA.obd.ObdReading;
import com.autodiag.AutoDiagIA.repository.LogRepository;

import com.autodiag.AutoDiagIA.repository.SessionRepository;
import com.autodiag.AutoDiagIA.session.SessionStatus;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class SessionLogServiceImpl implements SessionLogService {

    private final LogRepository logRepository;
    private final SessionRepository sessionRepository;
    private final ModelMapper modelMapper;
    private final AIService  aiService;



    @Autowired
    public SessionLogServiceImpl(LogRepository logRepository, SessionRepository sessionRepository, ModelMapper modelMapper, AIService aiService) {
        this.logRepository = logRepository;
        this.sessionRepository = sessionRepository;
        this.modelMapper = modelMapper;
        this.aiService = aiService;
    }

    @Transactional
    public void saveReadings(Session session, List<ObdReading> readings) {
        List<Log> logs = new ArrayList<>();

        for (ObdReading reading : readings) {
            reading.values().forEach(value -> {
                Log log = new Log();

                log.setSession(session);
                log.setPid(reading.pid());
                log.setValueName(value.name());
                log.setValue(value.value());
                log.setUnit(value.unit());
                log.setTimestamp(reading.timestamp());

                logs.add(log);
            });
        }

        if (!logs.isEmpty()) {
            logRepository.saveAll(logs);
        }
    }



    @Transactional(readOnly = true)
    @Override
    public Page<LogDTO> getSessionLogs(
            Long sessionId,
            String pid,
            Instant fromTime,
            Instant toTime,
            Pageable pageable) {

        if (!sessionRepository.existsById(sessionId)) {
            throw new EntityNotFoundException(
                    "Sessão de id: " + sessionId + " inexistente"
            );
        }

        if (fromTime != null && toTime != null
                && fromTime.isAfter(toTime)) {
            throw new IllegalArgumentException(
                    "A data inicial não pode ser posterior à data final."
            );
        }

        if (pid != null && pid.isBlank()) {
            pid = null;
        }

        Page<Log> logs;

        if (pid != null && !pid.isBlank()) {
            logs = logRepository.findBySession_IdAndPid(
                    sessionId, pid, pageable
            );
        } else {
            logs = logRepository.findBySession_Id(
                    sessionId, pageable
            );
        }

        return logs.map(log -> modelMapper.map(log, LogDTO.class));
    }


    @Override
    public String analyse(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Sessão de id: " + sessionId + " inexistente"
                        ));

        if (session.getStatus() != SessionStatus.FINISHED) {
            throw new IllegalStateException(
                    "Não é possível analisar uma sessão no estado "
                            + session.getStatus()
            );
        }
        List<LogDTO> sessionData =
                logRepository.findBySession_IdOrderByTimestampAsc(sessionId)
                        .stream()
                        .map(log -> modelMapper.map(log, LogDTO.class))
                        .toList();

        if (sessionData.isEmpty()) {
            throw new IllegalStateException(
                    "Não existem registros de telemetria para analisar."
            );
        }


        return aiService.analyseSession(modelMapper.map(session, SessionDTO.class), sessionData);
    }
    }
