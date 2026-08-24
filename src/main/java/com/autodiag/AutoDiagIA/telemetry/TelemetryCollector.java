package com.autodiag.AutoDiagIA.telemetry;

import com.autodiag.AutoDiagIA.obd.ObdClient;
import com.autodiag.AutoDiagIA.obd.ObdPid;
import com.autodiag.AutoDiagIA.obd.ObdReading;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TelemetryCollector {

    private final ObdClient obdClient;

    public TelemetryCollector(ObdClient obdClient) {
        this.obdClient = obdClient;
    }

    public List<ObdReading> collect(List<ObdPid> pids) throws IOException {

        List<ObdReading> readings = new ArrayList<>();

        for (ObdPid pid : pids) {
            readings.add(obdClient.read(pid));
        }

        return readings;
    }
}