package com.autodiag.AutoDiagIA.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.autodiag.AutoDiagIA.communication.Elm327Client;
import com.autodiag.AutoDiagIA.communication.SerialPortManager;
import com.fazecast.jSerialComm.SerialPort;

@Configuration 
public class CommunicationConfig {


    @Bean(destroyMethod = "closePort")
    public SerialPort serialPort() throws IOException {
        SerialPort port = SerialPort.getCommPort("COM4");

        SerialPortManager.openAndSetCommPort(
                port,
                38400,
                8,
                SerialPort.ONE_STOP_BIT,
                SerialPort.NO_PARITY
        );

        return port;
    }


}