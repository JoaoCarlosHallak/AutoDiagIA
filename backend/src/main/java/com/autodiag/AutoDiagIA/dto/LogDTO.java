package com.autodiag.AutoDiagIA.dto;

import java.time.Instant;

public class LogDTO {
        private Long id;
        private Long sessionId;
        private String pid;
        private String valueName;
        private Double value;
        private String unit;
        private Instant timestamp;

        public LogDTO() {}

    public LogDTO(Long id, Long sessionId, String pid, String valueName, Double value, String unit, Instant timestamp) {
        this.id = id;
        this.sessionId = sessionId;
        this.pid = pid;
        this.valueName = valueName;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}