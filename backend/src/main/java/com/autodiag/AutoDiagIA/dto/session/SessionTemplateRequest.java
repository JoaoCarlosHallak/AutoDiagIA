package com.autodiag.AutoDiagIA.dto.session;

import com.autodiag.AutoDiagIA.session.TriggerConfig;

import java.util.List;

public record SessionTemplateRequest(
        String name,
        String startReason,
        List<String> pids,
        boolean includeSessionSettings,
        Long intervalMs,
        Long maxDurationMs,
        TriggerConfig trigger
) {}
