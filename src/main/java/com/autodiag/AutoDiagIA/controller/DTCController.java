package com.autodiag.AutoDiagIA.controller;


import com.autodiag.AutoDiagIA.obd.DTCReading;
import com.autodiag.AutoDiagIA.service.DTCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dtc")
public class DTCController {


    private final DTCService dtcService;

    @Autowired
    public DTCController(DTCService dtcService){
        this.dtcService = dtcService;
    }


    @GetMapping("/{mode}")
    public ResponseEntity<DTCReading> getDTC(@PathVariable String mode) {
        return new ResponseEntity<>(dtcService.getDTCbyMode(mode), HttpStatus.OK);
    }









}
