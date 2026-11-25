package com.autodiag.AutoDiagIA.entity;

import com.autodiag.AutoDiagIA.session.TriggerOperator;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "session_templates")
public class SessionTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 1000)
    private String startReason;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "session_template_pids",
            joinColumns = @JoinColumn(name = "template_id")
    )
    @OrderColumn(name = "pid_order")
    @Column(name = "pid", nullable = false, length = 2)
    private List<String> pids = new ArrayList<>();

    @Column(nullable = false)
    private boolean includeSessionSettings;

    private Long intervalMs;

    private Long maxDurationMs;

    private String triggerPid;

    @Enumerated(EnumType.STRING)
    private TriggerOperator triggerOperator;

    private Double triggerValue;

    private Integer triggerValueIndex;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartReason() {
        return startReason;
    }

    public void setStartReason(String startReason) {
        this.startReason = startReason;
    }

    public List<String> getPids() {
        return pids;
    }

    public void setPids(List<String> pids) {
        this.pids = pids;
    }

    public boolean isIncludeSessionSettings() {
        return includeSessionSettings;
    }

    public void setIncludeSessionSettings(boolean includeSessionSettings) {
        this.includeSessionSettings = includeSessionSettings;
    }

    public Long getIntervalMs() {
        return intervalMs;
    }

    public void setIntervalMs(Long intervalMs) {
        this.intervalMs = intervalMs;
    }

    public Long getMaxDurationMs() {
        return maxDurationMs;
    }

    public void setMaxDurationMs(Long maxDurationMs) {
        this.maxDurationMs = maxDurationMs;
    }

    public String getTriggerPid() {
        return triggerPid;
    }

    public void setTriggerPid(String triggerPid) {
        this.triggerPid = triggerPid;
    }

    public TriggerOperator getTriggerOperator() {
        return triggerOperator;
    }

    public void setTriggerOperator(TriggerOperator triggerOperator) {
        this.triggerOperator = triggerOperator;
    }

    public Double getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(Double triggerValue) {
        this.triggerValue = triggerValue;
    }

    public Integer getTriggerValueIndex() {
        return triggerValueIndex;
    }

    public void setTriggerValueIndex(Integer triggerValueIndex) {
        this.triggerValueIndex = triggerValueIndex;
    }
}
