package com.autodiag.AutoDiagIA.obd;

import java.time.Instant;
import java.util.List;

public record DTCReading(String mode, List<String> codes, Instant instant) {
}
