package com.autodiag.AutoDiagIA.entity;

import java.time.Instant;

import com.autodiag.session.AnalysisMode;
import com.autodiag.session.SessionEndReason;
import com.autodiag.session.SessionStatus;
import com.autodiag.session.TriggerOperator;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;



@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicle;

    private String startReason;

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




    public Session(Long id, String vehicle, String startReason, SessionStatus status, AnalysisMode analysisMode, long intervalMs, long maxDurationMs, String triggerPid, TriggerOperator triggerOperator, Double triggerValue, Integer triggerValueIndex, Instant createdAt, Instant startedAt, Instant finishedAt, SessionEndReason endReason) {
        this.id = id;
        this.vehicle = vehicle;
        this.startReason = startReason;
        this.status = status;
        this.analysisMode = analysisMode;
        this.intervalMs = intervalMs;
        this.maxDurationMs = maxDurationMs;
        this.triggerPid = triggerPid;
        this.triggerOperator = triggerOperator;
        this.triggerValue = triggerValue;
        this.triggerValueIndex = triggerValueIndex;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.endReason = endReason;
    }


    public Session() {
    }






    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVehicle() {
        return this.vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }


    public String getStartReason() {
        return this.startReason;
    }

    public void setStartReason(String startReason) {
        this.startReason = startReason;
    }


    public SessionStatus getStatus() {
        return this.status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public AnalysisMode getAnalysisMode() {
        return this.analysisMode;
    }

    public void setAnalysisMode(AnalysisMode analysisMode) {
        this.analysisMode = analysisMode;
    }

    public long getIntervalMs() {
        return this.intervalMs;
    }

    public void setIntervalMs(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    public long getMaxDurationMs() {
        return this.maxDurationMs;
    }

    public void setMaxDurationMs(long maxDurationMs) {
        this.maxDurationMs = maxDurationMs;
    }

    public String getTriggerPid() {
        return this.triggerPid;
    }

    public void setTriggerPid(String triggerPid) {
        this.triggerPid = triggerPid;
    }

    public TriggerOperator getTriggerOperator() {
        return this.triggerOperator;
    }

    public void setTriggerOperator(TriggerOperator triggerOperator) {
        this.triggerOperator = triggerOperator;
    }

    public Double getTriggerValue() {
        return this.triggerValue;
    }

    public void setTriggerValue(Double triggerValue) {
        this.triggerValue = triggerValue;
    }

    public Integer getTriggerValueIndex() {
        return this.triggerValueIndex;
    }

    public void setTriggerValueIndex(Integer triggerValueIndex) {
        this.triggerValueIndex = triggerValueIndex;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStartedAt() {
        return this.startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return this.finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public SessionEndReason getEndReason() {
        return this.endReason;
    }

    public void setEndReason(SessionEndReason endReason) {
        this.endReason = endReason;
    }




}
