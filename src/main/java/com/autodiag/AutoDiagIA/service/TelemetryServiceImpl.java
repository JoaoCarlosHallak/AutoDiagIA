package com.autodiag.AutoDiagIA.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.autodiag.AutoDiagIA.exception.InvalidObdRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autodiag.AutoDiagIA.obd.ObdPid;
import com.autodiag.AutoDiagIA.obd.ObdReading;
import com.autodiag.AutoDiagIA.service.TelemetryService;
import com.autodiag.AutoDiagIA.telemetry.TelemetryCollector;

@Service
public class TelemetryServiceImpl implements TelemetryService {

    private final TelemetryCollector telemetryCollector;

    @Autowired 
    public TelemetryServiceImpl(
            TelemetryCollector telemetryCollector) {
        this.telemetryCollector = telemetryCollector;
    }

    @Override
    public List<ObdReading> getPidsData(List<String> pids) throws IOException {

        List<ObdPid> validPids = new ArrayList<>();

        if(pids.contains("all")){
            return telemetryCollector.collect(List.of(ObdPid.values()));
        }

        for (String pid : pids) {
            try {
                ObdPid obdPid = ObdPid.fromString(pid);
                validPids.add(obdPid);
            } catch (IllegalArgumentException e) {
                throw new InvalidObdRequestException("PID inválido: " + pid);
            }
        }

        if (validPids.isEmpty()) {
            throw new InvalidObdRequestException(
                "Nenhum PID válido foi informado."
            );
        }

        return telemetryCollector.collect(validPids);
    }

    @Override 
    public ObdReading getPidData(String name) throws IOException {
        try {
            ObdPid obdPid = ObdPid.fromString(name);
            List<ObdReading> readings = telemetryCollector.collect(List.of(obdPid));
            return readings.getFirst();
        } catch (IllegalArgumentException e) {
            throw new InvalidObdRequestException("PID inválido: " + name);
        }
    }
}