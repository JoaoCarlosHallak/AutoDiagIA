package com.autodiag.AutoDiagIA.obd;

import java.time.Instant;

public record ObdReading(
        String pid,
        double value,
        String unit,
        Instant timestamp
) {
}