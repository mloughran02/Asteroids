package com.micheal.asteroids;
import com.fazecast.jSerialComm.SerialPort;
import java.io.InputStream;
import java.util.Scanner;

public class MPU6050Reader {
    private SerialPort serialPort;
    private volatile float roll = 0;
    private volatile float pitch = 0;
    private volatile float yaw = 0;
    private Thread readerThread;
    private boolean running = false;

    public MPU6050Reader(String portName, int baudRate) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);

        if (!serialPort.openPort()) {
            throw new RuntimeException("Failed to open port " + portName);
        }
        System.out.println("Serial port opened: " + portName);

        running = true;
        readerThread = new Thread(this::readSerial);
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void readSerial() {
        try (InputStream in = serialPort.getInputStream();
             Scanner scanner = new Scanner(in)) {

            while (running && scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                parseLine(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseLine(String line) {
        String[] parts = line.split("/");
        if (parts.length == 3) {
            try {
                roll = Float.parseFloat(parts[0]);
                pitch = Float.parseFloat(parts[1]);
                yaw = Float.parseFloat(parts[2]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid data format: " + line);
            }
        } else {
            System.err.println("Unexpected line format: " + line);
        }
    }

    // Accessors
    public float getRoll() {
        return roll;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    // Close serial port cleanly
    public void close() {
        running = false;
        if (readerThread != null && readerThread.isAlive()) {
            try {
                readerThread.join();
            } catch (InterruptedException ignored) {}
        }
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("Serial port closed.");
        }
    }
}
