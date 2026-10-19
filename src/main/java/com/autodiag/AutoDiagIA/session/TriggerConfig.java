package com.autodiag.AutoDiagIA.session;

public record TriggerConfig(
        String pid,
        Integer valueIndex,
        TriggerOperator operator,
        Double triggerValue
) {}