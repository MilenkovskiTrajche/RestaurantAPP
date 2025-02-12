package com.example.demo;
import javafx.collections.ObservableList;
import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class PrinterService {

    private static final byte[] CUT_PAPER = {0x1B, 0x69}; // ESC i for partial cut, ESC m (0x1B, 0x6D) for full cut
    private static final byte[] RESET_PRINTER = {0x1B, 0x40}; // Reset the printer to default settings
    private static final byte[] SET_CYRILLIC_CODE_PAGE = {0x1B, 0x74, 17}; // ESC t 17 for CP1251 (Windows Cyrillic encoding)

    public void printOrder(String employeeName, int tableNumber, ObservableList<String[]> orderData, String title) {
        StringBuilder receipt = new StringBuilder();

        // Add title
        receipt.append(centerText(title, 32)).append("\n");
        receipt.append("ВРАБОТЕН: ").append(employeeName).append(" | МАСА: ").append(tableNumber).append("\n");
        receipt.append("Време: ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy"))).append("\n");
        receipt.append("\n");
        receipt.append("--------------------------------\n");

        for (String[] articleData : orderData) {
            if (articleData.length >= 4) {
                String articleName = articleData[1];
                String quantity = articleData[3];
                // Format article line
                receipt.append("\n");
                receipt.append(formatArticle(articleName, quantity)).append("\n");
                receipt.append("\n");
            }
        }
        receipt.append("--------------------------------\n");
        receipt.append("\n\n");
        // Send the receipt to the printer
        sendToPrinter(receipt.toString());
    }

    public void printEmployeeOverview(String title, double fiscaltotal, double invoicetotal, double karticatotal, LocalDate datefrom, LocalDate dateto, String timefrom, String timeto) {
        StringBuilder receipt = new StringBuilder();

        receipt.append("ПРЕГЛЕД ПО ВРАБОТЕН - ").append(title).append("\n");
        receipt.append("\n");
        receipt.append("Во период").append("\n");
        receipt.append("ОД: ").append(datefrom.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append(" до ").append(dateto.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("\n");
        receipt.append("\n");
        receipt.append("ОД: ").append(timefrom).append(" до ").append(timeto).append("\n");
        receipt.append("\n");
        receipt.append("--------------------------------\n");
        receipt.append("\n");
        receipt.append(formatOverview("Фискален промет:", String.valueOf(fiscaltotal))).append("\n");
        receipt.append("\n");
        receipt.append(formatOverview("Фактура промет:", String.valueOf(invoicetotal))).append("\n");
        receipt.append("\n");
        receipt.append(formatOverview("Картица промет:", String.valueOf(karticatotal))).append("\n");
        receipt.append("\n");
        receipt.append(formatOverview("Вкупен промет:", String.valueOf(fiscaltotal+invoicetotal+karticatotal))).append("\n");
        receipt.append("--------------------------------\n");
        receipt.append("\n");
        receipt.append("\n\n");
        // Send the receipt to the printer
        sendToPrinter(receipt.toString());
    }
    private String formatArticle(String name, String qty) {
        int maxArticleWidth = 25; // Maximum space for article name (adjustable)

        // Shorten long article names
        if (name.length() > maxArticleWidth) {
            name = name.substring(0, maxArticleWidth);
        }

        // Calculate dynamic spacing
        int spacesNeeded = maxArticleWidth - name.length();
        String spacing = " ".repeat(spacesNeeded);

        // Format article line with aligned pricing
        return String.format("%s%s %4s", name, spacing, qty);
    }
    private String formatOverview(String name, String qty) {
        int maxArticleWidth = 20; // Maximum space for article name (adjustable)

        // Shorten long article names
        if (name.length() > maxArticleWidth) {
            name = name.substring(0, maxArticleWidth);
        }

        // Calculate dynamic spacing
        int spacesNeeded = maxArticleWidth - name.length();
        String spacing = " ".repeat(spacesNeeded);

        // Format article line with aligned pricing
        return String.format("%s%s %5s", name, spacing, qty);
    }
    private String centerText(String text, int width) {
        int padSize = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padSize)) + text;
    }

    private void sendToPrinter(String receiptText) {
        try {
            // Find the thermal printer (e.g., Bixolon)
            PrintService printService = findThermalPrinter();
            if (printService == null) {
                System.err.println("No Bixolon receipt printer found.");
                return;
            }

            // Open a raw print job
            DocPrintJob job = printService.createPrintJob();
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();

            // Set the code page for Cyrillic characters (CP866 encoding)
            byte[] commandBytes = SET_CYRILLIC_CODE_PAGE;

            // Convert the receipt text to byte array using CP866 encoding
            byte[] receiptBytes = receiptText.getBytes("CP866");  // Use CP866 for Cyrillic text

            // Combine commandBytes and receiptBytes
            byte[] combinedBytes = new byte[commandBytes.length + receiptBytes.length];
            System.arraycopy(commandBytes, 0, combinedBytes, 0, commandBytes.length);
            System.arraycopy(receiptBytes, 0, combinedBytes, commandBytes.length, receiptBytes.length);

            // Retry sending the job if the printer is busy
            boolean jobSent = false;
            int retries = 3;
            while (retries > 0 && !jobSent) {
                try {
                    Doc doc = new SimpleDoc(combinedBytes, flavor, null);
                    job.print(doc, printAttributes);
                    jobSent = true;  // Job successfully sent
                } catch (PrintException e) {
                    if (e.getMessage().contains("already printing")) {
                        System.err.println("Printer is already printing, retrying...");
                        Thread.sleep(1000);  // Wait for 1 second before retrying
                        retries--;
                    } else {
                        throw e;  // Other exceptions are rethrown
                    }
                }
            }

            if (!jobSent) {
                System.err.println("Failed to send print job after multiple retries.");
            }

            // Send cut paper command at the end of the receipt
            if (jobSent) {
                //job.print(new SimpleDoc(CUT_PAPER, flavor, null), printAttributes);
                DocPrintJob cutJob = printService.createPrintJob();
                Doc cutDoc = new SimpleDoc(CUT_PAPER, flavor, null);
                cutJob.print(cutDoc, printAttributes); // Send cut paper command
            }

        } catch (Exception e) {
            System.out.println("send to printer failed: " + e.getMessage());
        }
    }


    private PrintService findThermalPrinter() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : services) {
            if (service.getName().toLowerCase().contains("bixolon")) { // Search for Bixolon printers
                return service;
            }
        }
        return PrintServiceLookup.lookupDefaultPrintService(); // Use default printer if no match found
    }

}

