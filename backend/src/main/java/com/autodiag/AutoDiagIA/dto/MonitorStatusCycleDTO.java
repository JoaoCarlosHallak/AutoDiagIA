package com.autodiag.AutoDiagIA.dto.monitor;

import java.util.List;

public record MonitorStatusCycleDTO(IgnitionType ignitionType, List<MonitorDTO> monitors){}