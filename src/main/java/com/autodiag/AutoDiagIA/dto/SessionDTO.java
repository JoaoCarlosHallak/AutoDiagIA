package com.autodiag.AutoDiagIA.dto;

import com.autodiag.AutoDiagIA.session.*;
import jakarta.persistence.*;

import java.time.Instant;

public class SessionDTO{
    private Long sessionId;
    private String vehicle;
    private String startReason;
    private SessionStatus status;
    private AnalysisMode analysisMode;
    private long intervalMs;
    private long maxDurationMs;
    private TriggerConfig triggerConfig;
    private Instant createdAt;
    private Instant startedAt;
    private Instant finishedAt;
    private SessionEndReason endReason;


    public SessionDTO(Long sessionId, String vehicle, String startReason, SessionStatus status, AnalysisMode analysisMode, long intervalMs, long maxDurationMs, TriggerConfig triggerConfig, Instant createdAt, Instant startedAt, Instant finishedAt, SessionEndReason endReason) {
        this.vehicle = vehicle;
        this.startReason = startReason;
        this.status = status;
        this.analysisMode = analysisMode;
        this.intervalMs = intervalMs;
        this.maxDurationMs = maxDurationMs;
        this.triggerConfig = triggerConfig;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.endReason = endReason;
    }

    public SessionDTO() {}

    public Long getSessionId() {
        return sessionId;
    }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public String getStartReason() {
        return startReason;
    }

    public void setStartReason(String startReason) {
        this.startReason = startReason;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public AnalysisMode getAnalysisMode() {
        return analysisMode;
    }

    public void setAnalysisMode(AnalysisMode analysisMode) {
        this.analysisMode = analysisMode;
    }

    public long getIntervalMs() {
        return intervalMs;
    }

    public void setIntervalMs(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    public long getMaxDurationMs() {
        return maxDurationMs;
    }

    public void setMaxDurationMs(long maxDurationMs) {
        this.maxDurationMs = maxDurationMs;
    }

    public TriggerConfig getTriggerConfig() {
        return triggerConfig;
    }

    public void setTriggerConfig(TriggerConfig triggerConfig) {
        this.triggerConfig = triggerConfig;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public SessionEndReason getEndReason() {
        return endReason;
    }

    public void setEndReason(SessionEndReason endReason) {
        this.endReason = endReason;
    }
}
