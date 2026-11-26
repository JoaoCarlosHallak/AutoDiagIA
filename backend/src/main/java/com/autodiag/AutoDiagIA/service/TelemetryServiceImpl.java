package com.autodiag.AutoDiagIA.service;

import java.util.ArrayList;
import java.util.List;

import com.autodiag.AutoDiagIA.exception.InvalidObdRequestException;
import com.autodiag.AutoDiagIA.exception.InvalidRequestException;
import com.autodiag.AutoDiagIA.exception.ObdResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autodiag.AutoDiagIA.obd.ObdPid;
import com.autodiag.AutoDiagIA.dto.ObdResponse;
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
    public List<ObdResponse> getPidsData(List<String> pids)  {
        if (pids == null || pids.isEmpty()) {
            throw new InvalidRequestException("Informe pelo menos um PID.");
        }

        List<ObdPid> validPids = new ArrayList<>();

        if(pids.contains("all")){
            return telemetryCollector.collect(List.of(ObdPid.values()));
        }

        for (String pid : pids) {
            try {
                ObdPid obdPid = ObdPid.fromString(pid);
                validPids.add(obdPid);
            } catch (IllegalArgumentException e) {
                throw new InvalidObdRequestException("PID inválido: " + pid, e);
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
    public ObdResponse getPidData(String name) {
        ObdPid obdPid;
        try {
            obdPid = ObdPid.fromString(name);
        } catch (IllegalArgumentException e) {
            throw new InvalidObdRequestException("PID inválido: " + name, e);
        }

        List<ObdResponse> readings = telemetryCollector.collect(List.of(obdPid));
        if (readings.isEmpty()) {
            throw new ObdResponseException(
                    "Não foi possível obter uma leitura para o PID " + obdPid.getCode() + "."
            );
        }

        return readings.getFirst();
    }
}