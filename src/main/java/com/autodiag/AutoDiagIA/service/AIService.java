package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.dto.LogDTO;
import com.autodiag.AutoDiagIA.dto.SessionDTO;

import java.util.List;

public interface AIService {
    String analyseSession(SessionDTO sessionDTO, List<LogDTO> sessionData);
}
