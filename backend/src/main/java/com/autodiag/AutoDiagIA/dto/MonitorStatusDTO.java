package com.autodiag.AutoDiagIA.dto;

import com.autodiag.AutoDiagIA.dto.monitor.IgnitionType;
import com.autodiag.AutoDiagIA.dto.monitor.MonitorDTO;

import java.util.List;

public record MonitorStatusDTO(
        boolean milOn, int DtcS, IgnitionType ignitionType, List<MonitorDTO> monitors
) implements ObdResponse {}