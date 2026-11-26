package com.autodiag.AutoDiagIA.obd;

import java.time.Instant;
import java.util.List;

public record ObdReading(
        String pid,
        List<ObdValue> values,
        Instant timestamp
) {
}