package com.autodiag.AutoDiagIA.dto.monitor;

public record MonitorDTO(
    MonitorType type,
    boolean supported,
    Boolean complete
) {}