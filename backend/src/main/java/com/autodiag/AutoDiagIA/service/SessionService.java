package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.dto.CreateSessionRequest;
import com.autodiag.AutoDiagIA.dto.SessionDTO;
import com.autodiag.AutoDiagIA.entity.Session;

import java.util.List;

public interface SessionService {

    SessionDTO create(CreateSessionRequest request);
    SessionDTO start(Long id);
    SessionDTO stop(Long id);
    List<SessionDTO> findAll();
    SessionDTO findById(Long id);
}
