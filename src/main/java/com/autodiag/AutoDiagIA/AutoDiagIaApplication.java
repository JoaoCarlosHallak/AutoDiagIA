package com.autodiag.AutoDiagIA;

import com.autodiag.AutoDiagIA.communication.Elm327Client;
import com.autodiag.AutoDiagIA.communication.SerialPortManager;
import com.autodiag.AutoDiagIA.obd.ObdClient;
import com.autodiag.AutoDiagIA.obd.ObdPid;
import com.autodiag.AutoDiagIA.obd.ObdReading;
import com.autodiag.AutoDiagIA.obd.ObdResponseParser;
import com.autodiag.AutoDiagIA.telemetry.TelemetryCollector;
import com.fazecast.jSerialComm.SerialPort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class AutoDiagIaApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(AutoDiagIaApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {




	}
}

