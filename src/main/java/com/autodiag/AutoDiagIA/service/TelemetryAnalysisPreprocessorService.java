package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.dto.LogDTO;

import java.util.List;

public interface TelemetryAnalysisPreprocessorService {
    String prepareData(List<LogDTO> logs);
}
