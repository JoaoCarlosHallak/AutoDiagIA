package com.autodiag.AutoDiagIA.dto.monitor;

import java.util.List;

public record MonitorStatusDTO(
    boolean milOn, int DtcS, IgnitionType ignitionType, List<MonitorDTO> monitors
) {}