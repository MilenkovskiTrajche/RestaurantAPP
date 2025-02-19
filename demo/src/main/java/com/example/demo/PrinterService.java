package com.example.demo;
import javafx.collections.ObservableList;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PrinterService {

    private static final byte[] CUT_PAPER = {0x1B, 0x69}; // ESC i for partial cut, ESC m (0x1B, 0x6D) for full cut
    private static final byte[] RESET_PRINTER = {0x1B, 0x40}; // Reset the printer to default settings
    private static final byte[] SET_CYRILLIC_CODE_PAGE = {0x1B, 0x74, 17}; // ESC t 17 for CP1251 (Windows Cyrillic encoding)

    private static PrinterService instance;
    private final ExecutorService printExecutor;

    // Private constructor
    private PrinterService() {
        // Create a single-threaded executor to ensure jobs are executed one by one
        this.printExecutor = Executors.newSingleThreadExecutor();
    }

    public static PrinterService getInstance() {
        if (instance == null) {
            synchronized (PrinterService.class) {
                if (instance == null) {
                    instance = new PrinterService();
                }
            }
        }
        return instance;
    }

    // Submit a print job for sequential execution
    public void submitPrintJob(Runnable printJob) {
        printExecutor.submit(printJob);
    }

    public void shutdown() {
        printExecutor.shutdown();
        try {
            if (!printExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                printExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            printExecutor.shutdownNow();
        }
    }


    public static String formatPrice(double price){
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(price).replace(",", ".");
    }
    public Boolean printOrder(String employeeName, int tableNumber, ObservableList<String[]> orderData, String title) {
        StringBuilder receipt = new StringBuilder();
        // Add title
        receipt.append(centerText(title)).append("\n");
        receipt.append("ВРАБОТЕН: ").append(employeeName).append(" | МАСА: ").append(tableNumber).append("\n");
        receipt.append("ВРЕМЕ: ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy"))).append("\n");
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
        return sendToPrinter(receipt.toString());
    }

    public boolean printEmployeeOverview(String title, double fiscaltotal, double invoicetotal, double karticatotal, LocalDate datefrom, LocalDate dateto, String timefrom, String timeto) {
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
        receipt.append(formatOverview("Фискален промет:",formatPrice(fiscaltotal))).append("\n");
        receipt.append("\n");
        receipt.append(formatOverview("Фактура промет:", formatPrice(invoicetotal))).append("\n");
        receipt.append("\n");
        receipt.append(formatOverview("Картица промет:", formatPrice(karticatotal))).append("\n");
        receipt.append("\n");
        receipt.append(formatOverview("Вкупен промет:", formatPrice(fiscaltotal+invoicetotal+karticatotal))).append("\n");
        receipt.append("--------------------------------\n");
        receipt.append("\n");
        receipt.append("\n\n");
        // Send the receipt to the printer
        return sendToPrinter(receipt.toString());
    }

    public Boolean printBill(String employeeName, ObservableList<String[]> orderData, double ddv18, double ddv15, double ddv5, double totalPrice) {
        StringBuilder receipt = new StringBuilder();

        // Add title
        receipt.append(centerText("СМЕТКА")).append("\n");
        receipt.append("--------------------------------\n");

        // Add articles
        receipt.append("Артикли              Кол.  Цена\n");
        receipt.append("--------------------------------\n");

        for (String[] articleData : orderData) {
            if (articleData.length >= 4) {
                String articleName = articleData[0];
                String quantity = articleData[1];
                String unitPrice = PrinterService.formatPrice(Double.parseDouble(articleData[2]));
                String totalPriceForItem = PrinterService.formatPrice(Double.parseDouble(articleData[3]));

                // Format article line
                receipt.append(formatArticleBill(articleName, quantity, unitPrice, totalPriceForItem)).append("\n");
            }
        }

        receipt.append("--------------------------------\n");
        receipt.append(String.format("ДДВ 18%%: %23.2f\n", ddv18));
        receipt.append(String.format("ДДВ 10%%: %23.2f\n", ddv15));
        receipt.append(String.format("ДДВ 5%%:  %23.2f\n", ddv5));
        receipt.append(String.format("Вкупно ДДВ: %20.2f\n", (ddv18 + ddv15 + ddv5)));
        receipt.append("--------------------------------\n");
        receipt.append(String.format("Вкупен промет: %17.2f\n", totalPrice));
        receipt.append("--------------------------------\n");

        // Add employee and date-time info
        receipt.append("Вработен: ").append(employeeName).append("\n");
        receipt.append("Датум: ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy"))).append("\n");
        receipt.append("\n");
        receipt.append(centerText("ПОВЕЛЕТЕ ПОВТОРНО!\n\n\n")).append("\n");
        receipt.append("\n");
        // Send the receipt to the printer
        return sendToPrinter(receipt.toString());
    }
    public Boolean printAdminBill(String employeeName, ObservableList<String[]> orderData, double ddv18, double ddv15, double ddv5, double totalPrice,String datum,String vreme) {
        StringBuilder receipt = new StringBuilder();

        // Add title
        receipt.append(centerText("СМЕТКА")).append("\n");
        receipt.append("--------------------------------\n");

        // Add articles
        receipt.append("Артикли              Кол.  Цена\n");
        receipt.append("--------------------------------\n");

        for (String[] articleData : orderData) {
            if (articleData.length >= 4) {
                String articleName = articleData[0];
                String quantity = articleData[1];
                String unitPrice = articleData[2];
                String totalPriceForItem = articleData[3];

                // Format article line
                receipt.append(formatArticleBill(articleName, quantity, unitPrice, totalPriceForItem)).append("\n");
            }
        }

        receipt.append("--------------------------------\n");
        receipt.append(String.format("ДДВ 18%%: %23.2f\n", ddv18));
        receipt.append(String.format("ДДВ 10%%: %23.2f\n", ddv15));
        receipt.append(String.format("ДДВ 5%%:  %23.2f\n", ddv5));
        receipt.append(String.format("Вкупно ДДВ: %20.2f\n", (ddv18 + ddv15 + ddv5)));
        receipt.append("--------------------------------\n");
        receipt.append(String.format("Вкупен промет: %17.2f\n", totalPrice));
        receipt.append("--------------------------------\n");

        // Add employee and date-time info
        receipt.append("Вработен: ").append(employeeName).append("\n");
        receipt.append("Датум: ").append(datum).append(" ").append(vreme).append("\n");
        receipt.append("\n");
        receipt.append(centerText("ПОВЕЛЕТЕ ПОВТОРНО!")).append("\n");
        receipt.append("\n");
        receipt.append("\n");
        // Send the receipt to the printer
        return sendToPrinter(receipt.toString());
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
    private String formatArticleBill(String name, String qty, String price, String total) {
        int maxArticleWidth = 20; // Maximum space for article name (adjustable)

        // Shorten long article names
        if (name.length() > maxArticleWidth) {
            name = name.substring(0, maxArticleWidth);
        }

        // Calculate dynamic spacing
        int spacesNeeded = maxArticleWidth - name.length();
        String spacing = " ".repeat(spacesNeeded);

        // Format article line with aligned pricing
        return String.format("%s%s %2s x %4s |%5s", name, spacing, qty, price, total);
    }
    private String centerText(String text) {
        int padSize = (32 - text.length()) / 2;
        return " ".repeat(Math.max(0, padSize)) + text;
    }

private Boolean sendToPrinter(String receiptText) {
    try {
        // Find the thermal printer (e.g., Bixolon)
        PrintService printService = findThermalPrinter();
        if (printService == null) {
            RestaurantApp.showAlert("Не е најден принтерот - bixolon");
            return false;
        }
        // Open a raw print job
        DocPrintJob job = printService.createPrintJob();
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();

        // Set the code page for Cyrillic characters (CP866 encoding)
        byte[] commandBytes = SET_CYRILLIC_CODE_PAGE;

        // Convert the receipt text to byte array using CP866 encoding
        byte[] receiptBytes = receiptText.getBytes("CP866"); // Use CP866 for Cyrillic text

        // Combine commandBytes, receiptBytes, and CUT_PAPER
        byte[] combinedBytes = new byte[commandBytes.length + receiptBytes.length + CUT_PAPER.length];
        System.arraycopy(commandBytes, 0, combinedBytes, 0, commandBytes.length);
        System.arraycopy(receiptBytes, 0, combinedBytes, commandBytes.length, receiptBytes.length);
        System.arraycopy(CUT_PAPER, 0, combinedBytes, commandBytes.length + receiptBytes.length, CUT_PAPER.length);

        // Send the combined data as a single print job
        Doc doc = new SimpleDoc(combinedBytes, flavor, null);
        job.print(doc, printAttributes);
        return true;
    } catch (Exception e) {
        clearPrintQueue();
        RestaurantApp.showAlert("Грешка при испраќање до принтер: " + e.getMessage());
        return false;
    }
}

    public void clearPrintQueue() {
        try {
            // Check the operating system
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // Generate the VBScript dynamically (optional)
                generateVBSFile();

                // Run the VBScript with wscript
                ProcessBuilder builder = new ProcessBuilder("wscript", "clearPrintQueue.vbs");
                builder.redirectErrorStream(true); // Redirect errors to the output stream
                Process process = builder.start();

                // Wait for the process to complete
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    RestaurantApp.showAlertInformation("Print spooler restarted");
                } else {
                    RestaurantApp.showAlert("Неуспешно, нема admin привилегии");
                }
            }
        } catch (IOException | InterruptedException e) {
            RestaurantApp.showAlert("Грешка при чистење на принтерот:\n" + e.getMessage());
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
    private void generateVBSFile() throws IOException {
        String vbsContent = """
        Set UAC = CreateObject("Shell.Application")
        UAC.ShellExecute "cmd.exe", "/c net stop spooler && timeout /t 5 && net start spooler", "", "runas", 1
        """;

        // Get the path to the current working directory
        String scriptPath = System.getProperty("user.dir") + File.separator + "clearPrintQueue.vbs";

        // Write the VBScript content to a file
        try (FileWriter writer = new FileWriter(scriptPath)) {
            writer.write(vbsContent);
        }
    }



}