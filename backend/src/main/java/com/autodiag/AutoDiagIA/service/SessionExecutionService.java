package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.dto.session.SessionDTO;
import com.autodiag.AutoDiagIA.session.SessionEndReason;

public interface SessionExecutionService {
    void start(SessionDTO sessionDTO);
    void finish(SessionDTO sessionDTO, SessionEndReason reason);
    void shutdown();
    void cancelTask(Long sessionId);

}
