package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.obd.DTCReading;

import java.io.IOException;

public interface DTCService {

    DTCReading getDTCbyMode(String mode) throws IOException;

}
