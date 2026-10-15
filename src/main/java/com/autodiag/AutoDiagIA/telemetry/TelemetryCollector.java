package com.autodiag.AutoDiagIA.telemetry;

import com.autodiag.AutoDiagIA.exception.ObdResponseException;
import com.autodiag.AutoDiagIA.obd.ObdClient;
import com.autodiag.AutoDiagIA.obd.ObdPid;
import com.autodiag.AutoDiagIA.obd.ObdReading;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;



@Component 
public class TelemetryCollector {

    private final ObdClient obdClient;

    public TelemetryCollector(ObdClient obdClient) {
        this.obdClient = obdClient;
    }

    public List<ObdReading> collect(List<ObdPid> pids)
            throws IOException {

        List<ObdReading> readings = new ArrayList<>();

        for (ObdPid pid : pids) {
            try {
                readings.add(obdClient.read(pid));

            } catch (ObdResponseException e) {
                System.err.println(
                    "Falha ao consultar PID "
                    + pid.getCode() + ": " + e.getMessage());
            }
        }

        return readings;
    }
}