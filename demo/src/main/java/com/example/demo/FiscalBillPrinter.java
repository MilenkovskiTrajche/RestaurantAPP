package com.example.demo;

import com.fazecast.jSerialComm.SerialPort;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

public class FiscalBillPrinter {

    private static FiscalBillPrinter instance; // Singleton instance
    private SerialPort serialPort;
    private PrintWriter writer;

    // Private constructor for Singleton
    private FiscalBillPrinter() {
    }

    // Get the single instance of the FiscalPrinter
    public static synchronized FiscalBillPrinter getInstance() {
        if (instance == null) {
            instance = new FiscalBillPrinter();
        }
        return instance;
    }

    // Connect to the fiscal printer
    public synchronized void connectToPrinter(String portName) {
        try {
            serialPort = SerialPort.getCommPort(portName);

            // Use device-specific RS232 settings
            serialPort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
            serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

            if (serialPort.openPort()) {
                OutputStream outputStream = serialPort.getOutputStream();
                writer = new PrintWriter(outputStream, true);
                System.out.println("Connected to Fiscal Printer on " + portName);
            } else {
                System.err.println("Failed to open the port: " + portName);
                writer = null; // Ensure writer is null if the port is not opened
            }
        } catch (Exception e) {
            System.err.println("Failed to connect to the fiscal printer: " + e.getMessage());
            writer = null; // Ensure writer is null on error
        }
    }

    private synchronized void sendCommand(String command) {
        if (writer != null) {
            try {
                // Convert command to CP866 encoding
                byte[] commandBytes = command.getBytes("CP866");
                serialPort.getOutputStream().write(commandBytes);
                serialPort.getOutputStream().flush();

                System.out.println("Command sent: " + command);

                // Read the response from the printer
                Thread.sleep(500); // Allow time for a response
                byte[] responseBuffer = new byte[serialPort.bytesAvailable()];
                int bytesRead = serialPort.getInputStream().read(responseBuffer);

                if (bytesRead > 0) {
                    String response = new String(responseBuffer, 0, bytesRead, "CP866");
                    System.out.println("Printer Response: " + response);
                } else {
                    System.out.println("No response from printer.");
                }
            } catch (Exception e) {
                System.err.println("Error sending command: " + e.getMessage());
            }
        } else {
            System.err.println("Writer is not initialized. Cannot send command.");
        }
    }





    // Print a daily report
    public synchronized void printDailyReport() {
        try {
            sendCommand("PRINT DAILY REPORT");
            System.out.println("Daily report command sent.");
        } catch (Exception e) {
            System.err.println("Error while printing daily report: " + e.getMessage());
        }
    }

    // Print a fiscal receipt for multiple articles
    public synchronized void printReceipt(String date, List<Article> articles) {
        try {
            // Set the date
            sendCommand("SET DATE=" + date);

            // Register each article
            for (Article article : articles) {
                String registerCommand = String.format(
                        "REGISTER ITEM=\"%s\" QTY=%d PRICE=%.2f TAX=%s",
                        article.getName(),
                        article.getQuantity(),
                        article.getPrice(),
                        article.getTaxGroup()
                );
                sendCommand(registerCommand);
            }

            // Finalize and print the receipt
            sendCommand("PRINT RECEIPT");
            System.out.println("Receipt printed successfully.");
        } catch (Exception e) {
            System.err.println("Error while printing receipt: " + e.getMessage());
        }
    }

    // Close the connection
    public synchronized void closeConnection() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("Connection to printer closed.");
        }
    }

    // Article class
    public static class Article {
        private String name;
        private int quantity;
        private double price;
        private String taxGroup;

        public Article(String name, int quantity, double price, String taxGroup) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
            this.taxGroup = taxGroup;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }

        public String getTaxGroup() {
            return taxGroup;
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        FiscalBillPrinter printerService = FiscalBillPrinter.getInstance();
        printerService.connectToPrinter("COM3"); // Adjust the port name as needed

        //printerService.sendCommand("SET DATE=19-02-2025");
        printerService.sendCommand("REGISTER ITEM=\"Koka Kola\" QTY=2 PRICE=100.00 TAX=A");
        printerService.sendCommand("PRINT RECEIPT");


        //printerService.closeConnection();
    }

}
