package com.example.demo;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BillPrinter {

    public void printBill(String employeeName, ObservableList<String[]> orderData, double ddv18, double ddv15, double ddv5, double totalPrice) {
        int counterArticls=1;
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: top-center;");
        double sumddv = 0.0;
        sumddv=ddv18+ddv15+ddv5;
        // Title: "СМЕТКА"
        Label titleLabel = new Label("СМЕТКА");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        layout.getChildren().add(titleLabel);

        // Separator Line
        layout.getChildren().add(new Line(0, 0, 350, 0));

        // Articles Header
        HBox headerRow = new HBox();
        Label articleHeader = new Label("Артикли");
        //Label quantityHeader = new Label("Кол.");
        Label priceHeader = new Label("Кол.    Цена");

        articleHeader.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        //quantityHeader.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        priceHeader.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        headerRow.getChildren().addAll(articleHeader, spacer1, spacer2, priceHeader);
        layout.getChildren().add(headerRow);

        // Separator Line
        layout.getChildren().add(new Line(0, 0, 350, 0));

        // Articles Data
        for (String[] articleData : orderData) {
            if (articleData.length >= 4) {
                counterArticls++;
                Label articleNameLabel = new Label(articleData[0]);
                Label quantityLabel = new Label(articleData[1]);
                Label priceLabel = new Label(articleData[2]);
                Label attpriceLabel = new Label(articleData[3]);

                Region articleSpacer = new Region();
                Region priceSpacer = new Region();

                HBox.setHgrow(articleSpacer, Priority.ALWAYS);
                HBox.setHgrow(priceSpacer, Priority.ALWAYS);

                // Set the quantity and price next to each other with an " x " between them
                Label quantityAndPriceLabel = new Label(quantityLabel.getText() + " x " + priceLabel.getText() + "  |   " + attpriceLabel.getText());

                HBox row = new HBox();
                row.getChildren().addAll(articleNameLabel, articleSpacer, quantityAndPriceLabel);
                layout.getChildren().add(row);
            }
        }

        // Separator Line
        layout.getChildren().add(new Line(0, 0, 350, 0));

        // Tax Breakdown
        layout.getChildren().add(createLabelRow("ДДВ 18 %:", ddv18));
        layout.getChildren().add(createLabelRow("ДДВ 15 %:", ddv15));
        layout.getChildren().add(createLabelRow("ДДВ 5 %:", ddv5));

        // Make the "Вкупно ДДВ" label bold
        Label totalDdvLabel = new Label("Вкупно ДДВ:");
        totalDdvLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        layout.getChildren().add(createLabelRowWithCustomLabel(totalDdvLabel, sumddv));

        // Separator Line
        layout.getChildren().add(new Line(0, 0, 350, 0));

        // Make the "Вкупен промет" label bold
        Label totalPriceLabel = new Label("Вкупен промет:");
        totalPriceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        layout.getChildren().add(createLabelRowWithCustomLabel(totalPriceLabel, totalPrice));

        // Separator Line
        layout.getChildren().add(new Line(0, 0, 350, 0));

        // Employee and Date Info
        Label employeeLabel = new Label("Вработен: " + employeeName);
        Label dateTimeLabel = new Label("Датум: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) +
                "    Час: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        Label thankYouLabel = new Label("ПОВЕЛЕТЕ ПОВТОРНО!");

        layout.getChildren().addAll(employeeLabel, dateTimeLabel, thankYouLabel);
        layout.setPadding(new Insets(50,20,0,20));


        // **PREVIEW FIRST, THEN PRINT**
        int previewWidth = 378;  // Default width
        int previewHeight = 350 + (counterArticls * 25);  // Add extra height based on number of articles
//
//        Stage previewStage = new Stage();
//        previewStage.setScene(new Scene(layout, previewWidth, previewHeight));
//        previewStage.setTitle("Преглед на печатење");
//        previewStage.show();

        // Uncomment the next line to print without preview
        printNode(layout,previewHeight);
    }

    private HBox createLabelRow(String text, double value) {
        Label label = new Label(text);
        Label valueLabel = new Label(String.format("%.2f", value));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox();
        row.getChildren().addAll(label, spacer, valueLabel);
        return row;
    }
    private HBox createLabelRowWithCustomLabel(Label customLabel, double value) {
        Label valueLabel = new Label(String.format("%.2f", value));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox();
        row.getChildren().addAll(customLabel, spacer, valueLabel);
        return row;
    }

    public void printNode(Node node,int previewHeight) {
        PrinterJob job = PrinterJob.createPrinterJob();

        if (job != null) {
            // Set fixed width for the printer (10 cm ≈ 378 pixels)
            int fixedWidth = 378;

            if (node instanceof Region) {
                ((Region) node).setPrefWidth(fixedWidth);
                ((Region) node).setMinWidth(fixedWidth);
                ((Region) node).setMaxWidth(fixedWidth);
                ((Region) node).setMinHeight(previewHeight);
                ((Region) node).setMaxHeight(previewHeight);
                ((Region) node).setPrefHeight(previewHeight);
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
