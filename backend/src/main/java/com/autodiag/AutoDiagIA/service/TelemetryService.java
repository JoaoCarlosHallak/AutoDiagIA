package com.autodiag.AutoDiagIA.service;

import java.io.IOException;
import java.util.List;

import com.autodiag.AutoDiagIA.obd.ObdReading;

public interface TelemetryService {

    List<ObdReading> getPidsData(List<String> pids);
    ObdReading getPidData(String name);

}
