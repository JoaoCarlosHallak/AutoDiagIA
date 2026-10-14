package com.autodiag.AutoDiagIA.service;

import java.io.IOException;
import java.util.List;

import com.autodiag.AutoDiagIA.obd.ObdReading;

public interface TelemetryService {

    List<ObdReading> getPidsData(List<String> pids) throws IOException;
    ObdReading getPidData(String name) throws IOException;

}
