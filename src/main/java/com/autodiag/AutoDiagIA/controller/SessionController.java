package com.autodiag.AutoDiagIA.controller;

import com.autodiag.AutoDiagIA.dto.CreateSessionRequest;
import com.autodiag.AutoDiagIA.dto.SessionDTO;
import com.autodiag.AutoDiagIA.service.SessionService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/session")
public class SessionController {

    private final SessionService sessionService;


    @Autowired
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<SessionDTO> createSession(@RequestBody CreateSessionRequest request) {
        return new ResponseEntity<>(sessionService.create(request), HttpStatus.CREATED);

    }

    @PostMapping("/start/{id}")
    public ResponseEntity<SessionDTO> startSession(@PathVariable Long id) {
        return new ResponseEntity<>(sessionService.start(id), HttpStatus.CREATED);
    }

    @PostMapping("/stop/{id}")
    public ResponseEntity<SessionDTO> stopSession(@PathVariable Long id) {
        return new ResponseEntity<>(sessionService.stop(id), HttpStatus.CREATED);
    }



}
