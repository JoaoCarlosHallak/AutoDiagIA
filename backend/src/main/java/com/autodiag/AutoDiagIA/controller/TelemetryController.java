package com.autodiag.AutoDiagIA.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.autodiag.AutoDiagIA.dto.CollectionRequest;
import com.autodiag.AutoDiagIA.dto.ObdResponse;
import com.autodiag.AutoDiagIA.service.TelemetryService;
import com.autodiag.AutoDiagIA.telemetry.TelemetryScheduler;


@RestController 
@RequestMapping("/telemetry")
public class TelemetryController {


    private final TelemetryService telemetryService;
    private final TelemetryScheduler telemetryScheduler;

    @Autowired 
    public TelemetryController(TelemetryService telemetryService, TelemetryScheduler telemetryScheduler) {
        this.telemetryService = telemetryService;
        this.telemetryScheduler = telemetryScheduler;
    }





    @PostMapping("/pid")
    public ResponseEntity<List<ObdResponse>> getPidsData(@RequestBody List<String> pids) {
        return new ResponseEntity<>(telemetryService.getPidsData(pids), HttpStatus.OK);
    }

    @GetMapping("/pid")
    public ResponseEntity<ObdResponse> getPidData(@RequestParam String name) {
        return new ResponseEntity<>(telemetryService.getPidData(name), HttpStatus.OK);
    }
    
    
    @PostMapping("/collection/start")
    public ResponseEntity<String> startCollection(@RequestBody CollectionRequest request) {
        telemetryScheduler.start(request.pids(), request.intervalMs());
        return new ResponseEntity<>("Collection started", HttpStatus.OK);
    }

    @PostMapping("/collection/stop")
    public ResponseEntity<String> stopCollection() {
        telemetryScheduler.stop();
        return new ResponseEntity<>("Collection stopped", HttpStatus.OK);
    }

    @GetMapping("/collection/status")
    public ResponseEntity<Boolean> collectionStatus() {
        return new ResponseEntity<>(telemetryScheduler.isRunning(),HttpStatus.OK);
    }



}
