package com.example.demo;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;
import java.util.Optional;


public class Admin extends Application {
    public final ListView<String> vrabotenList = new ListView<>();
    private final ComboBox<String> tipComboBox = new ComboBox<>();
    TableView<String[]> articleTable = new TableView<>();
    // Buttons for the options
    public Button addEditEmployeeButton = new Button("Додади/Промени вработен");
    public Button addEditArticleButton = new Button("Додади/Промени артикл");
    public Button viewBy = new Button("Прегледи");

    @Override
    public void start(Stage primaryStage) {
        homescreen(primaryStage);
    }

    private void homescreen(Stage primaryStage) {
        // Create the main layout
        VBox mainPane = new VBox(20); // 20 px spacing between elements
        mainPane.setPadding(new Insets(20));
        mainPane.setAlignment(Pos.CENTER); // Center everything

        // Password section at the top
        Label passwordLabel = new Label("Шифра");
        passwordLabel.setStyle("-fx-font-size: 40; -fx-font-weight: bold;");
        PasswordField passwordField = new PasswordField();
        passwordField.setMaxWidth(150);
        passwordField.setPrefSize(200, 50);
        passwordField.setStyle("-fx-font-size: 40; -fx-alignment: center;");

        // Limit password input to 3 characters
        TextFormatter<String> passwordFormatter = new TextFormatter<>(change ->
                change.getControlNewText().length() <= 3 ? change : null
        );
        passwordField.setTextFormatter(passwordFormatter);

        Button exitButton = new Button("Излези");

        // Set common button styling
        addEditEmployeeButton.setStyle("-fx-font-size: 25;");
        addEditArticleButton.setStyle("-fx-font-size: 25;");
        viewBy.setStyle("-fx-font-size: 25;");
        exitButton.setStyle("-fx-font-size: 25;");

        // Set button sizes
        addEditEmployeeButton.setPrefSize(400, 50);
        addEditArticleButton.setPrefSize(400, 50);
        viewBy.setPrefSize(400, 50);
        exitButton.setPrefSize(400, 50);

        // Disable buttons initially
        addEditEmployeeButton.setDisable(true);
        addEditArticleButton.setDisable(true);
        viewBy.setDisable(true);

        passwordField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                int pasword = 0;
                try {
                     pasword= Integer.parseInt(passwordField.getText());
                }catch (NumberFormatException e) {
                    showAlertError("Внеси шифра!");
                    passwordField.clear();
                }
                if(checkAdmin(String.valueOf(pasword))) {
                    addEditEmployeeButton.setDisable(false);
                    addEditArticleButton.setDisable(false);
                    viewBy.setDisable(false);
                }else{
                    showAlertInformation("Внесената шифра не е админ.");
                    addEditEmployeeButton.setDisable(true);
                    addEditArticleButton.setDisable(true);
                    viewBy.setDisable(true);
                }
                passwordField.clear();
            }
        });

        // Add everything to the layout
        mainPane.getChildren().addAll(
                passwordLabel, passwordField,
                addEditEmployeeButton, addEditArticleButton, viewBy, exitButton
        );
        addEditEmployeeButton.setOnAction(_ -> addEditEmployeeButton());
        addEditArticleButton.setOnAction(_ -> addEditArticleButton());
        viewBy.setOnAction(_ ->{
            RestaurantApp restaurantApp = new RestaurantApp();
            restaurantApp.showAdminPanel();
        });
        exitButton.setOnAction(_ -> primaryStage.close());

        Platform.runLater(passwordField::requestFocus);
        // Create the scene
        Scene scene = new Scene(mainPane, 800, 500);
        primaryStage.setTitle("Admin");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    static boolean checkAdmin(String proverka) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select tip from Vraboten where Shifra = ?"))
        {
            String tip= "";
            stmt.setInt(1, Integer.parseInt(proverka));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                tip = rs.getString("tip");
            }
            if(tip.equals("admin")){
                return true;
            }

        } catch (SQLException e) {
            System.out.println("HandleTableEntry sql exception" + e);

        }
        return false;
    }

    private void addEditEmployeeButton() {
        Stage primaryStage = new Stage();
        TextField shifraField = new TextField();
        TextField imeField = new TextField();
        // Left pane: List of vraboten
        VBox leftPane = new VBox(10);
        //leftPane.setPadding(new Insets(10));
        leftPane.setAlignment(Pos.CENTER);
        leftPane.setPrefWidth(300);

        Label listLabel = new Label("Вработени");
        listLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        vrabotenList.setPrefHeight(350);
        vrabotenList.setStyle("-fx-font-size: 18;");
        Button refreshButton = new Button("Освежи");
        refreshButton.setOnAction(_ ->{ vrabotenList.getItems().clear();loadVrabotenList();});

        leftPane.getChildren().addAll(listLabel, vrabotenList, refreshButton);

        // Right pane: Input fields for shifra, ime, tip
        VBox rightPane = new VBox(10);
        //rightPane.setPadding(new Insets(10));
        rightPane.setAlignment(Pos.CENTER);
        rightPane.setPrefWidth(400);

        Label formLabel = new Label("Додади / Промени Вработен");
        formLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        Label shifraLabel = new Label("Шифра:");
        shifraField.setPrefWidth(100);
        shifraField.setMaxWidth(150);
        TextFormatter<String> passwordFormatter = new TextFormatter<>(change ->
                change.getControlNewText().length() <= 3 ? change : null
        );
        shifraField.setTextFormatter(passwordFormatter);

        Label imeLabel = new Label("Име:");
        imeField.setPrefWidth(100);
        imeField.setMaxWidth(150);

        ComboBox<String> tipComboBox = new ComboBox<>();
        tipComboBox.getItems().addAll("kelner", "admin");
        tipComboBox.setPrefWidth(150);

        HBox dugminja = new HBox(10);
        //dugminja.setPadding(new Insets(10));
        dugminja.setAlignment(Pos.BOTTOM_CENTER);
        dugminja.setPrefWidth(800);

        Button saveButton = new Button("Зачувај");
        saveButton.setPrefWidth(100);
        saveButton.setOnAction(_ ->{
            if(tipComboBox.getItems().isEmpty()){
                showAlertInformation("Мора да изберите тип на вработен!");
                return;
            }
            saveVraboten(shifraField.getText(), imeField.getText(), tipComboBox.getValue());
            vrabotenList.getItems().clear();
            loadVrabotenList();
            shifraField.clear();
            imeField.clear();
            tipComboBox.getSelectionModel().clearSelection();
        });

        Button updateButton = new Button("Промени");
        updateButton.setPrefWidth(130);
        updateButton.setOnAction(_ -> {
            String selected = vrabotenList.getSelectionModel().getSelectedItem();
            if(selected != null) {
                String[] parts = selected.split(" - ");
                if(tipComboBox.getItems().isEmpty()){
                    showAlertInformation("Мора да изберите тип на вработен!");
                    return;
                }
                updateVraboten(shifraField.getText(), imeField.getText(), tipComboBox.getValue(),parts[0]);
                vrabotenList.getItems().clear();
                loadVrabotenList();
                shifraField.clear();
                imeField.clear();
                tipComboBox.getSelectionModel().clearSelection();
            }
        });

        vrabotenList.setOnMouseClicked(_ ->{
            String selected = vrabotenList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String[] parts = selected.split(" - ");
                shifraField.setText(parts[0]);
                imeField.setText(parts[1]);
                tipComboBox.setValue(parts[2]);
            }
        });

        Button deleteButton = new Button("Избриши");
        deleteButton.setPrefWidth(130);
        deleteButton.setOnAction(_ -> {
            String selected = vrabotenList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlertInformation("Изберете вработен за бришење.");
                return;
            }
            deleteVraboten(shifraField.getText());
            vrabotenList.getItems().clear();
            loadVrabotenList();
            shifraField.clear();
            imeField.clear();
            tipComboBox.getSelectionModel().clearSelection();
        });


        Button exit = new Button("Излези");
        exit.setPrefWidth(100);
        exit.setOnAction(_ -> {
            primaryStage.close();
            // Disable buttons initially
            addEditEmployeeButton.setDisable(true);
            addEditArticleButton.setDisable(true);
            viewBy.setDisable(true);
        });

        dugminja.getChildren().addAll(saveButton,updateButton,deleteButton,exit);
        rightPane.getChildren().addAll(formLabel, shifraLabel, shifraField, imeLabel, imeField, tipComboBox,dugminja);

        // Main layout
        HBox mainLayout = new HBox(10, leftPane, rightPane);
        //mainLayout.setPadding(new Insets(10));

        // Scene and stage setup
        Scene scene = new Scene(mainLayout, 800, 500);
        primaryStage.setTitle("Вработени");
        primaryStage.setScene(scene);
        RestaurantApp.enableMacedonianKeyboard(scene);
        primaryStage.show();

        // Load initial data
        vrabotenList.getItems().clear();
        loadVrabotenList();
        shifraField.clear();
        imeField.clear();
        tipComboBox.getSelectionModel().clearSelection();
    }

    private void loadVrabotenList() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select * from Vraboten"))
        {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int shifra = rs.getInt("shifra");
                String ime = rs.getString("ime");
                String tip = rs.getString("tip");
                vrabotenList.getItems().add(shifra + " - " + ime + " - " + tip);
            }
        } catch (SQLException e) {
            System.out.println("HandleTableEntry sql exception" + e);

        }
    }

    private void saveVraboten(String pass,String ime_novo,String tip_nov) {
        int shifra;
        if(pass.length() <3) {
            showAlertError("Шифрата мора да биде 3 бројки.");
            return;
        }
        try {
            shifra = Integer.parseInt(pass);
        } catch (NumberFormatException e) {
            showAlertInformation("Шифрата мора да биде број.");
            return;
        }

        if (ime_novo.isEmpty() || tip_nov.isEmpty()) {
            showAlertInformation("Сите полиња мора да бидат пополнети.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check for duplicate shifra
            PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM Vraboten WHERE shifra = ?");
            checkStmt.setInt(1, shifra);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                showAlertError("Шифрата веќе постои. Ве молиме внесете уникатна шифра.");
                return;
            }

            // Insert or update record
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO Vraboten (shifra, ime, tip) VALUES (?, ?, ?) ON CONFLICT (shifra) DO UPDATE SET ime = ?, tip = ?");
            stmt.setInt(1, shifra);
            stmt.setString(2, ime_novo);
            stmt.setString(3, tip_nov);
            stmt.setString(4, ime_novo);
            stmt.setString(5, tip_nov);
            stmt.executeUpdate();

            showAlertInformation("Вработениот е зачуван успешно.");
            vrabotenList.getItems().clear();
            loadVrabotenList();
        } catch (SQLException e) {
            showAlertError("Настана грешка при зачувување");
        }
    }
    private void deleteVraboten(String shifra) {
        int shifraInt;
        if (shifra.length() < 3) {
            showAlertError("Шифрата мора да биде 3 бројки.");
            return;
        }
        try {
            shifraInt = Integer.parseInt(shifra);
        } catch (NumberFormatException e) {
            showAlertInformation("Шифрата мора да биде број.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();) {

            // Step 1: Begin transaction
            conn.setAutoCommit(false);

            try {
                // Step 2: Check if the employee exists
                String checkQuery = "SELECT COUNT(*) FROM Vraboten WHERE Shifra = ?";
                try (PreparedStatement stmtCheck = conn.prepareStatement(checkQuery)) {
                    stmtCheck.setInt(1, shifraInt);
                    try (ResultSet rs = stmtCheck.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            showAlertError("Вработен со дадената шифра не постои или е веќе избришан.");
                            conn.rollback();
                            return;
                        }
                    }
                }

                // Step 3: Soft delete the employee - set active = FALSE and set deleted_at timestamp
                String updateQuery = "delete from vraboten where shifra = ?";
                try (PreparedStatement stmtUpdate = conn.prepareStatement(updateQuery)) {
                    stmtUpdate.setInt(1, shifraInt);
                    int rowsUpdated = stmtUpdate.executeUpdate();
                    if (rowsUpdated > 0) {
                        showAlertInformation("Вработениот е успешно избришан.");
                    } else {
                        showAlertError("Вработен со дадената шифра не постои.");
                    }
                }

                // Step 4: Commit the transaction
                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                showAlertError("Настана грешка при бришење: " + e.getMessage());
            }

        } catch (SQLException e) {
            showAlertError("Настана грешка при поврзување со базата: " + e.getMessage());
        }
    }


    private void updateVraboten(String pass,String ime_novo,String tip_nov,String shifrapostoecka) {
        int shifra;
        if(pass.length() <3 || shifrapostoecka.length() <3) {
            showAlertError("Шифрата мора да биде 3 бројки.");
            return;
        }
        try {
            shifra = Integer.parseInt(pass);
        } catch (NumberFormatException e) {
            showAlertInformation("Шифрата мора да биде број.");
            return;
        }
        if (ime_novo.isEmpty() || tip_nov == null) {
            showAlertInformation("Сите полиња мора да бидат пополнети.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE Vraboten SET shifra = ?, ime = ?, tip = ? WHERE shifra = ?");
            stmt.setInt(1,shifra);
            stmt.setString(2, ime_novo);
            stmt.setString(3, tip_nov);
            stmt.setInt(4, Integer.parseInt(shifrapostoecka));

            int updatedRows = stmt.executeUpdate();
            if (updatedRows > 0) {
                showAlertInformation("Вработениот е променет успешно.");
                loadVrabotenList();
            } else {
                showAlertError("Вработен со дадената шифра не постои.");
            }

        } catch (SQLException e) {
            showAlertError("Настана грешка при промена: " + e.getMessage());
        }
    }

    private void addEditArticleButton() {
        // Table for displaying articles
        Stage primaryStage = new Stage();
        articleTable = new TableView<>();
        TableColumn<String[], String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0]));
        TableColumn<String[], String> nazivColumn = new TableColumn<>("Назив");
        nazivColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1]));

        TableColumn<String[], String> cenaColumn = new TableColumn<>("Цена");
        cenaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[2]));

        TableColumn<String[], String> ddvColumn = new TableColumn<>("ДДВ");
        ddvColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[3]));

        TableColumn<String[], String> tipColumn = new TableColumn<>("Тип");
        tipColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[4]));

        articleTable.getColumns().addAll(idColumn,nazivColumn, cenaColumn, ddvColumn, tipColumn);
        articleTable.setPrefWidth(500);
        articleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        articleTable.setStyle("-fx-font-size: 18");

        // Load data into the table
        loadArticleTable(articleTable);
        // Input fields
        TextField nazivField = new TextField();
        nazivField.setPromptText("Назив");
        nazivField.setPrefWidth(100);
        nazivField.setStyle("-fx-font-size: 18");

        TextField cenaField = new TextField();
        cenaField.setPromptText("Цена");
        cenaField.setPrefWidth(100);
        cenaField.setStyle("-fx-font-size: 18");

        HBox ddvtip = new HBox(10);
        //dugminja.setPadding(new Insets(10));
        ddvtip.setAlignment(Pos.BOTTOM_CENTER);
        ddvtip.setPrefWidth(550);

        Label ddvlabel = new Label("ДДВ");
        ComboBox<Integer> ddvComboBox = new ComboBox<>();
        ddvComboBox.getItems().addAll(18, 10, 5);
        ddvComboBox.setValue(18); // Default DDV value
        ddvComboBox.setPrefWidth(100);
        ddvComboBox.setStyle("-fx-font-size: 18");

        Label tiplabel = new Label("Тип:");
        ComboBox<String> tipComboBox = new ComboBox<>();
        tipComboBox.getItems().addAll("kujna", "shank");
        tipComboBox.setValue("kujna"); // Default tip value
        tipComboBox.setPrefWidth(120);
        tipComboBox.setStyle("-fx-font-size: 18");

        ddvtip.getChildren().addAll(ddvlabel,ddvComboBox, tiplabel,tipComboBox);

        HBox dugminja = new HBox(10);
        //dugminja.setPadding(new Insets(10));
        dugminja.setAlignment(Pos.BOTTOM_CENTER);
        dugminja.setPrefWidth(550);
        // Buttons
        Button saveButton = new Button("Зачувај");
        saveButton.setPrefWidth(130);
        saveButton.setOnAction(_ -> {
                    if(nazivField.getText().contains("ј") || nazivField.getText().contains("Ј")){
                        for(int i=0;i<nazivField.getText().length();i++){
                            if(nazivField.getText().charAt(i)=='ј'){
                                nazivField.replaceSelection("j");
                            }
                            if(nazivField.getText().charAt(i)=='Ј'){
                                nazivField.replaceSelection("J");
                            }
                        }
                    }
                    saveArticle(nazivField, cenaField, ddvComboBox, tipComboBox, articleTable);
                    nazivField.clear();cenaField.clear();
        });

        Button updateButton = new Button("Промени");
        updateButton.setPrefWidth(130);
        updateButton.setOnAction(_ -> {
            changeLatters(nazivField.getText());
            updateArticle(nazivField, cenaField, ddvComboBox, tipComboBox, articleTable, articleTable.getSelectionModel().getSelectedItem());
            nazivField.clear();cenaField.clear();
        });

        Button refreshButton = new Button("Освежи");
        refreshButton.setPrefWidth(130);
        refreshButton.setOnAction(_ -> {
            loadArticleTable(articleTable);
            nazivField.clear();cenaField.clear();
        });

        Button deleteBtn = new Button("Избриши");
        deleteBtn.setPrefWidth(130);
        deleteBtn.setOnAction(_ -> {
            String[] selectedArticle = articleTable.getSelectionModel().getSelectedItem();
            if(selectedArticle != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Дали сте сигурни за бришење на артикл?");
                alert.setHeaderText(null); // You can add a header text if needed
                alert.setContentText("Бришење на артикл " + selectedArticle[1]);

                // Show the dialog and wait for the user's response
                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    deleteArticle(selectedArticle);
                } else {
                    showAlertInformation("Откажано бришење");
                }
            }
            nazivField.clear();cenaField.clear();

        });

        Button exitButton = new Button("Излези");
        exitButton.setPrefWidth(130);
        exitButton.setOnAction(_ -> {
            primaryStage.close();
            nazivField.clear();cenaField.clear();
            // Disable buttons initially
            addEditEmployeeButton.setDisable(true);
            addEditArticleButton.setDisable(true);
            viewBy.setDisable(true);
        });

        articleTable.setOnMouseClicked(_->{
            String[] selectedArticle = articleTable.getSelectionModel().getSelectedItem();
            if(selectedArticle != null) {
                nazivField.setText(selectedArticle[1]);
                cenaField.setText(selectedArticle[2]);
                ddvComboBox.setValue(Integer.valueOf(selectedArticle[3]));
                tipComboBox.setValue(selectedArticle[4]);
            }
        });
        dugminja.getChildren().addAll(saveButton, updateButton, refreshButton, deleteBtn,exitButton);
        // Right pane layout
        VBox rightPane = new VBox(10,
                new Label("Назив"), nazivField,
                new Label("Цена"), cenaField,
                ddvtip,
                dugminja);
        rightPane.setPadding(new Insets(10));
        rightPane.setAlignment(Pos.CENTER);

        // Main layout
        HBox mainLayout = new HBox(10, articleTable, rightPane);
        mainLayout.setPadding(new Insets(10));

        // Scene setup
        Scene scene = new Scene(mainLayout);
        primaryStage.setTitle("Додади/Промени Артикл");
        RestaurantApp.enableMacedonianKeyboard(scene);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void changeLatters(String zbor){
        System.out.println(zbor);
        if(zbor.contains("ј")){
            zbor.replace('ј','j');
        }
        if (zbor.contains("ѓ")){
            zbor.replace('ѓ','г');
        }
        if(zbor.contains("ѕ")){
            zbor.replace('ѕ','s');
        }
        if(zbor.contains("љ")){
            zbor.replace('љ','л');
        }
        if(zbor.contains("њ")){
            zbor.replace('њ','н');
        }
        if(zbor.contains("ќ")){
            zbor.replace('ќ','к');
        }
        System.out.println(zbor);
    }
    // Load articles into the TableView
    private void loadArticleTable(TableView<String[]> articleTable) {
        articleTable.getItems().clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Artikl")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String id = String.valueOf(rs.getInt("id"));
                String naziv = rs.getString("naziv");
                String cena = String.valueOf(rs.getInt("cena"));
                String ddv = String.valueOf(rs.getInt("ddv"));
                String tip = rs.getString("tip");
                articleTable.getItems().add(new String[]{id,naziv, cena, ddv, tip});
            }
        } catch (SQLException e) {
            showAlertError("Грешка при читање на артикли: " + e.getMessage());
        }
    }

    // Save a new article
    private void saveArticle(TextField nazivField, TextField cenaField, ComboBox<Integer> ddvComboBox, ComboBox<String> tipComboBox, TableView<String[]> articleTable) {
        String naziv = nazivField.getText();
        int cena;
        try {
            cena = Integer.parseInt(cenaField.getText());
        } catch (NumberFormatException e) {
            showAlertInformation("Цена мора да биде број.");
            return;
        }
        int ddv = ddvComboBox.getValue();
        String tip = tipComboBox.getValue();

        if (naziv.isEmpty()) {
            showAlertInformation("Називот не може да биде празен.");
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection();) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO Artikl (naziv, cena, ddv, tip) VALUES (?, ?, ?, ?)");
            stmt.setString(1, naziv);
            stmt.setInt(2, cena);
            stmt.setInt(3, ddv);
            stmt.setString(4, tip);
            stmt.executeUpdate();
            showAlertInformation("Артиклот е зачуван успешно.");
            loadArticleTable(articleTable);
        } catch (SQLException e) {
            showAlertError("Грешка при зачувување на артикл: " + e.getMessage());
        }
    }

    // Update an existing article
    private void updateArticle(TextField nazivField, TextField cenaField, ComboBox<Integer> ddvComboBox, ComboBox<String> tipComboBox, TableView<String[]> articleTable, String[] selectedArticle) {
        if (selectedArticle == null) {
            showAlertInformation("Изберете артикл за промена.");
            return;
        }
        String id =selectedArticle[0];
        String naziv = nazivField.getText();
        int cena;
        try {
            cena = Integer.parseInt(cenaField.getText());
        } catch (NumberFormatException e) {
            showAlertInformation("Цена мора да биде број.");
            return;
        }
        int ddv = ddvComboBox.getValue();
        String tip = tipComboBox.getValue();

        if (naziv.isEmpty()) {
            showAlertError("Називот не може да биде празен.");
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection();) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE Artikl SET naziv = ?, cena = ?, ddv = ?, tip = ? WHERE id = ?");
            stmt.setString(1, naziv);
            stmt.setInt(2, cena);
            stmt.setInt(3, ddv);
            stmt.setString(4, tip);
            stmt.setInt(5, Integer.parseInt(id));
            stmt.executeUpdate();
            showAlertInformation("Артиклот е променет успешно.");
            loadArticleTable(articleTable);
        } catch (SQLException e) {
            showAlertError("Грешка при промена на артикл: " + e.getMessage());
        }
    }

    private void deleteArticle(String [] selectedArticle) {
        int id = Integer.parseInt(selectedArticle[0]);
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("Delete from artikl WHERE id = ?");
            stmt.setInt(1, id);

            stmt.executeUpdate();
            showAlertInformation("Артиклот е избришан." + selectedArticle[1]);
            loadArticleTable(articleTable);
        } catch (SQLException e) {
            showAlertError("Грешка при промена на артикл: " + e.getMessage());
        }
    }
    static void showAlertInformation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информација");
        alert.setContentText(message);
        alert.showAndWait();
    }
    private static void showAlertError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Грешка");
        alert.setContentText(message);
        alert.showAndWait();
    }
        public static void main(String[] args) {
        launch(args);
    }
}

