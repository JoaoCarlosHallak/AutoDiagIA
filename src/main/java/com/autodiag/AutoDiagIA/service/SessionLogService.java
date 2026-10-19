package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.dto.LogDTO;
import com.autodiag.AutoDiagIA.entity.Session;
import com.autodiag.AutoDiagIA.obd.ObdReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

public interface SessionLogService {
    void saveReadings(Session session, List<ObdReading> readings);

    @Transactional(readOnly = true)
    Page<LogDTO> getSessionLogs(
            Long sessionId,
            String pid,
            Instant fromTime,
            Instant toTime,
            Pageable pageable);
}
