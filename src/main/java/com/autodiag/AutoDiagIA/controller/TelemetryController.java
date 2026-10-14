package com.autodiag.AutoDiagIA.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.autodiag.AutoDiagIA.obd.ObdReading;
import com.autodiag.AutoDiagIA.service.TelemetryService;


@RestController 
@RequestMapping("/telemetry")
public class TelemetryController {


    private final TelemetryService telemetryService;

    @Autowired 
    public TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }





    @PostMapping("/pid")
    public ResponseEntity<List<ObdReading>> getPidsData(@RequestBody List<String> pids) throws IOException {
        return new ResponseEntity<>(telemetryService.getPidsData(pids), HttpStatus.OK);
    }

    @GetMapping("/pid")
    public ResponseEntity<ObdReading> getPidData(@RequestParam String name) throws IOException {
        return new ResponseEntity<>(telemetryService.getPidData(name), HttpStatus.OK);
    }
    
    









}
