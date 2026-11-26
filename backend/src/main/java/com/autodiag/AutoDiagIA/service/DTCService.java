package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.obd.DTCReading;

public interface DTCService {

    DTCReading getDTCbyMode(String mode);
    Void clearDtc();

}
