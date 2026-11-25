package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.dto.SessionTemplateDTO;
import com.autodiag.AutoDiagIA.dto.SessionTemplateRequest;

import java.util.List;

public interface SessionTemplateService {
    List<SessionTemplateDTO> findAll();
    SessionTemplateDTO create(SessionTemplateRequest request);
    SessionTemplateDTO update(Long id, SessionTemplateRequest request);
    void delete(Long id);
}
