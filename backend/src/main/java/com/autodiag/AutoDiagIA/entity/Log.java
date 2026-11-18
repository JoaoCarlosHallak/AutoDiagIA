package com.autodiag.AutoDiagIA.entity;


import java.time.Instant;

import jakarta.persistence.*;


@Entity
@Table(
    name = "logs",
    indexes = {
        @Index(
            name = "idx_logs_timestamp",
            columnList = "session_id, timestamp"
        )
    }
)
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    private String pid;

    private String valueName;

    private Double value;

    private String unit;

    private Instant timestamp;

    public Log(Long id, Session session, String pid, String valueName, Double value, String unit, Instant timestamp) {
        this.id = id;
        this.session = session;
        this.pid = pid;
        this.valueName = valueName;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
    }

    public Log() {}
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
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