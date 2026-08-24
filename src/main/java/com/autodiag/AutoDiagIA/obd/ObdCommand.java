package com.autodiag.AutoDiagIA.obd;

public record ObdCommand(ObdMode obdMode, ObdPid obdPid) {
    public String toCommand() {
        return obdMode.getCode() + " " + obdPid.getCode();
    }
}
