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
        System.out.println(this.sendCommand("ATZ") + " resetado: ");
        try {
            String echoResponse = this.sendCommand("ATE0");
            String automaticProtocolResponse = this.sendCommand("ATSP0");

            // sem echo
            if (!echoResponse.contains("OK")){
                throw new IOException("Falha ao desativar o echo: " + echoResponse);
            }
            if (!automaticProtocolResponse.contains("OK")){
                throw new IOException("Falha ao desativar o automatico: " + automaticProtocolResponse);
            }
            System.out.println("Echo desativado com sucesso | Elm327 em modo automatico de protocolo");
            System.out.println("Protocolo determinado: " + this.sendCommand("ATDP"));
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


