package com.autodiag.AutoDiagIA;

import com.autodiag.AutoDiagIA.communication.Elm327Client;
import com.autodiag.AutoDiagIA.communication.SerialPortManager;
import com.autodiag.AutoDiagIA.obd.ObdClient;
import com.autodiag.AutoDiagIA.obd.ObdPid;
import com.autodiag.AutoDiagIA.obd.ObdReading;
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

    SerialPort port = SerialPortManager.getCommPort("COM1");

    SerialPortManager.openAndSetCommPort(
            port,
            38400,
            8,
            SerialPort.ONE_STOP_BIT,
            SerialPort.NO_PARITY
    );

    Elm327Client elm327Client = new Elm327Client(port);

        ObdClient obdClient = new ObdClient(elm327Client);

        List<ObdPid> pids = List.of(
        ObdPid.ENGINE_RPM,
        ObdPid.VEHICLE_SPEED,
        ObdPid.ENGINE_COOLANT_TEMPERATURE
);

    TelemetryCollector collector =
            new TelemetryCollector(obdClient);

    List<ObdReading> readings =
            collector.collect(pids);

    for (ObdReading reading : readings) {
        System.out.println(reading);
    }




    SerialPortManager.closeCommPort(port);
	}
}

