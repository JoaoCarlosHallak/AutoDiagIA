package com.autodiag.AutoDiagIA.dto;

import java.util.List;

import com.autodiag.AutoDiagIA.session.AnalysisMode;
import com.autodiag.AutoDiagIA.session.TriggerConfig;

public record CreateSessionRequest(
        String vehicle,
        List<String> pids,
        long intervalMs,
        String startReason,
        long maxDurationMs,
        TriggerConfig trigger,
        AnalysisMode analysisMode
) {}