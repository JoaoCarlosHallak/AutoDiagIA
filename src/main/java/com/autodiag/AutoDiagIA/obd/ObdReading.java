package com.autodiag.AutoDiagIA.obd;

import java.time.Instant;

public record ObdReading(
        ObdPid pid,
        double value,
        String unit,
        Instant timestamp
) {
}