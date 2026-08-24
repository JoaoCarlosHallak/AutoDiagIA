package com.autodiag.AutoDiagIA.communication;

import com.fazecast.jSerialComm.SerialPort;



public class SerialPortManager {

    public static SerialPort[] getCommPorts() {
        return SerialPort.getCommPorts();
    }

    public static SerialPort getCommPort(String portName) {
        return SerialPort.getCommPort(portName);
    }



    public static void openAndSetCommPort(SerialPort port, int baudRate, int numDataBits, int numStopBits, int parity) {
        port.setBaudRate(baudRate);
        port.setNumDataBits(numDataBits);
        port.setNumStopBits(numStopBits);
        port.setParity(parity);

        if (!port.openPort()) {
            System.out.println("Could not open port for COM1");
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











