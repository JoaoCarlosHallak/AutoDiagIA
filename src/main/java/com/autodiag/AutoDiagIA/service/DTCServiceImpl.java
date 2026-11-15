package com.autodiag.AutoDiagIA.service;


import com.autodiag.AutoDiagIA.obd.DTCReading;
import com.autodiag.AutoDiagIA.obd.DTCResponseParser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DTCServiceImpl implements DTCService {



    @Override
    public DTCReading getDTCbyMode(String mode) {

        if (!(List.of("03", "07", "0A").contains(mode))) {
            throw new IllegalArgumentException("Invalid mode");
        }





        //DTCResponseParser.parse()




        return null;
    }
}
