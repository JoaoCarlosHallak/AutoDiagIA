package com.autodiag.session;

public record TriggerConfig(
        String pid,
        int valueIndex,
        TriggerOperator operator,
        double triggerValue
) {}