package com.autodiag.AutoDiagIA.controller;

import com.autodiag.AutoDiagIA.dto.SessionTemplateDTO;
import com.autodiag.AutoDiagIA.dto.SessionTemplateRequest;
import com.autodiag.AutoDiagIA.service.SessionTemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/session/templates")
public class SessionTemplateController {

    private final SessionTemplateService sessionTemplateService;

    public SessionTemplateController(SessionTemplateService sessionTemplateService) {
        this.sessionTemplateService = sessionTemplateService;
    }

    @GetMapping
    public ResponseEntity<List<SessionTemplateDTO>> getAll() {
        return ResponseEntity.ok(sessionTemplateService.findAll());
    }

    @PostMapping
    public ResponseEntity<SessionTemplateDTO> create(
            @RequestBody SessionTemplateRequest request
    ) {
        return new ResponseEntity<>(
                sessionTemplateService.create(request),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionTemplateDTO> update(
            @PathVariable Long id,
            @RequestBody SessionTemplateRequest request
    ) {
        return ResponseEntity.ok(sessionTemplateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sessionTemplateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
