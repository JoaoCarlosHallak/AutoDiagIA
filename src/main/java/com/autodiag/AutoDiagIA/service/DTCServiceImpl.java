package com.autodiag.AutoDiagIA.service;


import com.autodiag.AutoDiagIA.obd.DTCReading;
import com.autodiag.AutoDiagIA.obd.DTCResponseParser;
import com.autodiag.AutoDiagIA.obd.ObdClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DTCServiceImpl implements DTCService {

    private final ObdClient obdClient;

    @Autowired
    public DTCServiceImpl(ObdClient obdClient) {
        this.obdClient = obdClient;
    }



    @Override
    public DTCReading getDTCbyMode(String mode) {
        mode = mode.toLowerCase();

        if (!(List.of("confirmado", "pendente", "permanente").contains(mode))) {
            throw new IllegalArgumentException("Invalid mode");
        }
        return obdClient.readDTC(mode);


    }
}
