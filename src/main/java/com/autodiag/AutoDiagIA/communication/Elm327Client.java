package com.autodiag.AutoDiagIA.communication;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Elm327Client {


    private final SerialPort serialPort;
    private final OutputStream out;
    private final InputStream in;


    public Elm327Client(SerialPort serialPort) {
       this.serialPort = serialPort;
       this.out = serialPort.getOutputStream();
       this.in = serialPort.getInputStream();
    }


    public String sendCommand(String command) throws IOException {

        out.write((command + "\r").getBytes(StandardCharsets.US_ASCII));
        out.flush();

        byte[] buffer = new byte[1024];
        int bytesRead = in.read(buffer);

        return new String(
            buffer,
            0,
            bytesRead,
            StandardCharsets.US_ASCII
        );
    }


    public static void showElm327Commands() {

        System.out.println("""
            
            ===== COMANDOS ELM327 =====
            
            [1]  ATZ    - Reset
            [2]  ATI    - Identificação do ELM327
            [3]  AT@1   - Descrição do dispositivo
            [4]  ATE0   - Echo OFF
            [5]  ATE1   - Echo ON
            [6]  ATL0   - Linefeeds OFF
            [7]  ATL1   - Linefeeds ON
            [8]  ATS0   - Spaces OFF
            [9]  ATS1   - Spaces ON
            [10] ATH0   - Headers OFF
            [11] ATH1   - Headers ON
            
            ===== PROTOCOLO =====
            
            [12] ATSP0 - Seleção automática de protocolo
            [13] ATSP1 - SAE J1850 PWM
            [14] ATSP2 - SAE J1850 VPW
            [15] ATSP3 - ISO 9141-2
            [16] ATSP4 - ISO 14230-4 KWP (5 baud init)
            [17] ATSP5 - ISO 14230-4 KWP (fast init)
            [18] ATSP6 - ISO 15765-4 CAN 11-bit / 500 kbit
            [19] ATSP7 - ISO 15765-4 CAN 29-bit / 500 kbit
            [20] ATSP8 - ISO 15765-4 CAN 11-bit / 250 kbit
            [21] ATSP9 - ISO 15765-4 CAN 29-bit / 250 kbit
            
            [22] ATDP   - Descrever protocolo atual
            [23] ATDPN  - Descrever protocolo atual por número
            
            ===== CONFIGURAÇÃO CAN =====
            
            [24] ATPP   - Parâmetros programáveis
            [25] ATPC   - CAN monitor
            [26] ATCRA  - CAN Receive Address
            [27] ATCFC1  - CAN Flow Control ON
            [28] ATCFC0  - CAN Flow Control OFF
            
            ===== MEMÓRIA / CONFIGURAÇÃO =====
            
            [29] ATWS   - Warm Start
            [30] ATSW   - Set Wakeup
            [31] ATWM   - Set Wakeup Message
            [32] ATWM  - Wakeup Message
            
            ===== INFORMAÇÕES =====
            
            [33] ATI    - Identificação
            [34] AT@1   - Descrição do dispositivo
            [35] AT@2   - Identificador do dispositivo
            [36] ATRV   - Tensão da bateria
            
            [0] SAIR
            
            """);
}


    public void startCommunicationConsole() throws IOException {

        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.print(">> ");

            String command = scanner.nextLine();

            if (command.equals("exit")) {
                break;
            }

            if (command.equals("help")) {
                showElm327Commands();
                continue;
            }

            String response = sendCommand(command);

            System.out.println("<< " + response);
        }

        scanner.close();
    }

}

