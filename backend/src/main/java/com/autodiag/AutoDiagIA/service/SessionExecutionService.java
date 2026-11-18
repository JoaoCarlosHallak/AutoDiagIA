package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.dto.SessionDTO;
import com.autodiag.AutoDiagIA.entity.Session;
import com.autodiag.AutoDiagIA.obd.ObdPid;
import com.autodiag.AutoDiagIA.obd.ObdReading;
import com.autodiag.AutoDiagIA.session.SessionEndReason;

import java.time.Instant;
import java.util.List;

public interface SessionExecutionService {
    void start(SessionDTO sessionDTO);
    void finish(SessionDTO sessionDTO, SessionEndReason reason);
    void shutdown();
    void cancelTask(Long sessionId);

}
