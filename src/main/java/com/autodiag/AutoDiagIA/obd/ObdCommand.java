package com.autodiag.AutoDiagIA.obd;

public record ObdCommand(ObdPid obdPid) {
    public String toCommand() {
        return "01" + " " + obdPid.getCode();
    }
}
