package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.dto.CreateSessionRequest;
import com.autodiag.AutoDiagIA.entity.Session;

public interface SessionService {

    Session create(CreateSessionRequest request);

}
