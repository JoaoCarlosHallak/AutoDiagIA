package com.autodiag.AutoDiagIA.telemetry;
import com.autodiag.AutoDiagIA.obd.ObdReading;
import com.autodiag.AutoDiagIA.service.TelemetryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class TelemetryScheduler {

    private final TelemetryService telemetryService;

    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> collectionTask;

    @Autowired 
    public TelemetryScheduler(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    public synchronized void start(List<String> pids,long intervalMs) {

        if (collectionTask != null && !collectionTask.isDone()) {
            throw new IllegalStateException(
                    "Já existe uma coleta ativa."
            );
        }

        if (pids == null || pids.isEmpty()) {
            throw new IllegalArgumentException(
                    "Informe pelo menos um PID."
            );
        }

        if (intervalMs < 1000) {
            throw new IllegalArgumentException(
                    "O intervalo mínimo é de 1000 ms."
            );
        }

        List<String> requestedPids = List.copyOf(pids);

        collectionTask = executor.scheduleAtFixedRate(() -> {
            try {
                List<ObdReading> readings =
                        telemetryService.getPidsData(requestedPids);

                readings.forEach(System.out::println);

            } catch (IOException | RuntimeException e) {
                System.err.println(
                        "Erro na coleta: " + e.getMessage()
                );
            }
        }, 0, intervalMs, TimeUnit.MILLISECONDS);
    }

    public synchronized void stop() {
        if (collectionTask != null) {
            collectionTask.cancel(false);
            collectionTask = null;
        }
    }

    public synchronized boolean isRunning() {
        return collectionTask != null && !collectionTask.isDone();
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}