package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.dto.session.SessionTemplateDTO;
import com.autodiag.AutoDiagIA.dto.session.SessionTemplateRequest;

import java.util.List;

public interface SessionTemplateService {
    List<SessionTemplateDTO> findAll();
    SessionTemplateDTO create(SessionTemplateRequest request);
    SessionTemplateDTO update(Long id, SessionTemplateRequest request);
    void delete(Long id);
}
