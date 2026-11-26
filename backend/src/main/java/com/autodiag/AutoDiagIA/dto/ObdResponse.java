package com.autodiag.AutoDiagIA.dto;

public sealed interface ObdResponse permits
    ObdReading, MonitorStatusDTO, MonitorStatusCycleDTO {
}
