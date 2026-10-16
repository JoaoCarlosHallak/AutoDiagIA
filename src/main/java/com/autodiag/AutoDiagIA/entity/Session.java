package com.autodiag.AutoDiagIA.entity;

import java.time.Instant;

import com.autodiag.session.AnalysisMode;
import com.autodiag.session.SessionEndReason;
import com.autodiag.session.SessionStatus;
import com.autodiag.session.TriggerConfig;
import com.autodiag.session.TriggerOperator;

import jakarta.annotation.Generated;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "sessions")
@Data 
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleId;

    private String reason;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Enumerated(EnumType.STRING)
    private AnalysisMode analysisMode;

    private long intervalMs;

    private long maxDurationMs;

    private String triggerPid;

    @Enumerated(EnumType.STRING)
    private TriggerOperator triggerOperator;

    private Double triggerValue;

    private Integer triggerValueIndex;

    private Instant createdAt;

    private Instant startedAt;

    private Instant finishedAt;

    @Enumerated(EnumType.STRING)
    private SessionEndReason endReason;





}
