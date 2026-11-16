package com.autodiag.AutoDiagIA.obd;

import com.autodiag.AutoDiagIA.communication.Elm327Client;
import com.autodiag.AutoDiagIA.exception.ObdResponseException;
import java.io.IOException;


import org.springframework.stereotype.Component;


@Component 
public class ObdClient {

    private final Elm327Client elm327Client;

    public ObdClient(Elm327Client elm327Client) {
        this.elm327Client = elm327Client;

    }

    public ObdReading readPid(ObdPid pid) throws IOException {

        String command = ObdCommand.commandToMode01(pid);

        String response = elm327Client.sendCommand(command);
        System.out.println("Comando enviado: " + command);
        System.out.println("Resposta recebida: [" + response + "]");

        try {
            ObdReading reading =
                ObdResponseParser.parse(response, pid);
            return reading;
        } catch (IllegalArgumentException e) {
            throw new ObdResponseException("Falha ao ler o PID " + pid.getCode(), e);
        }
    }



    public DTCReading readDTC(String mode) throws IOException {

        switch (mode) {
            case "confirmado" -> mode = DTCMode.CONFIRMED_DTC.getCode();
            case "pendente" -> mode = DTCMode.PENDENT_DTC.getCode();
            case "permanente" -> mode = DTCMode.PERMANENT_DTC.getCode();
        }

        String response = elm327Client.sendCommand(mode);
        System.out.println("Comando enviado: " + mode);
        System.out.println("Resposta recebida: [" + response + "]");

        try {
            return DTCResponseParser.parse(response, mode);
        } catch (IllegalArgumentException e) {
            throw new ObdResponseException("Falha ao ler DTC: " + mode);
        }
    }



}




