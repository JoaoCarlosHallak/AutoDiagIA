package com.autodiag.AutoDiagIA.obd;


public record ObdValue(
        String name,
        Double value,
        String unit
) {
}