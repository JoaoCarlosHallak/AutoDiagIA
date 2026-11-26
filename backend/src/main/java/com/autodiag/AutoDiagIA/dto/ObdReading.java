package com.autodiag.AutoDiagIA.dto;

import com.autodiag.AutoDiagIA.obd.ObdValue;

import java.time.Instant;
import java.util.List;

public record ObdReading(
        String pid,
        List<ObdValue> values,
        Instant timestamp
) implements ObdResponse {
}