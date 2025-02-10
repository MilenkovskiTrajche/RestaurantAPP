package com.example.demo;
import javafx.collections.ObservableList;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PrinterService {

    public void printOrder(String employeeName, int tableNumber, ObservableList<String[]> orderData, String title) {
        int countArticls =0;
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 10; -fx-alignment: top-center;");

        // Centered, bold title
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        layout.getChildren().add(titleLabel);

        // Employee name and table number (centered)
        Label employeeLabel = new Label("Вработен: " + employeeName + " | Маса: " + tableNumber);
        employeeLabel.setStyle("-fx-alignment: top-left;");
        layout.getChildren().add(employeeLabel);

        // Order time (centered)
        Label timeLabel = new Label("Време на нарачка: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy")));
        timeLabel.setStyle("-fx-alignment: top-left;");
        layout.getChildren().add(timeLabel);

        // Horizontal line after time
        Line separator = new Line(0, 0, 250, 0);
        layout.getChildren().add(separator);

        // Left-aligned "Артикли:" and order details
        VBox articlesLayout = new VBox(5);
        articlesLayout.setStyle("-fx-alignment: top-left; -fx-padding: 5;");

        for (String[] articleData : orderData) {
            if (articleData.length >= 4) {
                countArticls++;
                String articleName = articleData[1]; // Name
                String quantity = articleData[3];    // Quantity

                // Create labels for article name and quantity
                Label articleNameLabel = new Label(articleName);
                Label quantityLabel = new Label(quantity);

                // Set font for readability
                articleNameLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
                quantityLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

                // Create a flexible spacer
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS); // Makes the spacer push content to the right

                // Arrange the labels inside an HBox
                HBox row = new HBox();
                row.setSpacing(10); // Adjust spacing between name and quantity
                row.getChildren().addAll(articleNameLabel, spacer, quantityLabel);

                // Add the row to the articles layout
                articlesLayout.getChildren().add(row);
            }
        }

        layout.getChildren().add(articlesLayout);

        // Horizontal line after time
        Line separatorBotoom = new Line(0, 0, 250, 0);
        layout.getChildren().add(separatorBotoom);

        // **PREVIEW FIRST, THEN PRINT**
        int previewWidth = 300;  // Fixed width
        int previewHeight = 150 + (orderData.size() * 20);  // Adjust height based on number of articles

        // Ensure a minimum height of 400px
        previewHeight = Math.max(previewHeight, 150);

        Stage previewStage = new Stage();
        previewStage.setScene(new Scene(layout, previewWidth, previewHeight));
        previewStage.setTitle("Преглед на печатење");
        previewStage.show();

        // Uncomment the next line to print without preview
        // printNode(layout,countArticls);
    }

    private void printNode(Node node, int numberOfArticles) {
        Printer printer = Printer.getDefaultPrinter();
        PrinterJob job = PrinterJob.createPrinterJob();

        if (job != null && job.showPrintDialog(null)) {
            // Get the printable area
            PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, 10, 10, 10, 10);
            double printableWidth = pageLayout.getPrintableWidth();
            double printableHeight = pageLayout.getPrintableHeight();

            // Calculate estimated height of the bill
            double baseHeight = 150; // Fixed height for headers, tax breakdown, total, employee info
            double articleRowHeight = 20; // Estimated height per article row
            double estimatedHeight = baseHeight + (numberOfArticles * articleRowHeight);

            // Scale if bill is too tall
            double scale = 1.0;
            if (estimatedHeight > printableHeight) {
                scale = printableHeight / estimatedHeight;
            }

            // Apply scaling only if needed
            if (scale < 1.0) {
                node.setScaleX(scale);
                node.setScaleY(scale);
            }

            // Print the scaled bill
            boolean success = job.printPage(node);
            if (success) {
                job.endJob();
            }
        }
    }
}

