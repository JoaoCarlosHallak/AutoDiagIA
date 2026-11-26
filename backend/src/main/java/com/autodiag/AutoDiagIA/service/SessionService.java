package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.dto.session.CreateSessionRequest;
import com.autodiag.AutoDiagIA.dto.session.SessionDTO;

import java.util.List;

public interface SessionService {

    SessionDTO create(CreateSessionRequest request);
    SessionDTO start(Long id);
    SessionDTO stop(Long id);
    List<SessionDTO> findAll();
    SessionDTO findById(Long id);
    void delete(Long id);
}
