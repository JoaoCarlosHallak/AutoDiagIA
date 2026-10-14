package com.autodiag.AutoDiagIA.communication;
import com.autodiag.AutoDiagIA.exception.Elm327CommunicationException;
import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

@Component 
public class Elm327Client {


    private final SerialPort serialPort;
    private final OutputStream out;
    private final InputStream in;


    public Elm327Client(SerialPort serialPort) throws IOException {
       this.serialPort = serialPort;
       this.out = serialPort.getOutputStream();
       this.in = serialPort.getInputStream();
       Elm327StartConfig();
    }

    private void Elm327StartConfig() throws IOException{
        try {
            String response = this.sendCommand("ATE0"); // sem echo
            if (!response.contains("OK")) {
                throw new IOException("Falha ao desativar o echo: " + response);
            }
            System.out.println("Echo desativado com sucesso");
        } catch (IOException e) {
            throw new Elm327CommunicationException("Erro ao configurar o ELM327: " + e.getMessage(), e);
        }
    }


    public String sendCommand(String command) throws IOException {
        try {

        out.write((command + "\r").getBytes(StandardCharsets.US_ASCII));
        out.flush();


        StringBuilder builder = new StringBuilder();

        long timeout = System.currentTimeMillis() + 3000;

        while (System.currentTimeMillis() < timeout) {

            if (in.available() > 0) {

                int data = in.read();

                if (data == '>') {
                    break;
                }

                builder.append((char) data);
            }


        }

        return builder.toString().trim();
    } catch (IOException e) {
            throw new Elm327CommunicationException("Erro na comunicação com o ELM327: " + e.getMessage(), e);
    }
}
}


