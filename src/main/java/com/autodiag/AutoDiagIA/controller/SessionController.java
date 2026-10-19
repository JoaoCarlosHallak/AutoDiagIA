package com.autodiag.AutoDiagIA.controller;

import com.autodiag.AutoDiagIA.dto.CreateSessionRequest;
import com.autodiag.AutoDiagIA.dto.LogDTO;
import com.autodiag.AutoDiagIA.dto.SessionDTO;
import com.autodiag.AutoDiagIA.service.SessionLogService;
import com.autodiag.AutoDiagIA.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;



@RestController
@RequestMapping("/session")
public class SessionController {

    private final SessionService sessionService;
    private final SessionLogService sessionLogService;


    @Autowired
    public SessionController(SessionService sessionService, SessionLogService sessionLogService) {
        this.sessionService = sessionService;
        this.sessionLogService = sessionLogService;
    }

    @PostMapping
    public ResponseEntity<SessionDTO> createSession(@RequestBody CreateSessionRequest request) {
        return new ResponseEntity<>(sessionService.create(request), HttpStatus.CREATED);

    }

    @GetMapping
    public ResponseEntity<List<SessionDTO>> getAllSessions() {
        return new ResponseEntity<>(sessionService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionDTO> getSessionById(@PathVariable  Long id) {
        return new ResponseEntity<>(sessionService.findById(id), HttpStatus.OK);
    }

    @PostMapping("/start/{id}")
    public ResponseEntity<SessionDTO> startSession(@PathVariable Long id) {
        return new ResponseEntity<>(sessionService.start(id), HttpStatus.CREATED);
    }

    @PostMapping("/stop/{id}")
    public ResponseEntity<SessionDTO> stopSession(@PathVariable Long id) {
        return new ResponseEntity<>(sessionService.stop(id), HttpStatus.CREATED);
    }

    @GetMapping("/{sessionId}/logs")
    public ResponseEntity<Page<LogDTO>> getSessionLogs(
            @PathVariable Long sessionId,

            @RequestParam(required = false)
            String pid,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant to,

            @PageableDefault(
                    size = 100,
                    sort = "timestamp",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable){


            return new ResponseEntity<>(sessionLogService.getSessionLogs(sessionId, pid, from, to, pageable), HttpStatus.OK);


    }



}
