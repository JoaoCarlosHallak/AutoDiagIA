package com.autodiag.AutoDiagIA.session;

public class TriggerConfig {
    private String pid;
    private Integer valueIndex;
    private TriggerOperator operator;
    private Double triggerValue;

    public TriggerConfig() {}

    public TriggerConfig(String pid, Integer valueIndex, TriggerOperator operator, Double triggerValue) {
        this.pid = pid;
        this.valueIndex = valueIndex;
        this.operator = operator;
        this.triggerValue = triggerValue;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public Integer getValueIndex() {
        return valueIndex;
    }

    public void setValueIndex(Integer valueIndex) {
        this.valueIndex = valueIndex;
    }

    public TriggerOperator getOperator() {
        return operator;
    }

    public void setOperator(TriggerOperator operator) {
        this.operator = operator;
    }

    public Double getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(Double triggerValue) {
        this.triggerValue = triggerValue;
    }
}