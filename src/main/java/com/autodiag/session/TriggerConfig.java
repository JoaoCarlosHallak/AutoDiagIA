package com.autodiag.session;

public record TriggerConfig(
        String pid,
        Integer valueIndex,
        TriggerOperator operator,
        Double triggerValue
) {}