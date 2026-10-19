package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.dto.LogDTO;
import com.autodiag.AutoDiagIA.entity.Log;
import com.autodiag.AutoDiagIA.entity.Session;
import com.autodiag.AutoDiagIA.obd.ObdReading;
import com.autodiag.AutoDiagIA.repository.LogRepository;

import com.autodiag.AutoDiagIA.repository.SessionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class SessionLogServiceImpl implements SessionLogService {

    private final LogRepository logRepository;
    private final SessionRepository sessionRepository;
    private final ModelMapper modelMapper;


    public SessionLogServiceImpl(LogRepository logRepository, SessionRepository sessionRepository, ModelMapper modelMapper) {
        this.logRepository = logRepository;
        this.sessionRepository = sessionRepository;
        this.modelMapper = modelMapper;
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
}