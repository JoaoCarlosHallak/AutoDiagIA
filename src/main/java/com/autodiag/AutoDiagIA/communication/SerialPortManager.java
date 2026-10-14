package com.autodiag.AutoDiagIA.communication;

import com.fazecast.jSerialComm.SerialPort;



public class SerialPortManager {

    public static SerialPort[] getCommPorts() {
        return SerialPort.getCommPorts();
    }

    public static SerialPort getCommPort(String portName) {
        return SerialPort.getCommPort(portName);
    }

//"Continue lendo os bytes até o ELM327 mandar >."
//
//O > é o prompt do ELM327, indicando que terminou aquela resposta.

    public static void openAndSetCommPort(SerialPort port, int baudRate, int numDataBits, int numStopBits, int parity) {
        port.setBaudRate(baudRate);
        port.setNumDataBits(numDataBits);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 3000, 0); // 3 SEGUNDOS DE TIMEOUT
        port.setNumStopBits(numStopBits);
        port.setParity(parity);

        if (!port.openPort()) {
            System.out.println("Could not open port for COM4");
        }

        System.out.println("Opened port for " + port + " with baud rate " + port.getBaudRate() + " and parity " + port.getParity());

    }

    public static void closeCommPort(SerialPort port) {
        if (port.closePort()) {
            System.out.println("Closed port for " + port);
            return;
        }
        System.out.println("Error closing port for " + port);
    }







}








