package com.autodiag.AutoDiagIA.obd;

public abstract class ObdCommand {

    public static String commandToMode01(ObdPid obdPid) {
        return "01" + obdPid.getCode();
    }
    public static String commandToMode03(){
        return "03";
    }

    public static String commandToMode07(){
        return "07";
    }

    public static String commandToMode0A(){
        return "0A";
    }

    public static String commandToMode04() {
        return "04";
    }
}
