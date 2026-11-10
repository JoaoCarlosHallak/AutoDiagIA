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

    private void Elm327StartConfig() throws IOException {
        try {
            String reset = sendCommand("ATZ", 5000);
            System.out.println("Reset: " + reset.replace("\r", "\\r"));

            String echo = sendCommand("ATE0");
            if (!echo.contains("OK")) {
                throw new IOException("Falha ao desativar o echo: " + echo);
            }

            String sp = sendCommand("ATSP0");
            if (!sp.contains("OK")) {
                throw new IOException("Falha ao ativar protocolo automático: " + sp);
            }

            // força a busca de protocolo agora
            String probe = sendCommand("0100", 15000);

            System.out.println("Protocolo determinado: " + sendCommand("ATDP"));
        } catch (IOException e) {
            throw new Elm327CommunicationException("Erro ao configurar o ELM327: " + e.getMessage(), e);
        }
    }

    public synchronized String sendCommand(String command) throws IOException {
        return sendCommand(command, 3000);
    }

    public synchronized String sendCommand(String command, long timeoutMs) throws IOException {
        while (in.available() > 0) in.read();

        out.write((command + "\r").getBytes(StandardCharsets.US_ASCII));
        out.flush();

        StringBuilder sb = new StringBuilder();
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            if (in.available() > 0) {
                int b = in.read();
                if (b == -1) throw new IOException("Porta serial fechada");
                if (b == '>') return sb.toString().trim();
                sb.append((char) b);
            } else {
                try { Thread.sleep(5); }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Leitura interrompida", e);
                }
            }
        }
        throw new IOException("Timeout aguardando '>' para " + command + ". Parcial: " + sb);
    }
}


