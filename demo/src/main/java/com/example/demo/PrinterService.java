package com.example.demo;
import javafx.collections.ObservableList;
import javafx.print.*;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.ScrollPane;



public class PrinterService {

    public void printOrder(String employeeName, int tableNumber, ObservableList<String[]> orderData, String title) {
        int countArticls =0;
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: top-left;-fx-padding: 20;");
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
        Line separator = new Line(0, 0, 350, 0);
        layout.getChildren().add(separator);

        // Left-aligned "Артикли:" and order details
        VBox articlesLayout = new VBox(5);
        articlesLayout.setStyle("-fx-alignment: top-left;");

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
                spacer.setPrefWidth(50);

                HBox.setHgrow(spacer, Priority.ALWAYS); // Makes the spacer push content to the right

                // Arrange the labels inside an HBox
                HBox row = new HBox();
                row.setSpacing(5); // Adjust spacing between name and quantity
                row.getChildren().addAll(articleNameLabel, spacer, quantityLabel);

                // Add the row to the articles layout
                articlesLayout.getChildren().add(row);
            }
        }
        layout.getChildren().add(articlesLayout);

        // Horizontal line after time
        Line separatorBotoom = new Line(0, 0, 350, 0);
        layout.getChildren().add(separatorBotoom);

        // **PREVIEW FIRST, THEN PRINT**
        int previewWidth = 370;  // Fixed width
        int previewHeight = 150 + (countArticls * 30);

//        layout.setPrefSize(previewWidth, previewHeight);
//        Stage previewStage = new Stage();
//        previewStage.setScene(new Scene(layout, previewWidth, previewHeight));
//        previewStage.setTitle("Преглед на печатење");
//        previewStage.show();

        // Uncomment the next line to print without preview
        printNode(layout,previewHeight);
    }

    public void printNode(Node node,int previewHeight) {
        Printer printer = Printer.getDefaultPrinter();
        PrinterJob job = PrinterJob.createPrinterJob();

        if (job != null) {
            // Set fixed width for the printer (10 cm ≈ 378 pixels)
            int fixedWidth = 378;

            if (node instanceof Region) {
                ((Region) node).setPrefWidth(fixedWidth);
                ((Region) node).setMinWidth(fixedWidth);
                ((Region) node).setMaxWidth(fixedWidth);
                ((Region) node).setPrefHeight(previewHeight);
                ((Region) node).setMinHeight(previewHeight);
                ((Region) node).setMaxHeight(previewHeight);
            }

            // Create a preview stage without ScrollPane
            Stage previewStage = new Stage();
            Scene previewScene = new Scene(new ScrollPane(node)); // Wrap in a ScrollPane if long
            previewStage.setScene(previewScene);
            previewStage.setTitle("Преглед на печатењеPRINTNODE");
            previewStage.show();

//            // Print the scaled bill
//            boolean success = job.printPage(node);
//            if (success) {
//                job.endJob();
//            }
        }

    }
}

