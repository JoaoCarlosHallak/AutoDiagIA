package com.autodiag.AutoDiagIA.service;

import java.util.List;

import com.autodiag.AutoDiagIA.dto.ObdResponse;

public interface TelemetryService {

    List<ObdResponse> getPidsData(List<String> pids);
    ObdResponse getPidData(String name);

}
