package com.autodiag.AutoDiagIA;

import com.autodiag.AutoDiagIA.communication.Elm327Client;
import com.autodiag.AutoDiagIA.communication.SerialPortManager;
import com.fazecast.jSerialComm.SerialPort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
public class AutoDiagIaApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(AutoDiagIaApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

    SerialPort port = SerialPortManager.getCommPort("COM1");

    SerialPortManager.openAndSetCommPort(
            port,
            38400,
            8,
            SerialPort.ONE_STOP_BIT,
            SerialPort.NO_PARITY
    );

    Elm327Client elm327Client = new Elm327Client(port);

    elm327Client.startCommunicationConsole();

    SerialPortManager.closeCommPort(port);
	}
}

