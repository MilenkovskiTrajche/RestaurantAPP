package com.example.demo;


import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.stage.StageStyle;
import javafx.beans.binding.Bindings;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;


public class RestaurantApp extends Application {

    private static String ime="";
    TextField tableField = new TextField();
    TextField passwordField = new TextField();
    static final Set<Map<String, Object>> articls = new HashSet<>();
    private final Set<String> existingTables = new HashSet<>();
    private final GridPane tableGrid = new GridPane();
    TableView<String[]> orderTable = new TableView<>();
    private final ObservableList<String[]> orderData = FXCollections.observableArrayList();
    private final ObservableList<String[]> orderDataPrintShank = FXCollections.observableArrayList();
    private final ObservableList<String[]> orderDataPrintKujna = FXCollections.observableArrayList();


    public static final ObservableList<String[]> AllData = FXCollections.observableArrayList();
    private final Label totalPriceLabel = new Label("Вкупно: 0");
    private final TextField articleInputField = new TextField();
    private final TextField quantityInputField = new TextField();
    public int roww = 8;
    public int coll = 0;
    private Stage articleStage = new Stage();
    private final Stage adminStage = new Stage();
    Button adminfiskalnabtn = new Button("Печати - Фискална");
    Button adminfakturabtn = new Button("Печати - Фактура");
    private final Map<String, List<String[]>> tableOrders = new HashMap<>();
    private static TableView<String[]> tableView;
    private final ObservableList<String[]> smetkaData = FXCollections.observableArrayList();
    private final ObservableList<String[]> smetkadataAdmin = FXCollections.observableArrayList();
    private TableView<String[]> leftAdminTable = new TableView<>();
    private final ObservableList<String[]> rezultatiAdmin = FXCollections.observableArrayList();
    private TableView<String[]> rightAdminResultTable = new TableView<>();

    @Override
    public void start(Stage primaryStage) {
        ReadArticles();//procitajgi i zacuvajgi vo lista ednash
        tableGrid.setVgap(5);//okolu masite da ima prazno mesto
        getTabels();
        handleLogin(primaryStage);//login screen prikazi
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/paparaciloginbg.png"))));
    }
    private void handleLogin(Stage primaryStage) {
        // Get screen size
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

        // Left panel
        VBox leftPane = new VBox(screenHeight * 0.03); // Dynamic spacing
        leftPane.setPadding(new Insets(screenHeight * 0.02, screenWidth * 0.05, screenHeight * 0.02, screenWidth * 0.02));

        // Labels and Input Fields
        Label passwordLabel = new Label("Шифра");
        passwordField = new PasswordField();
        passwordLabel.styleProperty().bind(Bindings.concat("-fx-font-size: ", screenWidth / 30, "px;"));
        passwordField.setPrefSize(screenWidth * 0.15, screenHeight * 0.08);
        passwordField.styleProperty().bind(Bindings.concat("-fx-font-size: ", screenWidth / 25, "px;"));
        setMaxInputLength(passwordField, 3);

        Label tableLabel = new Label("Маса");
        tableField = new TextField();
        tableLabel.styleProperty().bind(Bindings.concat("-fx-font-size: ", screenWidth / 30, "px;"));
        tableField.setPrefSize(screenWidth * 0.15, screenHeight * 0.08);
        tableField.styleProperty().bind(Bindings.concat("-fx-font-size: ", screenWidth / 28, "px;"));
        setMaxInputLength(tableField, 5);

        leftPane.getChildren().addAll(passwordLabel, passwordField, tableLabel, tableField);
        setEnterKeyNavigation(passwordField, tableField);

        // Vertical Line Separator
        Region separator = new Region();
        separator.setPrefWidth(screenWidth * 0.005);
        separator.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        // Handle Enter key for tableField
        tableField.setOnAction(_ -> {
            String enteredTable = tableField.getText();
            try {
                int employeeId = Integer.parseInt(passwordField.getText());
                if (employeeId < 0) return;
            } catch (NumberFormatException e) {
                showAlert("Шифрата мора да биди број!");
                resetFields();
                return;
            }

            if (checkAdmin(passwordField.getText()) && enteredTable.isEmpty()) {
                showAdminPanel();
                resetFields();
                return;
            }

            try {
                int tn = Integer.parseInt(enteredTable);
                if (tn < 0) {
                    tableField.clear();
                    Platform.runLater(tableField::requestFocus);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Масата мора да биди број!");
                resetFields();
                return;
            }

            if (checkPassword(passwordField.getText())) {
                handleTableEntry(enteredTable, passwordField.getText());
                resetFields();
            } else {
                resetFields();
            }
        });

        // Align right panel with tables
        HBox rightPane = new HBox(screenWidth * 0.005, separator, tableGrid);
        rightPane.setAlignment(Pos.TOP_RIGHT);
        rightPane.setPadding(new Insets(screenHeight * 0.02, screenWidth * 0.02, screenHeight * 0.02, screenWidth * 0.03));

        leftPane.setAlignment(Pos.CENTER);

        // Combine Left and Right Panels
        HBox mainLayout = new HBox(screenWidth * 0.01, leftPane, rightPane);
        mainLayout.setPadding(new Insets(screenHeight * 0.02));

        // Create Scene and Stage
        Scene scene = new Scene(mainLayout);
        primaryStage.setTitle("Папараци2");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    // Helper method to reset fields and focus back on password field
    private void resetFields() {
        passwordField.setText("");
        tableField.setText("");
        Platform.runLater(passwordField::requestFocus);
    }


    // Handle table entry after pressing Enter
    private void handleTableEntry(String tableNumber, String ps) {
        if (tableNumber.isEmpty()) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM vraboten WHERE shifra = ?"))
        {
            stmt.setInt(1, Integer.parseInt(ps));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int employeeId = rs.getInt("Shifra");
                    initialize(tableNumber, employeeId);
                }
            }
        } catch (SQLException e) {
            System.out.println("HandleTableEntry sql exception" + e);
        }
    }
    private void getTabels() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("select v.ime,masa,shifra from naracka as n\n" +
                     "inner join vraboten as v on v.shifra=n.vrabotenshifra;")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String imee = rs.getString("ime");
                    int masaa = rs.getInt("masa");
                    int shifra = rs.getInt("shifra");
                    ime = imee;
                    existingTables.add(shifra + ":" + masaa);
                    addTableButtonToGrid(String.valueOf(masaa), coll, roww,shifra);
                    if (coll >= 5) {
                        coll = 0;
                        roww++;
                    } else {
                        coll++;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("GetTables sql exception" + e);
        }
    }

    public void initialize(String tn, int employeeId) {
        articleStage = new Stage();
        orderTable = new TableView<>();
        loadPreviousOrders(tn, employeeId);
        orderTable.setItems(AllData);
        TableView<String[]> availableArticlesTable;
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();

        // Create a SplitPane to divide the window into left and right sections
        SplitPane splitPane = new SplitPane();
        SplitPane splitPane1 = new SplitPane();

        availableArticlesTable = createAvailableArticlesTable();
        orderTable = createOrderTable(tn,employeeId);

        // VBox for displaying the total price (will take 10% of the height)
        VBox rightBox = new VBox(10);
        // Set the rightBox to take full height available
        rightBox.setStyle("-fx-pref-height: 100%;");
        rightBox.getChildren().addAll(orderTable);

        VBox.setVgrow(orderTable, Priority.ALWAYS);

        Label imevraboten = new Label();
        imevraboten.setText("Вработен:" + ime + "       Маса:" + tn);

        HBox bottombox = new HBox(20);
        bottombox.setPrefHeight(screenHeight * 0.05);
        bottombox.setMaxHeight(screenHeight * 0.05);
        bottombox.setMaxHeight(screenHeight * 0.05);

        HBox.setHgrow(imevraboten, Priority.ALWAYS);
        imevraboten.setMaxWidth(Double.MAX_VALUE);

        // Dynamic font size for labels
        imevraboten.styleProperty().bind(Bindings.concat("-fx-font-size: ", screenWidth / 50, "px;"));
        totalPriceLabel.styleProperty().bind(Bindings.concat("-fx-padding: 0 0 0 10;-fx-border-color: gray;-fx-font-weight: bold;-fx-font-size: ", screenWidth / 50, "px;"));
        bottombox.setPadding(new Insets(0,20,0,15));
        bottombox.getChildren().addAll(imevraboten,totalPriceLabel);
        // Create middle section layout with input fields and buttons
        VBox middleBox = createMiddleSection(tn, employeeId);
        splitPane.getItems().addAll(availableArticlesTable, middleBox, rightBox);
        splitPane.setDividerPosition(0, 0.30); // 30% left section
        splitPane.setDividerPosition(1, 0.55); // 70% right section
        splitPane1.getItems().addAll(splitPane,bottombox);
        splitPane1.setOrientation(Orientation.VERTICAL);
        splitPane1.setDividerPosition(0, 0.80);
        articleStage.initStyle(StageStyle.UNDECORATED);

        // Create and set the scene
        Scene scene = new Scene(splitPane1);
        articleStage.setTitle("Нарачка");
        enableMacedonianKeyboard(scene);
        articleStage.setScene(scene);
        articleStage.setMaximized(true);
        articleInputField.requestFocus();
        articleStage.show();// Show the stage first
    }

    private TableView<String[]> createAvailableArticlesTable() {
        tableView = new TableView<>(); // Initialize the tableView here
        // Define columns for ID, Naziv, and Cena
        TableColumn<String[], String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        idColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15)); // 30%

        TableColumn<String[], String> nazivColumn = new TableColumn<>("Артикл");
        nazivColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        nazivColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.60)); // 30%


        TableColumn<String[], String> cenaColumn = new TableColumn<>("Цена");
        cenaColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        cenaColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.18)); // 30%

        tableView.styleProperty().bind(Bindings.concat("-fx-font-size: ", tableView.widthProperty().divide(20).asString(), "px;"));

        tableView.getColumns().addAll(idColumn, nazivColumn, cenaColumn);

        updateAvailableArticlesTable(tableView);
        return tableView;
    }

    // Create the TableView for displaying the order (right side)
    private TableView<String[]> createOrderTable(String nm,int employeeId) {
            TableView<String[]> tableView = new TableView<>();

            TableColumn<String[], String> idColumn = new TableColumn<>("ID");
            idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
            //idColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.10)); // 30%

            TableColumn<String[], String> nazivColumn = new TableColumn<>("Артикл");
            nazivColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
            nazivColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.48)); // 30%

            TableColumn<String[], String> cenaColumn = new TableColumn<>("Цена");
            cenaColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
            cenaColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15)); // 30%

            TableColumn<String[], String> kolicinaColumn = new TableColumn<>("Количина");
            kolicinaColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue()[3])));
            kolicinaColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.10)); // 30%

            TableColumn<String[], String> vremeColumn = new TableColumn<>("Време");
            vremeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[4]));  // Time is in the 5th position
            vremeColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.20)); // 30%

            tableView.styleProperty().bind(Bindings.concat("-fx-font-size: ", tableView.widthProperty().divide(25).asString(), "px;"));

            // Add columns to the TableView
            //tableView.getColumns().addAll(idColumn, nazivColumn, cenaColumn, kolicinaColumn, vremeColumn);
            tableView.getColumns().addAll(nazivColumn, cenaColumn, kolicinaColumn, vremeColumn);

            loadPreviousOrders(nm,employeeId);
            tableView.setItems(AllData);
            return tableView;
    }

    // Create middle section for input fields and buttons
    private VBox createMiddleSection(String tn, int employeeId) {
        VBox middleBox = new VBox(10);
        middleBox.setStyle("-fx-padding: 10;");
        // Get the screen size
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

        // Calculate dynamic width (20% of screen width)
        double middleWidth = screenWidth * 0.40;
        middleBox.setPrefWidth(middleWidth);
        middleBox.setMaxWidth(middleWidth);

        // Labels and input fields
        Label articleLabel = new Label("Внеси назив/шифра за артикл:");
        articleLabel.setMaxWidth(middleWidth * 0.95);  // 95% of middle section width
        articleLabel.setAlignment(Pos.CENTER_LEFT);
        articleLabel.styleProperty().bind(Bindings.concat("-fx-font-weight: bold;;-fx-font-size: ", middleWidth / 50, "px;")); // Dynamic font

        articleInputField.setPromptText("назив/шифра");
        articleInputField.setMaxWidth(middleWidth * 0.95);
        articleInputField.setPrefHeight(screenHeight * 0.05);  // 5% of screen height
        articleInputField.styleProperty().bind(Bindings.concat("-fx-font-size: ", middleWidth / 30, "px;"));

        Label quantityLabel = new Label("Внеси количина:");
        quantityLabel.setMaxWidth(middleWidth * 0.95);
        quantityLabel.setAlignment(Pos.CENTER_LEFT);
        quantityLabel.styleProperty().bind(Bindings.concat("-fx-font-weight: bold;;-fx-font-size: ", middleWidth / 50, "px;"));

        quantityInputField.setPromptText("Количина");
        quantityInputField.setMaxWidth(middleWidth * 0.95);
        quantityInputField.setPrefHeight(screenHeight * 0.06); // 6% of screen height
        quantityInputField.styleProperty().bind(Bindings.concat("-fx-font-size: ", middleWidth / 30, "px;"));

        //buttons
        Button addArticleButton = new Button("Додади");
        Button escButton = new Button("ESC");
        Button smetkaButton = new Button("F1|Фискална");
        Button fakturasmetkaButton = new Button("F2|Фактура");
        Button karticasmetkaButton = new Button("F3|Картичка");
        Button deleteButton = new Button("Delete|Избриши");
        Button transferButton = new Button("F5|Префрли маса");
        Button transferArticleButton = new Button("F4|Префрли артикл");
        Button prefrlikelner = new Button("F10|Префрли келнер");

        // Define button list for easy iteration
        List<Button> buttons = Arrays.asList(
                smetkaButton, fakturasmetkaButton, deleteButton,
                addArticleButton, escButton, transferButton, transferArticleButton,prefrlikelner,karticasmetkaButton
        );

        // Apply styles dynamically
        for (Button button : buttons) {
            button.setMaxWidth(middleWidth * 0.95);
            button.styleProperty().bind(Bindings.concat("-fx-font-size: ", middleWidth / 40, "px;"));
        }

        quantityInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addArticleButton.fire();
            }
            if(event.getCode() == KeyCode.ESCAPE) {
                escButton.fire();
            }
        });

        Platform.runLater(articleInputField::requestFocus);
        setEnterKeyNavigation(articleInputField,quantityInputField);

        // Add listener to update table on search input
        articleInputField.textProperty().addListener((_, _, newValue) -> updateAvailableArticlesSearch(newValue));

        orderTable.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {  // Ensure that an item is selected
                // Move focus to the articleInputField when a row is selected in the orderTable
                Platform.runLater(articleInputField::requestFocus);
            }
        });

        // Handle Enter key in TableView to add the selected article to the order
        tableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String[] selectedArticle = tableView.getSelectionModel().getSelectedItem();
                if (selectedArticle != null) {
                    // Request focus on quantity input field
                    Platform.runLater(quantityInputField::requestFocus);
                    articleInputField.setText(selectedArticle[1]);
                    // Add a listener for when the user presses ENTER in the quantity input field
                    quantityInputField.setOnKeyPressed(quantityEvent -> {
                        if (quantityEvent.getCode() == KeyCode.ENTER) {
                            try {
                                int kolicina = 0;

                                // Check if the input is empty
                                if (quantityInputField.getText().isEmpty()) {
                                    kolicina = 1; // Set default value
                                }
                                if(!quantityInputField.getText().isEmpty()){
                                    // Try parsing the entered text to an integer
                                    kolicina = Integer.parseInt(quantityInputField.getText());

                                    // Check if the number is positive
                                    if (kolicina <= 0) {
                                        showAlert("Мора да биди поголем од 0."); // Show alert for non-positive values
                                        quantityInputField.clear(); // Clear the field for re-entry
                                        return; // Stop further processing
                                    }
                                }

                                String[] articleData = findArticle(articleInputField.getText().trim());
                                if (articleData == null) {
                                    articleInputField.clear();
                                    quantityInputField.clear();
                                    Platform.runLater(articleInputField::requestFocus);
                                    showAlertInformation("Не постои артикл");
                                    return;
                                }
                                // Add the article with the specified or default quantity
                                addToOrder(selectedArticle, kolicina, tn, employeeId);

                                // Clear the input fields and reset focus
                                articleInputField.clear();
                                Platform.runLater(articleInputField::requestFocus);
                                quantityInputField.clear(); // Clear after processing

                            } catch (NumberFormatException e) {
                                // Show an alert if the entered text is not a valid number
                                showAlert("Внеси број.");
                                quantityInputField.clear(); // Clear the field for re-entry
                            }
                        }
                    });
                } else {
                    showAlert("Немате селектирано артикл.");
                }
            }
        });

        addArticleButton.setOnAction(_ -> {
            Platform.runLater(articleInputField::requestFocus);
            if(quantityInputField.getText().isEmpty()){
                quantityInputField.setText("1");
            }
            onAddArticle(tn, employeeId);});

        escButton.setOnAction(_ -> {
            tableGrid.getChildren().clear();
            coll=0;roww=0;
            getTabels();
            loadPreviousOrders(tn, employeeId);
            if(!AllData.isEmpty() && !existingTables.contains(employeeId+":"+tn)) {
                existingTables.add(employeeId + ":" + tn);
                addTableButtonToGrid(tn, coll, roww,employeeId);
                if (coll >= 5) {
                    coll = 0;
                    roww++;
                } else {
                    coll++;
                }
            }
            if(existingTables.contains(employeeId+":"+tn) && AllData.isEmpty()) {
                String btnname = ime + ":" + tn;
                deleteTableButtonFromGrid(btnname);
            }
            if(!orderDataPrintKujna.isEmpty()){
                PrinterService printerService = new PrinterService();
                printerService.printOrder(ime, Integer.parseInt(tn),orderDataPrintKujna,"НАРАЧКА - КУJНА");
                orderDataPrintKujna.clear();
            }
            if(!orderDataPrintShank.isEmpty()){
                PrinterService printerService = new PrinterService();
                printerService.printOrder(ime, Integer.parseInt(tn), orderDataPrintShank, "НАРАЧКА - ШАНК");
                orderDataPrintShank.clear();
            }
            articleStage.close()
            ;});

        // Smetka Button
        smetkaButton.setOnAction(_ -> {
            if (AllData.isEmpty()) {
                // Show an alert if there is no order
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Нема нарачки");
                alert.setHeaderText(null);
                alert.setContentText("Нема нарачки за да се креира сметка.");
                alert.showAndWait();
                return;
            }
            createBill(employeeId,tn,"фискална");
            String btnname = ime + ":" + tn;
            deleteTableButtonFromGrid(btnname);
            smetkaData.clear();
            AllData.clear();
            escButton.fire();
        });
        // Smetka Button
        karticasmetkaButton.setOnAction(_ -> {
            if (AllData.isEmpty()) {
                // Show an alert if there is no order
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Нема нарачки");
                alert.setHeaderText(null);
                alert.setContentText("Нема нарачки за да се креира сметка.");
                alert.showAndWait();
                return;
            }
            createBill(employeeId,tn,"картичка");
            String btnname = ime + ":" + tn;
            deleteTableButtonFromGrid(btnname);
            smetkaData.clear();
            AllData.clear();
            escButton.fire();
        });

        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String[] selectedArticle = tableView.getSelectionModel().getSelectedItem();
                if(selectedArticle != null) {
                    articleInputField.setText(selectedArticle[1]);
                    Platform.runLater(quantityInputField::requestFocus);
                }
            }
        });

        fakturasmetkaButton.setOnAction(_ -> {
            if (AllData.isEmpty()) {
                // Show an alert if there is no order
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Нема нарачки");
                alert.setHeaderText(null);
                alert.setContentText("Нема нарачки за да се креира сметка.");
                alert.showAndWait();
                return;
            }
            createBill(employeeId,tn,"фактура");

            String btnname = ime + ":" + tn;
            deleteTableButtonFromGrid(btnname);
            smetkaData.clear();
            AllData.clear();
            escButton.fire();
        });

        deleteButton.setOnAction(_ -> {
            // Create a confirmation dialog
            if(AllData.isEmpty()) {
                showAlertInformation("Нема нарачки за бришење!");
                return;
            }
            String[] selectedItem = orderTable.getSelectionModel().getSelectedItem();
            if(selectedItem == null) {
                showAlertInformation("Одбери артикл за бришење од нарачка.");
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Избриши артикл");
            alert.setHeaderText("Дали сте сигурни за бришење на артикл од нарачката?\n" + selectedItem[1]);

            // Show the dialog and wait for a response
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    int articleId = Integer.parseInt(selectedItem[0]);
                    int quantity = Integer.parseInt(selectedItem[3]);
                    int orderId;
                    try {
                        orderId = getOrderIdForSelectedItem(employeeId, Integer.parseInt(tn));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        String insertIzbrihaniQuery = "INSERT INTO izbrishani (Masa, VrabotenShifra,artiklID,kolicina) VALUES (?, ?,?,?)";
                        try (PreparedStatement stmt = conn.prepareStatement(insertIzbrihaniQuery, Statement.RETURN_GENERATED_KEYS)) {
                            stmt.setInt(1, Integer.parseInt(tn)); // Masa
                            stmt.setInt(2, employeeId); // VrabotenShifra
                            stmt.setInt(3,articleId);
                            stmt.setInt(4,quantity);
                            stmt.executeUpdate();
                        }
                    } catch (SQLException e) {
                        showAlert(e.getMessage());
                    }
                    int snid;
                    try {
                        snid = getStavkaNarackaId(employeeId,orderId,articleId,quantity);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    deleteOrderFromDatabase(snid,orderId);
                    updateTotalPrice(tn, employeeId);

                    // Remove the selected item from AllData (ObservableList)
                    AllData.remove(selectedItem);

                    // Clone the selected item so we don't modify the original reference
                    String[] deletedItem = selectedItem.clone();

                    // Modify the article name (second element in the array)
                    deletedItem[3] = "-" + deletedItem[3];
                    if(checktipShankArtikl(Integer.parseInt(selectedItem[0]))){
                        //orderDataPrintShank.remove(selectedItem);
                        orderDataPrintShank.add(deletedItem);     // Add the modified version
                    }
                    if(!checktipShankArtikl(Integer.parseInt(selectedItem[0]))){
                        //orderDataPrintKujna.remove(selectedItem);
                        orderDataPrintKujna.add(deletedItem);     // Add the modified version
                    }
                    orderData.remove(selectedItem);

                    loadPreviousOrders(tn, employeeId);
                    if(AllData.isEmpty()) {
                        escButton.fire();
                    }
                    Platform.runLater(articleInputField::requestFocus);
                } else {
                    // If the user clicks "Cancel", do nothing
                    System.out.println("Откажано бришење.");
                }
            });
        });

        //Префрли маса button
        //transferButton.setAlignment(Pos.CENTER_LEFT);
        transferButton.setOnAction(_ -> {
            if(AllData.isEmpty()){
                showAlertInformation("Нема нарачки за да се префрлат!");
                return;
            }
            // Show input dialog to ask for the target table number
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Префрли маса");
            dialog.setHeaderText(null);
            dialog.setContentText("Внеси број на нова маса:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newTableNumber -> {
                try {
                    int targetTable = Integer.parseInt(newTableNumber);
                    if (targetTable <= 0) {
                        showAlert("Внесете валиден број за маса.");
                        return;
                    }
                    if(targetTable == Integer.parseInt(tn)) {
                        showAlert("Внесовте иста маса!");
                        return;
                    }
                    // Transfer orders in the database
                    transferOrdersInDatabase(Integer.parseInt(tn), targetTable, employeeId);

                    AllData.clear(); // Clear orders for the current table
                    orderData.clear();
                    escButton.fire();
                } catch (NumberFormatException e) {
                    showAlert("Внесете валиден број за маса.");
                } catch (SQLException e) {
                    showAlert("Настана грешка при трансферот на нарачките.\n" +e.getMessage());
                }
            });
        });

        //префрли артикл button
        //transferArticleButton.setAlignment(Pos.CENTER_LEFT);
        transferArticleButton.setOnAction(_ -> {
            if (AllData.isEmpty()) {
                showAlertInformation("Нема нарачки за префрлање!");
                return;
            }
            // Get selected item from the order table
            String[] selectedItem = orderTable.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                showAlertInformation("Одбери артикл за префрлање.");
                return;
            }

            // Show input dialog to ask for the target table number
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Префрли артикл");
            dialog.setHeaderText(null);
            dialog.setContentText("Внеси број на нова маса:");

            dialog.showAndWait().ifPresent(newTableNumber -> {
                try {
                    int targetTable = Integer.parseInt(newTableNumber);
                    if (targetTable <= 0) {
                        showAlert("Внесете валиден број за маса.");
                        return;
                    }
                    if(targetTable == Integer.parseInt(tn)) {
                        showAlert("Внесовте иста маса!");
                        return;
                    }

                    // Show confirmation dialog
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Потврда за префрлување");
                    alert.setHeaderText("Дали сте сигурни дека сакате да префрлите " + selectedItem[1] + " на маса " + targetTable + "?");
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            // Extract article details from the selected row
                            int articleId = Integer.parseInt(selectedItem[0]); // Assuming ID is in the first column
                            int quantity = Integer.parseInt(selectedItem[3]); // Assuming Quantity is in the last column

                            // Get the current order ID
                            int currentOrderId;
                            try {
                                currentOrderId = getOrderIdForSelectedItem(employeeId, Integer.parseInt(tn));
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }

                            // Transfer the article to the target table
                            try {
                                int stavkaNarackaId = getStavkaNarackaId(employeeId, currentOrderId, articleId, quantity);

                                transferArticleInDatabase(articleId, quantity, currentOrderId, targetTable, employeeId,stavkaNarackaId);

                                // Remove the transferred item from the UI
                                AllData.remove(selectedItem);
                                orderData.remove(selectedItem);
                                loadPreviousOrders(tn, employeeId);

                                // Check if AllData is empty after the transfer
                                if (AllData.isEmpty()) {
                                    // Free the table by deleting the order
                                    freeTableInDatabase(Integer.parseInt(tn), employeeId);
                                    escButton.fire();
                                }
                            } catch (SQLException e) {
                                showAlert("Настана грешка при префрлувањето на артиклот.\n"+e.getMessage());
                            }
                        } else {
                            System.out.println("Откажано префрлување.");
                        }
                    });
                } catch (NumberFormatException e) {
                    showAlert("Внесете валиден број за маса.");
                }
            });
        });

        prefrlikelner.setOnAction(_ -> {
            if (AllData.isEmpty()) {
                showAlertInformation("Нема нарачки за префрлање!");
                return;
            }
            boolean validinput = false;
            while (!validinput) {
                // Ask for the new vrabotenshifra
                TextInputDialog inputDialog = new TextInputDialog();
                inputDialog.setTitle("Внесете нова шифра");
                inputDialog.setHeaderText("Шифра за префрлување маса на вработен");
                inputDialog.setContentText("Шифра:");
                setMaxInputLength(inputDialog.getEditor(), 3);

                Optional<String> newShifraOpt = inputDialog.showAndWait();
                if (newShifraOpt.isPresent()) {
                    String newShifra = newShifraOpt.get();
                    try {
                        int shifra = Integer.parseInt(newShifra);
                        if (shifra < 0) {
                            showAlertInformation("Обиди се повторно");
                            continue;
                        }
                        if(shifra == employeeId){
                            showAlert("Внесовте иста шифра!");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        showAlertInformation("Шифрата мора да е број!");
                        continue;
                    }
                    if (!newShifra.trim().isEmpty()) {
                        // Run the queries
                        if(employeeId == Integer.parseInt(newShifra)) {
                            showAlert("Шифрата е иста!");
                            continue;
                        }
                        if (checkPassword(newShifra)) {
                            updateVrabotenShifra(employeeId,Integer.parseInt(tn), Integer.parseInt(newShifra));
                            AllData.clear(); // Clear orders for the current table
                            orderData.clear();
                            escButton.fire();
                            validinput = true;
                        } else {
                            showAlertInformation("Погрешна шифра.Обиди се повторно");
                        }
                    } else {
                        showAlert("Шифрата не може да биде празна.");
                    }
                }else{
                    validinput = true;
                }
            }
        });

        // Handle Enter key in articleInputField to move focus to TableView
        articleInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {tableView.requestFocus();}
            if(event.getCode() == KeyCode.ESCAPE) {escButton.fire();}
            if(event.getCode() == KeyCode.F1) {smetkaButton.fire();}
            if(event.getCode() == KeyCode.F2) {fakturasmetkaButton.fire();}
            if(event.getCode() == KeyCode.DELETE) {
                String[] selectedItem = orderTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {deleteButton.fire();}
            }
            if(event.getCode() == KeyCode.F3) {karticasmetkaButton.fire();}
            if(event.getCode() == KeyCode.F4) {transferArticleButton.fire();}
            if(event.getCode() == KeyCode.F5) {transferButton.fire();}
            if(event.getCode() == KeyCode.F10) {prefrlikelner.fire();}
        });

        HBox bottomButtonsBox2 = new HBox(20);  // 20px spacing between buttons
        bottomButtonsBox2.setAlignment(Pos.BASELINE_LEFT);  // Align buttons at the center
        transferArticleButton.prefWidthProperty().bind(bottomButtonsBox2.widthProperty().divide(2));
        transferButton.prefWidthProperty().bind(bottomButtonsBox2.widthProperty().divide(2));
        bottomButtonsBox2.getChildren().addAll(transferArticleButton,transferButton);

        HBox bottomButtonsBox3 = new HBox(20);  // 20px spacing between buttons
        bottomButtonsBox3.setAlignment(Pos.BASELINE_LEFT);  // Align buttons at the center
        prefrlikelner.prefWidthProperty().bind(bottomButtonsBox3.widthProperty().divide(2));
        deleteButton.prefWidthProperty().bind(bottomButtonsBox3.widthProperty().divide(2));
        bottomButtonsBox3.getChildren().addAll(prefrlikelner,deleteButton);

        HBox bottomButtonsBox4 = new HBox(20);  // 20px spacing between buttons
        bottomButtonsBox4.setAlignment(Pos.BASELINE_LEFT);  // Align buttons at the center
        addArticleButton.prefWidthProperty().bind(bottomButtonsBox4.widthProperty().divide(2));
        escButton.prefWidthProperty().bind(bottomButtonsBox4.widthProperty().divide(2));
        bottomButtonsBox4.getChildren().addAll(addArticleButton,escButton);

        Platform.runLater(articleInputField::requestFocus);
        // Bottom button layout using HBox
        HBox bottomButtonsBox = new HBox(20);  // 20px spacing between buttons
        bottomButtonsBox.setAlignment(Pos.BASELINE_LEFT);  // Align buttons at the center
        smetkaButton.prefWidthProperty().bind(bottomButtonsBox.widthProperty().divide(3));
        fakturasmetkaButton.prefWidthProperty().bind(bottomButtonsBox.widthProperty().divide(3));
        karticasmetkaButton.prefWidthProperty().bind(bottomButtonsBox.widthProperty().divide(3));
        // Add buttons to the bottom section
        bottomButtonsBox.getChildren().addAll(smetkaButton,fakturasmetkaButton,karticasmetkaButton);

        VBox separator = new VBox(20);
        separator.setAlignment(Pos.BASELINE_LEFT);
        separator.setPrefHeight(screenHeight*0.15);

        // Add all components to the VBox
        middleBox.getChildren().addAll(articleLabel, articleInputField, quantityLabel, quantityInputField, bottomButtonsBox4,separator, bottomButtonsBox,bottomButtonsBox2,bottomButtonsBox3);

        return middleBox;
    }


    private int getStavkaNarackaId(int employeeId, int narackaId, int artiklId, int kolicina) throws SQLException {
        String query = "SELECT sn.id FROM StavkaNaracka AS sn " +
                "INNER JOIN Naracka N ON sn.NarackaId = N.Id " +
                "WHERE VrabotenShifra = ? AND NarackaId = ? AND ArtiklId = ? AND Kolicina = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, employeeId);
            statement.setInt(2, narackaId);
            statement.setInt(3, artiklId);
            statement.setInt(4, kolicina);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            } else {
                throw new SQLException("No matching item found in the order.");
            }
        }
    }

    private void createBill(int employeeId,String tn,String tipSmetka){
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            // Calculate the total price
            int totalPrice = 0;
            String imevraboten=ime;
            for (String[] row : AllData) {
                int articleId = Integer.parseInt(row[0]);   // Assuming ID is in the first column
                int quantity = Integer.parseInt(row[3]);    // Assuming Quantity is in the fourth column

                // Get the price for each item (e.g., from the database)
                try (PreparedStatement stmt = conn.prepareStatement("SELECT Cena FROM Artikl WHERE Id = ?")) {
                    stmt.setInt(1, articleId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            int price = rs.getInt("Cena");
                            totalPrice += price * quantity;
                        }
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement("select ime from vraboten where shifra = ?")) {
                    stmt.setInt(1, employeeId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            imevraboten = rs.getString("ime");
                        }
                    }
                }
            }

            // Insert the bill (Smetka)
            int smetkaId;
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Smetka (VrabotenShifra, Masa, Vkupno,vrabotenime) VALUES (?, ?, ?,?) RETURNING Id")) {
                stmt.setInt(1, employeeId);  // The employee ID
                stmt.setInt(2, Integer.parseInt(tn));  // The table number (tn)
                stmt.setInt(3, totalPrice);
                stmt.setString(4,imevraboten);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        smetkaId = rs.getInt("Id");
                    } else {
                        throw new SQLException("Failed to insert Smetka.");
                    }
                }
            }

            // Insert items into StavkaNaSmetka
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO StavkaNaSmetka (SmetkaId, ArtiklId, Kolicina, Cena,vreme) VALUES (?, ?, ?, ?,?)")) {
                for (String[] row : AllData) {
                    int articleId = Integer.parseInt(row[0]);  // Assuming ID is in the first column
                    int quantity = Integer.parseInt(row[3]);   // Assuming Quantity is in the fourth column
                    String vreme = row[4];
                    if(vreme.length() == 5){
                        vreme += ":00";
                    }

                    // Get the price for each item
                    double price;
                    try (PreparedStatement priceStmt = conn.prepareStatement("SELECT Cena FROM Artikl WHERE Id = ?")) {
                        priceStmt.setInt(1, articleId);
                        try (ResultSet rs = priceStmt.executeQuery()) {
                            if (rs.next()) {
                                price = rs.getDouble("Cena");
                            } else {
                                throw new SQLException("Артикл со шифра " + articleId + " не постои.");
                            }
                        }
                    }

                    // Insert each item into StavkaNaSmetka
                    stmt.setInt(1, smetkaId);
                    stmt.setInt(2, articleId);
                    stmt.setInt(3, quantity);
                    stmt.setDouble(4, price);
                    stmt.setTime(5, Time.valueOf(vreme));
                    stmt.executeUpdate();
                }
            }
            try (PreparedStatement updatetip = conn.prepareStatement("update smetka set tip = ? where smetka.id = ?;")) {
                updatetip.setString(1, tipSmetka);
                updatetip.setInt(2, smetkaId);
                updatetip.executeUpdate();
            }

            // Delete the order (Naracka)
            try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM Naracka WHERE Masa = ? AND VrabotenShifra = ?")) {
                deleteStmt.setInt(1, Integer.parseInt(tn));
                deleteStmt.setInt(2, employeeId);
                deleteStmt.executeUpdate();
            }

            // Commit the transaction
            //conn.commit();
            double ddv18 = 0.0;
            double ddv10 = 0.0;
            double ddv5 = 0.0;
            double ddvValue = 0.0;

            try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT a.ddv, SUM(a.ddv_value) AS sumaDDV
                FROM smetka AS s
                INNER JOIN public.stavkanasmetka s2 ON s.id = s2.smetkaid
                INNER JOIN public.artikl a ON s2.artiklid = a.id
                WHERE smetkaid = ?
                GROUP BY a.ddv;
                """)) {

                stmt.setInt(1, smetkaId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ddvValue = rs.getDouble("sumaDDV");  // Get the sum of DDV
                        int ddvType = rs.getInt("ddv");  // Get the DDV percentage

                        switch (ddvType) {
                            case 18 -> ddv18 = ddvValue;
                            case 10 -> ddv10 = ddvValue;
                            case 5 -> ddv5 = ddvValue;
                        }
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement("""
                        select A.Naziv as artikli,sum(SNS.Kolicina) as Kolicina,MAX(A.Cena) as cena_Artikl,sum(SNS.Kolicina*A.Cena) as vkupno
                        from Smetka as s
                        inner join StavkaNaSmetka SNS on s.Id = SNS.SmetkaId
                        inner join vraboten as v on s.VrabotenShifra = v.Shifra
                        inner join Artikl A on SNS.ArtiklId = A.Id
                        where VrabotenShifra= ? and Masa= ? and SmetkaId= ?
                        group by A.naziv;""")) {
                stmt.setInt(1, employeeId);  // The employee ID
                stmt.setInt(2, Integer.parseInt(tn));  // The table number (tn)
                stmt.setInt(3, smetkaId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String naziv = rs.getString("artikli");
                        int kolicina = rs.getInt("Kolicina");
                        int cena = rs.getInt("cena_artikl");
                        int vkupno = rs.getInt("vkupno");

                        // Add the data to the ObservableList
                        smetkaData.add(new String[]{
                                naziv,                     // Article Name
                                String.valueOf(kolicina),  // Quantity
                                String.valueOf(cena),      // Price
                                String.valueOf(vkupno)     // Total Price per Article
                        });
                    }
                }
            }
            BillPrinter billPrinter = new BillPrinter();
            billPrinter.printBill(imevraboten, smetkaData, ddv18, ddv10, ddv5 , totalPrice);

            conn.commit();

        } catch (SQLException ex) {
            System.out.println("MiddleSectioin sql exception" + ex);
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.rollback();  // Rollback transaction on failure
            } catch (SQLException rollbackEx) {
                showAlert(rollbackEx.getMessage());
            }

            // Show an error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Грешка додека се правеше сметката. Обиди се уште еднаш.");
            alert.showAndWait();
        }
    }
    private void admincreateBill(int smetkaId,int employeeId){
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            double ddv18 = 0.0;
            double ddv10 = 0.0;
            double ddv5 = 0.0;
            double ddvValue = 0.0;

            try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT a.ddv, SUM(a.ddv_value) AS sumaDDV
                FROM smetka AS s
                INNER JOIN public.stavkanasmetka s2 ON s.id = s2.smetkaid
                INNER JOIN public.artikl a ON s2.artiklid = a.id
                WHERE smetkaid = ?
                GROUP BY a.ddv;
                """)) {

                stmt.setInt(1, smetkaId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ddvValue = rs.getDouble("sumaDDV");  // Get the sum of DDV
                        int ddvType = rs.getInt("ddv");  // Get the DDV percentage

                        switch (ddvType) {
                            case 18 -> ddv18 = ddvValue;
                            case 10 -> ddv10 = ddvValue;
                            case 5 -> ddv5 = ddvValue;
                        }
                    }
                }
            }

            String imevraboten="";
            try (PreparedStatement stmt = conn.prepareStatement("""
                        select ime from vraboten where shifra = ?
                        """)) {
                stmt.setInt(1, employeeId);  // The employee ID

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        imevraboten = rs.getString("ime");
                    }
                }
            }
            int totalPrice=0;
            try (PreparedStatement stmt = conn.prepareStatement("""
                        select A.Naziv as artikli,sum(SNS.Kolicina) as Kolicina,MAX(A.Cena) as cena_Artikl,sum(SNS.Kolicina*A.Cena) as vkupno
                        from Smetka as s
                        inner join StavkaNaSmetka SNS on s.Id = SNS.SmetkaId
                        inner join vraboten as v on s.VrabotenShifra = v.Shifra
                        inner join Artikl A on SNS.ArtiklId = A.Id
                        where VrabotenShifra= ? and SmetkaId= ?
                        group by A.naziv;""")) {
                stmt.setInt(1, employeeId);  // The employee ID
                stmt.setInt(2, smetkaId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String naziv = rs.getString("artikli");
                        int kolicina = rs.getInt("Kolicina");
                        int cena = rs.getInt("cena_artikl");
                        int vkupno = rs.getInt("vkupno");
                        totalPrice+=vkupno;

                        // Add the data to the ObservableList
                        smetkaData.add(new String[]{
                                naziv,                     // Article Name
                                String.valueOf(kolicina),  // Quantity
                                String.valueOf(cena),      // Price
                                String.valueOf(vkupno)     // Total Price per Article
                        });
                    }
                }
            }
            BillPrinter billPrinter = new BillPrinter();
            billPrinter.printBill(imevraboten, smetkaData, ddv18, ddv10, ddv5 , totalPrice);

            conn.commit();

        } catch (SQLException ex) {
            System.out.println("admincreateBILL sql exception" + ex);
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.rollback();  // Rollback transaction on failure
            } catch (SQLException rollbackEx) {
                showAlert(rollbackEx.getMessage());
            }

            // Show an error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Грешка додека се правеше сметката. Обиди се уште еднаш.");
            alert.showAndWait();
        }
    }

    private void updateVrabotenShifra(int currentVrabotenShifra, int masa, int newVrabotenShifra) {
        String querySelectExistingOrder = """
        SELECT Id FROM Naracka
        WHERE Masa = ? AND VrabotenShifra = ? AND Status = 'active';
    """;
        String queryMergeStavka = """
        UPDATE StavkaNaracka
        SET NarackaId = ?
        WHERE NarackaId = ?;
    """;
        String queryDeleteOrder = """
        DELETE FROM Naracka
        WHERE Id = ?;
    """;
        String queryUpdateVraboten = """
        UPDATE Naracka
        SET VrabotenShifra = ?
        WHERE Id = ?;
    """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            int existingOrderId = -1;
            int currentOrderId = -1;

            // Step 1: Get the current order ID
            try (PreparedStatement psSelect = conn.prepareStatement(querySelectExistingOrder)) {
                psSelect.setInt(1, masa);
                psSelect.setInt(2, currentVrabotenShifra);
                ResultSet rs = psSelect.executeQuery();
                if (rs.next()) {
                    currentOrderId = rs.getInt("Id");
                }
            }

            // Step 2: Check if there's an existing order for the new employee
            try (PreparedStatement psSelect = conn.prepareStatement(querySelectExistingOrder)) {
                psSelect.setInt(1, masa);
                psSelect.setInt(2, newVrabotenShifra);
                ResultSet rs = psSelect.executeQuery();
                if (rs.next()) {
                    existingOrderId = rs.getInt("Id");
                }
            }

            if (existingOrderId != -1) {
                // Step 3: Merge items into the existing order
                try (PreparedStatement psMerge = conn.prepareStatement(queryMergeStavka)) {
                    psMerge.setInt(1, existingOrderId);
                    psMerge.setInt(2, currentOrderId);
                    psMerge.executeUpdate();
                }

                // Step 4: Delete the redundant order
                try (PreparedStatement psDelete = conn.prepareStatement(queryDeleteOrder)) {
                    psDelete.setInt(1, currentOrderId);
                    psDelete.executeUpdate();
                }
            } else {
                // Step 5: Update the employee ID if no conflict exists
                try (PreparedStatement psUpdate = conn.prepareStatement(queryUpdateVraboten)) {
                    psUpdate.setInt(1, newVrabotenShifra);
                    psUpdate.setInt(2, currentOrderId);
                    psUpdate.executeUpdate();
                }
            }

            conn.commit();
            showAlertInformation("Успешно ажурирана шифра на вработен.");
        } catch (Exception e) {
            showAlert("Грешка при ажурирање на шифрата на вработен.\n" + e.getMessage());
        }
    }


    private void freeTableInDatabase(int tableNumber, int employeeId) throws SQLException {
        String deleteSourceOrderQuery = "DELETE FROM Naracka WHERE Masa = ? AND VrabotenShifra = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSourceOrderQuery)) {
            stmt.setInt(1, tableNumber);
            stmt.setInt(2, employeeId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new SQLException("Error freeing the table", ex);
        }
    }

    private void transferArticleInDatabase(int articleId, int quantity, int currentOrderId, int targetTable, int employeeId,int snid) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            int targetOrderId;

            // Step 1: Check if an order exists for the target table
            String checkOrderQuery = "SELECT Id FROM Naracka WHERE Masa = ? AND VrabotenShifra = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(checkOrderQuery)) {
                stmt.setInt(1, targetTable);
                stmt.setInt(2, employeeId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        targetOrderId = rs.getInt("Id");
                    } else {
                        // Create a new order for the target table
                        String createOrderQuery = "INSERT INTO Naracka (Masa, VrabotenShifra) VALUES (?, ?) RETURNING Id;";
                        try (PreparedStatement insertStmt = conn.prepareStatement(createOrderQuery)) {
                            insertStmt.setInt(1, targetTable);
                            insertStmt.setInt(2, employeeId);
                            try (ResultSet insertRs = insertStmt.executeQuery()) {
                                if (insertRs.next()) {
                                    targetOrderId = insertRs.getInt("Id");
                                } else {
                                    throw new SQLException("Failed to create order for target table.");
                                }
                            }
                        }
                    }
                }
            }

            // Step 2: Add the article to the target order
            String transferArticleQuery = """
            INSERT INTO StavkaNaracka (NarackaId, ArtiklId, Kolicina, Cena,vreme)
            SELECT ?, ArtiklId, ?, Cena,vreme
            FROM StavkaNaracka
            WHERE NarackaId = ? AND ArtiklId = ? and id = ?;
        """;
            try (PreparedStatement stmt = conn.prepareStatement(transferArticleQuery)) {
                stmt.setInt(1, targetOrderId);
                stmt.setInt(2, quantity);
                stmt.setInt(3, currentOrderId);
                stmt.setInt(4, articleId);
                stmt.setInt(5, snid);
                stmt.executeUpdate();
            }

            // Step 3: Remove the article from the current order
            String deleteArticleQuery = """
            DELETE FROM StavkaNaracka
            WHERE NarackaId = ? AND ArtiklId = ? and id = ?;
        """;
            try (PreparedStatement stmt = conn.prepareStatement(deleteArticleQuery)) {
                stmt.setInt(1, currentOrderId);
                stmt.setInt(2, articleId);
                stmt.setInt(3, snid);
                stmt.executeUpdate();
            }

            conn.commit(); // Commit transaction
        } catch (SQLException ex) {
            throw new SQLException("Error transferring article", ex);
        }
    }


    private void transferOrdersInDatabase(int currentTable, int targetTable, int employeeId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            int targetOrderId;

            // Step 1: Check if an order exists for the target table
            String checkOrderQuery = "SELECT Id FROM Naracka WHERE Masa = ? AND VrabotenShifra = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(checkOrderQuery)) {
                stmt.setInt(1, targetTable);
                stmt.setInt(2, employeeId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        targetOrderId = rs.getInt("Id");
                    } else {
                        // Create a new order for the target table
                        String createOrderQuery = "INSERT INTO Naracka (Masa, VrabotenShifra) VALUES (?, ?) RETURNING Id;";
                        try (PreparedStatement insertStmt = conn.prepareStatement(createOrderQuery)) {
                            insertStmt.setInt(1, targetTable);
                            insertStmt.setInt(2, employeeId);
                            try (ResultSet insertRs = insertStmt.executeQuery()) {
                                if (insertRs.next()) {
                                    targetOrderId = insertRs.getInt("Id");
                                } else {
                                    throw new SQLException("Failed to create order for target table.");
                                }
                            }
                        }
                    }
                }
            }

            // Step 2: Transfer items from the source table to the target table
            String transferItemsQuery = """
            INSERT INTO StavkaNaracka (NarackaId, ArtiklId, Kolicina, Cena,vreme)
            SELECT ?, ArtiklId, Kolicina, Cena,vreme
            FROM StavkaNaracka
            WHERE NarackaId = (SELECT Id FROM Naracka WHERE Masa = ? AND VrabotenShifra = ?);
        """;
            try (PreparedStatement stmt = conn.prepareStatement(transferItemsQuery)) {
                stmt.setInt(1, targetOrderId);
                stmt.setInt(2, currentTable);
                stmt.setInt(3, employeeId);
                stmt.executeUpdate();
            }

            // Step 3: Delete the source order and its items
            String deleteSourceOrderQuery = "DELETE FROM Naracka WHERE Masa = ? AND VrabotenShifra = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSourceOrderQuery)) {
                stmt.setInt(1, currentTable);
                stmt.setInt(2, employeeId);
                stmt.executeUpdate();
            }

            conn.commit(); // Commit transaction
        } catch (SQLException ex) {
            throw new SQLException("Error transferring orders", ex);
        }
    }
    // Handle adding article to the order from the input fields
    private void onAddArticle(String tn, int employeeId) {
        String articleInput = articleInputField.getText().trim();
        String quantityInput = quantityInputField.getText().trim();

        if (articleInput.isEmpty() || quantityInput.isEmpty()) {
            showAlert("Внеси артикл и количина!");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityInput);
            if (quantity <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showAlert("Количина мора да биди поголемо од 0.");
            return;
        }

        // Search for article by ID or name
        String[] articleData = findArticle(articleInput);

        if (articleData != null) {
            addToOrder(articleData, quantity, tn, employeeId);
        } else {
            showAlert("Не е пронајден артикл.");
        }
        // Clear input fields
        articleInputField.clear();
        quantityInputField.clear();
    }

    // Find article by ID or name
    static String[] findArticle(String input) {
        for (Map<String, Object> article : articls) {
            String id = article.get("id").toString();
            String naziv = article.get("naziv").toString();
            if (id.equals(input) || naziv.equalsIgnoreCase(input)) {
                String[] articleData = new String[3];
                articleData[0] = id;
                articleData[1] = naziv;
                articleData[2] = article.get("cena").toString();
                return articleData;
            }
        }
        return null;
    }

     //Add article to the order
    private void addToOrder(String[] selectedArticle, int quantity, String tn, int employeeId) {
        String[] orderItem = new String[5];
        orderItem[0] = selectedArticle[0];
        orderItem[1] = selectedArticle[1];
        orderItem[2] = selectedArticle[2]; // Keep as String
        orderItem[3] = String.valueOf(quantity);
        orderItem[4] = getCurrentTime();

        orderData.add(orderItem);

        if(checktipShankArtikl(Integer.parseInt(selectedArticle[0]))) {
            orderDataPrintShank.add(orderItem);
        }
        if(!checktipShankArtikl(Integer.parseInt(selectedArticle[0]))) {
            orderDataPrintKujna.add(orderItem);
        }

        tableOrders.computeIfAbsent(tn, _ -> new ArrayList<>()).add(orderItem);

        orderData.remove(orderItem);
        AllData.clear(); // Clear before adding the new data
        AllData.addAll(tableOrders.get(tn));

        saveOrderToDatabase(tn, employeeId, Integer.parseInt(selectedArticle[0]), quantity);
        updateTotalPrice(tn, employeeId);
    }
    // Method to get the current time and return it as a String
    private String getCurrentTime() {
        LocalTime now = LocalTime.now();  // Get the current time
        return now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));  // Format it as HH:mm
    }
    private boolean checktipShankArtikl(int idArtikl) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                     select tip from artikl where id = ?;
                    """)) {

            stmt.setInt(1,idArtikl);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String tip = rs.getString("tip");
                    if(tip.equals("shank")){
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("getTotalPrice sql exception" + e);
        }
        return false;
    }

    private int getTotalPrice(String tn,int employeeId) {
        int vk_cena = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                     select sum(cena*kolicina) as sum
                     from
                     (SELECT a.Cena, sn.Kolicina
                     FROM StavkaNaracka as sn
                     JOIN Naracka as n ON sn.NarackaId = n.Id
                     JOIN Artikl as a ON sn.ArtiklId = a.Id
                     WHERE n.Masa = ? AND n.VrabotenShifra = ? AND n.Status = 'active') as zemi_cena""")) {

            stmt.setInt(1, Integer.parseInt(tn));
            stmt.setInt(2, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vk_cena = rs.getInt("sum");
                }
            }
        } catch (SQLException e) {
            System.out.println("getTotalPrice sql exception" + e);
        }
        return vk_cena;
    }

    private void updateTotalPrice(String tn,int employeeId) {
        int vk_cena = getTotalPrice(tn, employeeId);
        totalPriceLabel.setText("Вкупно: " + vk_cena);
    }


    // Show alert if something goes wrong
    static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadPreviousOrders(String tableNumber, int employeeId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                     select a.Id,a.Naziv,a.cena,sn.Kolicina,TO_CHAR(sn.vreme,'HH24:MI:SS') as vreme,sn.id as snid
                     From stavkanaracka sn
                     inner join Naracka N on sn.NarackaId = N.Id
                     inner join Artikl A on A.Id = sn.ArtiklId
                     where n.masa = ? and n.vrabotenshifra = ? and n.Status = 'active';
                     """)) {

            stmt.setInt(1, Integer.parseInt(tableNumber));
            stmt.setInt(2, employeeId);

            try (ResultSet rs = stmt.executeQuery()) {
                // Clear AllData and tableOrders to avoid duplicate data
                AllData.clear();
                tableOrders.computeIfAbsent(tableNumber, _ -> new ArrayList<>()).clear();

                while (rs.next()) {
                    String[] orderItem = new String[6];
                    orderItem[0] = String.valueOf(rs.getInt("Id"));
                    orderItem[1] = rs.getString("Naziv");
                    orderItem[2] = String.valueOf(rs.getBigDecimal("Cena"));
                    orderItem[3] = String.valueOf(rs.getInt("Kolicina"));
                    orderItem[4] = String.valueOf(rs.getTime("vreme"));
                    orderItem[5] = String.valueOf(rs.getInt("snid"));

                    // Add the order item to the tableOrders and AllData
                    tableOrders.computeIfAbsent(tableNumber, _ -> new ArrayList<>()).add(orderItem);
                    AllData.add(orderItem);  // Add to AllData for the table
                }
            }
        } catch (SQLException e) {
            System.out.println("loadPreviousOrders sql exception" + e);
        }

        // Update the total price after loading previous orders
        updateTotalPrice(tableNumber, employeeId);

        // Refresh the TableView with the updated data
        if (orderTable != null) {
            orderTable.setItems(AllData);
            orderTable.refresh();
        } else {
            System.out.println("orderTable is null, ensure it's properly initialized.");
        }
    }

    private int getOrderIdForSelectedItem(int employeeId, int tableNumber) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT n.id FROM StavkaNaracka sn " +
                    "JOIN Naracka n ON sn.NarackaId = n.Id " +
                    "JOIN Artikl a ON sn.ArtiklId = a.Id " +
                    "WHERE n.Masa = ? AND n.VrabotenShifra = ? AND n.Status = 'active';";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                // Set the parameters for the query
                stmt.setInt(1, tableNumber);
                stmt.setInt(2, employeeId);

                // Execute the query to get the result set
                ResultSet rs = stmt.executeQuery();

                // Check if the result set has a valid entry
                if (rs.next()) {
                    return rs.getInt("id");  // Return the order ID
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving order ID: " + e);
        }
        return -1;  // Return -1 if no valid order ID is found
    }


    private void deleteOrderFromDatabase(int snid, int orderId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // First, delete the specific order item from StavkaNaracka table
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM StavkaNaracka WHERE id = ?")) {
                stmt.setInt(1, snid);
                stmt.executeUpdate();
            }

            // After deleting the item, check if the order is empty
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM StavkaNaracka WHERE NarackaId = ?")) {
                stmt.setInt(1, orderId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    // If the order has no more items, delete the order from Naracka table
                    try (PreparedStatement deleteOrderStmt = conn.prepareStatement(
                            "DELETE FROM Naracka WHERE Id = ?")) {
                        deleteOrderStmt.setInt(1, orderId);
                        deleteOrderStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("deleteOrderFromDatabase sql exception: " + e);
        }
    }

    private void saveOrderToDatabase(String tableNumber, int employeeId, int articleId, int quantity) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            int orderId = getOrCreateOrderId(conn, tableNumber, employeeId);
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO StavkaNaracka (NarackaId, ArtiklId, Kolicina, Cena,vreme) VALUES (?, ?, ?, (SELECT Cena FROM Artikl WHERE Id = ?),?)")) {
                stmt.setInt(1, orderId);
                stmt.setInt(2, articleId);
                stmt.setInt(3, quantity);
                stmt.setInt(4, articleId);
                stmt.setTime(5, Time.valueOf(LocalTime.now()));
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("saveOrderToDatabase sql exception" + e);
        }
    }
    private void changeTipSmetka(int smetkaID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "update smetka set tip = 'фискална' where id = ?;")) {
                stmt.setInt(1, smetkaID);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("changeTipSmetka sql exception" + e);
        }
    }

        private int getOrCreateOrderId(Connection conn, String tableNumber, int employeeId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT Id FROM Naracka WHERE Masa = ? AND VrabotenShifra = ? AND Status = 'active'")) {
            stmt.setInt(1, Integer.parseInt(tableNumber));
            stmt.setInt(2, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Id");
                } else {
                    try (PreparedStatement insertStmt = conn.prepareStatement(
                            "INSERT INTO Naracka (Masa, VrabotenShifra) VALUES (?, ?) RETURNING Id")) {
                        insertStmt.setInt(1, Integer.parseInt(tableNumber));
                        insertStmt.setInt(2, employeeId);
                        try (ResultSet generatedKeys = insertStmt.executeQuery()) {
                            if (generatedKeys.next()) {
                                return generatedKeys.getInt("Id");
                            } else {
                                throw new SQLException("Creating order failed, no ID obtained.");
                            }
                        }
                    }
                }
            }
        }
    }

    static void updateAvailableArticlesSearch(String searchText) {
        ObservableList<String[]> filteredArticles = FXCollections.observableArrayList();

        for (Map<String, Object> article : articls) {
            String id = article.get("id").toString().toLowerCase();
            String naziv = article.get("naziv").toString().toLowerCase();

            if (searchText == null || searchText.isEmpty() ||
                    id.contains(searchText.toLowerCase()) || naziv.contains(searchText.toLowerCase())) {
                String[] articleData = new String[3];
                articleData[0] = article.get("id").toString();
                articleData[1] = article.get("naziv").toString();
                articleData[2] = article.get("cena").toString();

                filteredArticles.add(articleData);
            }
        }

        // Update the TableView with filtered data
        tableView.setItems(filteredArticles);
    }

    // Update the available articles table with data from the articls Set
    static void updateAvailableArticlesTable(TableView<String[]> tableView) {
        ObservableList<String[]> articles = FXCollections.observableArrayList();

        // Loop through the articles and add them to the ObservableList
        for (Map<String, Object> article : articls) {
            String[] articleData = new String[3];
            articleData[0] = article.get("id").toString();
            articleData[1] = article.get("naziv").toString();
            articleData[2] = article.get("cena").toString();

            articles.add(articleData);
        }
        // Set the data to the table view
        tableView.setItems(articles);
    }

    // Add new table button to the grid
    private void addTableButtonToGrid(String tableNumber, int col, int row,int employeeid) {
        Button newTableButton = new Button(tableNumber);
        // Get screen size
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
        double btnwidth = screenWidth * 0.70;

        newTableButton.setPrefSize(btnwidth/6, screenHeight * 0.06);// Set size for the new table button
        newTableButton.styleProperty().bind(Bindings.concat("-fx-border-color: black;-fx-font-size: ", screenWidth / 60, "px;"));
        String username = ime;
        String btnname=username+":"+tableNumber;
        newTableButton.setText(username + ":" + tableNumber);

        existingTables.add(btnname);

        // Add the new button to the grid
        tableGrid.add(newTableButton, col, row);

        // Set an event handler for button clicks
        newTableButton.setOnAction(_ -> handleTableButtonAction(btnname));

        // Set an event handler for double-clicks (optional)
        newTableButton.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleTableButtonAction(btnname);
            }
        });

        // Create a Tooltip for the total price
        Tooltip priceTooltip = new Tooltip();
        priceTooltip.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Set event handler for mouse hover (show price)
        newTableButton.setOnMouseEntered(_ -> {
            int totalPrice = getTotalPrice(tableNumber,employeeid);
            loadPreviousOrders(tableNumber,employeeid);
            // Create a StringBuilder to format AllData contents
            StringBuilder allDataText = new StringBuilder();

            // Loop through AllData, but only include the 1st and 3rd items (index 1 and 3)
            for (String[] ro : AllData) {
                if (ro.length > 1) {
                    allDataText.append(ro[1]).append("\t"); // Append 2nd item (index 1)
                }
                if (ro.length > 3) {
                    allDataText.append(ro[3]).append("\n"); // Append 4th item (index 3) and add a newline
                }
            }

            // Append total price to the tooltip content
            allDataText.append("\nВкупно: ").append(totalPrice);

            // Set the formatted text in the tooltip
            priceTooltip.setText(allDataText.toString());
            Tooltip.install(newTableButton, priceTooltip); // Install tooltip
        });


        // Set event handler for mouse exit (hide tooltip)
        newTableButton.setOnMouseExited(_ -> Tooltip.uninstall(newTableButton, priceTooltip)); // Uninstall tooltip

    }
    private void handleTableButtonAction(String btnname) {
        // Extract the table number from the button name
        String[] parts = btnname.split(":");
        if (parts.length == 2) {
            String extractedTableNumber = parts[1];
            tableField.setText(extractedTableNumber); // Set the tableField text

            // Now, check the password and table entry
            String enteredTable = tableField.getText();
            String enteredPassword = passwordField.getText();

            if (checkPassword(enteredPassword)) {
                handleTableEntry(enteredTable, enteredPassword);
                // Clear fields after processing
                tableField.setText("");
                passwordField.setText("");
                Platform.runLater(passwordField::requestFocus);
            } else {
                // If password is incorrect, clear the fields and reset focus
                tableField.setText("");
                passwordField.setText("");
                Platform.runLater(passwordField::requestFocus);
            }
        }
    }

    private void deleteTableButtonFromGrid(String btnname) {
        // Remove the table button and delete button from existingTables and tableAssignment
        existingTables.remove(btnname);
        tableGrid.getChildren().clear();
        coll=0;roww=0;
        getTabels();
    }

    static boolean checkPassword(String proverka) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM vraboten");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String password= rs.getString("shifra");
                String name = rs.getString("Ime");

                // Print each row
                if(password.equals(proverka)){
                    ime=name;
                    return true;
                }
            }

        } catch (SQLException e) {
            System.out.println("checkPassword sql exception" + e);
        }
        return false;
    }

    private void setEnterKeyNavigation(TextField currentField, TextField nextField) {
        currentField.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.ENTER) {
                nextField.requestFocus();
            }
        });
    }
    static void ReadArticles() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, naziv, cena FROM artikl\n" +
                     "order by id;");
             ResultSet rs = stmt.executeQuery()) {

            // Process the result set and dynamically add articles
            while (rs.next()) {
                Map<String, Object> article = new HashMap<>();
                article.put("id", rs.getInt("id"));
                article.put("naziv", rs.getString("naziv"));
                article.put("cena", rs.getInt("cena"));

                // Add the map to the HashSet
                articls.add(article);
            }

        } catch (SQLException e) {
            System.out.println("ReadArticls sql exception" + e);
        }
    }

    //ADMIN panel
    public void showAdminPanel() {
        // Create the main SplitPane (Left - Right)
        SplitPane mainSplitPane = new SplitPane();

        // Left side: Split into top (TableView) and bottom (form)
        SplitPane leftSplitPane = new SplitPane();

        // Create top and bottom sections for the left side
        leftAdminTable = new TableView<>(); // Top part (TableView)
        setupLeftAdminTable();

        VBox leftBottomBox = createBottomSection(); // Bottom part (Form fields and buttons)

        // Left Split Pane: Top - TableView, Bottom - Form
        leftSplitPane.getItems().addAll(leftAdminTable, leftBottomBox);
        leftSplitPane.setOrientation(Orientation.VERTICAL);
        leftSplitPane.setDividerPosition(0, 0.60); // 50% for the top TableView and bottom form section
        leftSplitPane.setDividerPosition(1,0.40);

        // Right side: TableView for displaying the result (takes full height)
        rightAdminResultTable = new TableView<>();// Table to show results

        SplitPane rightSplitPane = new SplitPane();

        HBox doludesno = new HBox();
        doludesno.setSpacing(10);
        // Create a spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); // Allow the spacer to grow

        totalPriceLabel.setText("");
        doludesno.setMaxHeight(40);
        adminfakturabtn.setDisable(true);adminfiskalnabtn.setDisable(true);
        adminfakturabtn.prefWidthProperty().bind(rightAdminResultTable.widthProperty().multiply(0.25));
        adminfakturabtn.styleProperty().bind(Bindings.concat("-fx-font-size: ", rightAdminResultTable.widthProperty().divide(40).asString(), "px;"));
        adminfiskalnabtn.prefWidthProperty().bind(rightAdminResultTable.widthProperty().multiply(0.25));
        adminfiskalnabtn.styleProperty().bind(Bindings.concat("-fx-font-size: ", rightAdminResultTable.widthProperty().divide(40).asString(), "px;"));

        doludesno.setPadding(new Insets(5,10,5,10));
        doludesno.getChildren().addAll(adminfiskalnabtn, adminfakturabtn,spacer,totalPriceLabel);

        rightSplitPane.getItems().addAll(rightAdminResultTable,doludesno);

        rightSplitPane.setOrientation(Orientation.VERTICAL);
        rightSplitPane.setDividerPosition(0, 0.70);
        rightSplitPane.setDividerPosition(1,0.30);

        // Set the right side to take full height available
        VBox.setVgrow(rightAdminResultTable, Priority.ALWAYS);

        // Add the left split pane and right side into the main SplitPane
        mainSplitPane.getItems().addAll(leftSplitPane, rightSplitPane);

        // Create the scene
        Scene scene = new Scene(mainSplitPane);
        adminStage.setTitle("АДМИН");
        enableMacedonianKeyboard(scene);
        adminStage.setScene(scene);
        adminStage.setMaximized(true);
        adminStage.show();  // Show the stage first
    }

    private void setupLeftAdminTable() {
        // Create columns
        TableColumn<String[], String> imeColumn = new TableColumn<>("Име");
        imeColumn.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.15));
        TableColumn<String[], String> idColumn = new TableColumn<>("Id");
        idColumn.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.10));
        TableColumn<String[], String> masaColumn = new TableColumn<>("Маса");
        masaColumn.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.10));
        TableColumn<String[], String> vkupnoColumn = new TableColumn<>("Вкупно");
        vkupnoColumn.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.13));
        TableColumn<String[], String> datumColumn = new TableColumn<>("Дата");
        datumColumn.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.19));
        TableColumn<String[], String> vremecolumm = new TableColumn<>("Време");
        vremecolumm.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.15));
        TableColumn<String[], String> tipColumn = new TableColumn<>("Тип");
        tipColumn.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.14));

        // Set cell value factories to bind the correct column data
        imeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0]));
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1]));
        masaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[2]));
        vkupnoColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[3]));
        datumColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[4]));
        vremecolumm.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[5]));
        tipColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[6]));

        // Bind font size dynamically based on scene width
        leftAdminTable.styleProperty().bind(Bindings.concat("-fx-font-size: ",
                leftAdminTable.widthProperty().divide(35).asString(), "px;"));

        // Add columns to the TableView
        leftAdminTable.getColumns().addAll(imeColumn, idColumn, masaColumn, vkupnoColumn, datumColumn, vremecolumm,tipColumn);
    }

    private VBox createBottomSection() {
        VBox bottomSection = new VBox(10);

        //date fields
        DatePicker datefrom  = new DatePicker();
        DatePicker dateto  = new DatePicker();

        Label datefromLabel = new Label("Датум:");
        Label datetoLabel = new Label("до:");
        datefromLabel.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.10));
        datetoLabel.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.05));
        datefromLabel.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50).asString(), "px;"));
        datetoLabel.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50).asString(), "px;"));


        datefrom.setValue(LocalDate.now());dateto.setValue(LocalDate.now());
        datefrom.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.23));
        dateto.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.23));
        datefrom.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50).asString(), "px;"));
        dateto.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50).asString(), "px;"));


        //time fileds
        TextField timeFromField = new TextField("00:00");
        TextField timeToField = new TextField(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        Label timefromlabel = new Label("Време:");
        Label timetolabel = new Label("до:");
        timefromlabel.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50).asString(), "px;"));
        timetolabel.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50).asString(), "px;"));
        timefromlabel.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.10));
        timetolabel.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.05));

        timeFromField.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.12));
        timeToField.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.12));
        timeFromField.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50), "px;"));
        timeToField.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50), "px;"));


        setMaxInputLength(timeToField,5);setMaxInputLength(timeFromField,5);
        validateTimeInput(timeFromField);validateTimeInput(timeToField);//min pomali od 60 sat pomal od 24

        HBox datetime = new HBox(10,datefromLabel,datefrom,datetoLabel,dateto,timefromlabel,timeFromField,timetolabel,timeToField);
        datetime.setAlignment(Pos.TOP_LEFT);
        datetime.setPadding(new Insets(10));

        Label vraboten = new Label("Вработен:");
        vraboten.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.10));
        vraboten.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50), "px;"));

        ComboBox<Employee> vrabotenComboBox = new ComboBox<>();
        Employee alloptions = new Employee("Сите", "сите");
        List<Employee> employees = fetchEmployees(); // Implement this method to fetch "ime" and "shifra" from the database.
        vrabotenComboBox.getItems().addFirst(alloptions);
        vrabotenComboBox.getItems().addAll(employees);
        vrabotenComboBox.setValue(alloptions);
        vrabotenComboBox.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.15));
        vrabotenComboBox.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50), "px;"));

        Button prikaziButton = new Button("Прикажи");
        Button pregledVrabotenButton = new Button("Преглед по вработен");
        Button pregledArikliliButton = new Button("Преглед по артикли");
        prikaziButton.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.15));
        pregledVrabotenButton.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.25));
        pregledArikliliButton.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.25));
        prikaziButton.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50), "px;"));
        pregledVrabotenButton.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50), "px;"));
        pregledArikliliButton.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50), "px;"));

        // Layout for the action buttons
        HBox vrabotenartikli = new HBox(10,vraboten,vrabotenComboBox,pregledVrabotenButton,prikaziButton);
        vrabotenartikli.setAlignment(Pos.CENTER_LEFT);
        vrabotenartikli.setPadding(new Insets(10));

        //gore sredeno dynamic size width+font-size
        Button pregledSmetkiButton = new Button("Преглед по сметки");
        pregledSmetkiButton.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.25));
        pregledSmetkiButton.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50), "px;"));

        Label tipsmetka = new Label("Тип на сметка:");
        tipsmetka.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.15));
        tipsmetka.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50), "px;"));

        ComboBox<String> tipSmetkaComboBox = new ComboBox<>();
        tipSmetkaComboBox.getItems().addAll("Сите", "фискална", "фактура","картичка");
        tipSmetkaComboBox.setValue("Сите");
        tipSmetkaComboBox.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.18));
        tipSmetkaComboBox.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50), "px;"));

        Button pregledpoddv = new Button("Преглед по ДДВ");
        pregledpoddv.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.25));
        pregledpoddv.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50), "px;"));
        pregledpoddv.setOnAction(_ -> {
            setupRightDDVTable();
            adminfakturabtn.setDisable(true);
            adminfiskalnabtn.setDisable(true);
            if(timeFromField.getText().length() < 4 || timeToField.getText().length() < 4) {
                timeFromField.setText("00:00");
                timeToField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            executeDDVQuery(datefrom.getValue(),dateto.getValue(),timeFromField.getText(),timeToField.getText(),vrabotenComboBox.getValue().shifra,tipSmetkaComboBox.getValue());
            totalPriceLabel.setText("");
        });

        pregledArikliliButton.setOnAction(_ -> {
            setupRightArtikliTable();
            if(timeFromField.getText().length() < 4 || timeToField.getText().length() < 4) {
                timeFromField.setText("00:00");
                timeToField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            executeArtikliQuery(datefrom.getValue(), dateto.getValue(), timeFromField.getText(), timeToField.getText(), vrabotenComboBox.getValue().shifra,tipSmetkaComboBox.getValue());
            totalPriceLabel.setText("");
        });

        pregledSmetkiButton.setOnAction(_ -> {
            adminfakturabtn.setDisable(true);
            adminfiskalnabtn.setDisable(true);
            setupRightSmetkaTable();
            String[] selectedItem = leftAdminTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                executeQueryResult(selectedItem[1]);
                totalPriceLabel.setText("Вкупно: " + selectedItem[3]);
                totalPriceLabel.prefWidthProperty().bind(rightAdminResultTable.widthProperty().multiply(0.25));
                totalPriceLabel.styleProperty().bind(Bindings.concat("-fx-border-color: gray;-fx-font-weight: bold;-fx-font-size: ", rightAdminResultTable.widthProperty().divide(30), "px;"));
                if (selectedItem[6].equals("фискална") || selectedItem[6].equals("картичка")) {
                    adminfakturabtn.setDisable(false);
                }
                if (selectedItem[6].equals("фактура")) {
                    adminfakturabtn.setDisable(false);
                    adminfiskalnabtn.setDisable(false);
                }
            } else {
                showAlertInformation("Избери сметка!");
            }
        });

        pregledVrabotenButton.setOnAction(_ -> {
            if(timeFromField.getText().length() < 4 || timeToField.getText().length() < 4) {
                timeFromField.setText("00:00");
                timeToField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            showEmployeeOverview(datefrom.getValue(), dateto.getValue(), timeFromField.getText(), timeToField.getText(), vrabotenComboBox.getValue().shifra, vrabotenComboBox.getValue().ime);
        });

        Button izbrishaniButton = new Button("Преглед по избришани");
        izbrishaniButton.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.30));
        izbrishaniButton.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50), "px;"));
        izbrishaniButton.setOnAction(_ -> {
            setupRightDeleteArtikliTable();
            adminfakturabtn.setDisable(true);
            adminfiskalnabtn.setDisable(true);
            if(timeFromField.getText().length() < 4 || timeToField.getText().length() < 4) {
                timeFromField.setText("00:00");
                timeToField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            executeDeleteArtikliQuery(datefrom.getValue(), dateto.getValue(), timeFromField.getText(), timeToField.getText(), vrabotenComboBox.getValue().shifra);
        });

        HBox smetkilayot = new HBox(10,tipsmetka,tipSmetkaComboBox,pregledSmetkiButton);
        smetkilayot.setAlignment(Pos.CENTER_LEFT);
        smetkilayot.setPadding(new Insets(10));

        // Exit button
        Button izleziButton = new Button("излези");
        izleziButton.prefWidthProperty().bind(leftAdminTable.widthProperty().multiply(0.15));
        izleziButton.styleProperty().bind(Bindings.concat("-fx-font-size: ", leftAdminTable.widthProperty().divide(50), "px;"));
        izleziButton.setOnAction(_ -> adminStage.close());

        // Layout for the exit button
        HBox pregledi = new HBox(10,pregledArikliliButton,pregledpoddv,izbrishaniButton);
        pregledi.setAlignment(Pos.BOTTOM_LEFT);
        pregledi.setPadding(new Insets(10));
        HBox exitBox = new HBox(10,izleziButton);
        exitBox.setAlignment(Pos.BOTTOM_LEFT);
        exitBox.setPadding(new Insets(10));

        prikaziButton.setOnAction(_ -> {
            if(timeFromField.getText().length() < 4 || timeToField.getText().length() < 4) {
                timeFromField.setText("00:00");
                timeToField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            adminfakturabtn.setDisable(true);
            adminfiskalnabtn.setDisable(true);
            executeQuery(datefrom.getValue(),dateto.getValue(),timeFromField.getText(),timeToField.getText(),vrabotenComboBox.getValue().shifra,tipSmetkaComboBox.getValue());
            leftAdminTable.setItems(smetkadataAdmin);
            leftAdminTable.refresh();
            rezultatiAdmin.clear();
            rightAdminResultTable.refresh();
            totalPriceLabel.setText("");
            rightAdminResultTable.getItems().clear();
            rightAdminResultTable.getColumns().clear();
        });

        adminfakturabtn.setOnAction(_ ->{
            int vrabotenShifra = Integer.parseInt(rezultatiAdmin.getFirst()[8]);
            int smetkaID = Integer.parseInt(rezultatiAdmin.getFirst()[7]);
            admincreateBill(smetkaID, vrabotenShifra);
            smetkaData.clear();
        });
        adminfiskalnabtn.setOnAction(_ -> {
            int smetkaId = Integer.parseInt(rezultatiAdmin.getFirst()[7]);
            changeTipSmetka(smetkaId);
            adminfiskalnabtn.setDisable(true);
            executeQuery(datefrom.getValue(),dateto.getValue(),timeFromField.getText(),timeToField.getText(),vrabotenComboBox.getValue().shifra,tipSmetkaComboBox.getValue());
            leftAdminTable.setItems(smetkadataAdmin);
            leftAdminTable.refresh();
        });

        // Add all elements to the bottom section
        bottomSection.getChildren().addAll(datetime, vrabotenartikli, smetkilayot,pregledi,exitBox);

        return bottomSection;
    }
    // Employee classa
    static class Employee {
        private final String ime;
        private final String shifra;

        public Employee(String ime, String shifra) {
            this.ime = ime;
            this.shifra = shifra;
        }

        @Override
        public String toString() {
            return ime; // Display only the name in the ComboBox
        }
    }

    // Method to fetch employees from the database
    private List<Employee> fetchEmployees() {
        List<Employee> employees = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ime, shifra FROM vraboten")) {
            while (rs.next()) {
                employees.add(new Employee(rs.getString("ime"), rs.getString("shifra")));
            }
        } catch (SQLException e) {
            showAlert(e.getMessage());
        }
        return employees;
    }

    private void validateTimeInput(TextField timeField) {
        timeField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // Automatically add colon if two digits are entered
                if (newValue.length() == 2 && !newValue.contains(":")) {
                    timeField.setText(newValue + ":");
                }
                // Ensure the format is correct: HH:MM
                if (newValue.length() == 5 && newValue.indexOf(":") == 2) {
                    String hours = newValue.substring(0, 2);
                    String minutes = newValue.substring(3, 5);

                    // Validate hours (should be less than 24) and minutes (should be less than 60)
                    try {
                        int hour = Integer.parseInt(hours);
                        int minute = Integer.parseInt(minutes);

                        if (hour >= 24 || minute >= 60) {
                            showAlert( "Часот мора да биди под 24 и минутите под 60.");
                            timeField.setText(oldValue);  // Restore previous value if invalid
                        }
                    } catch (NumberFormatException e) {
                        // Handle invalid input, e.g., letters or symbols
                        timeField.setText(oldValue);  // Restore previous value if invalid
                        showAlert("Погрешено внесен датум!");
                    }
                }
            }
        });
    }

    private void executeQuery(LocalDate dateFrom, LocalDate dateTo, String vremeod, String vremedo, String shifrav, String tipSmetka) {
        try {
            // Ensure the time string is in the format HH:MM:SS
            if (vremeod.length() == 5) {
                vremeod += ":00"; // Append seconds
            }
            if (vremedo.length() == 5) {
                vremedo += ":59"; // Append seconds
            }

            // Base query
            String query = """
            SELECT smetka.vrabotenime as ime, id, masa, Vkupno, TO_CHAR(Datum, 'DD-MM-YYYY') as datum, TO_CHAR(Datum, 'HH24:MI:SS') as vreme, Smetka.tip
            FROM smetka
                     left outer join vraboten AS v ON smetka.VrabotenShifra = v.Shifra
            WHERE (Datum >= ?::timestamp + ?::time)
              AND (Datum <= ?::timestamp + ?::time)
              and smetka.vrabotenime is not null
        """;

            // Dynamically add conditions
            boolean filterByTipSmetka = !tipSmetka.equals("Сите");
            boolean filterByShifrav = !shifrav.equals("сите");

            if (filterByTipSmetka) {
                query += " AND Smetka.tip = ?";
            }
            if (filterByShifrav) {
                query += " AND v.Shifra = ?";
            }

            // Establish a connection to the database
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);

            // Set the common parameters
            ps.setDate(1, Date.valueOf(dateFrom)); // Date from
            ps.setTime(2, Time.valueOf(vremeod)); // Time from
            ps.setDate(3, Date.valueOf(dateTo)); // Date to
            ps.setTime(4, Time.valueOf(vremedo)); // Time to

            // Set conditional parameters
            int parameterIndex = 5;
            if (filterByTipSmetka) {
                ps.setString(parameterIndex, tipSmetka); // Add tipSmetka to the query
                parameterIndex++;
            }
            if (filterByShifrav) {
                ps.setInt(parameterIndex++, Integer.parseInt(shifrav)); // Add shifrav to the query
            }

            // Execute the query
            ResultSet rs = ps.executeQuery();

            // Clear and populate the data
            smetkadataAdmin.clear();
            while (rs.next()) {
                String[] row = new String[]{
                        rs.getString("ime"),
                        rs.getString("id"),
                        rs.getString("masa"),
                        rs.getString("vkupno"),
                        rs.getString("datum"),
                        rs.getString("vreme"),
                        rs.getString("tip")
                };
                smetkadataAdmin.add(row);
            }

            // Close the connections
            rs.close();
            ps.close();
            conn.close();

            // Update the table
            leftAdminTable.setItems(smetkadataAdmin);
            leftAdminTable.refresh();
        } catch (SQLException e) {
            showAlert(e.getMessage());
        }
    }

    private void executeDDVQuery(LocalDate dateFrom, LocalDate dateTo, String vremeod, String vremedo,String vrabotenshifra,String tipSmetka) {
        try {
            // Ensure the time string is in the format HH:MM:SS
            if (vremeod.length() == 5) {
                vremeod += ":00"; // Append seconds
            }
            if (vremedo.length() == 5) {
                vremedo += ":59"; // Append seconds
            }

            // Base query
            String query = """
            select a.ddv,SUM(a.ddv_value)AS ddv_vrednost
            from stavkanasmetka as sn
            inner join artikl as a on sn.ArtiklId = a.Id
            inner join smetka as s on sn.SmetkaId = s.Id
            WHERE (s.Datum >= ?::timestamp + ?::time)
              AND (s.Datum <= ?::timestamp + ?::time)
              AND s.vrabotenime is not null
        """;

            // Dynamically add conditions
            boolean filterByTipSmetka = !tipSmetka.equals("Сите");
            boolean filterByShifrav = !vrabotenshifra.equals("сите");

            if (filterByTipSmetka) {
                query += " AND s.tip = ?";
            }
            if (filterByShifrav) {
                query += " AND s.vrabotenshifra = ?";
            }

            // Add grouping and ordering
            query += """
            group by a.ddv
            order by a.ddv DESC;
        """;

            // Establish database connection
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);

            // Set the common parameters
            ps.setDate(1, Date.valueOf(dateFrom)); // Date from
            ps.setTime(2, Time.valueOf(vremeod)); // Time from
            ps.setDate(3, Date.valueOf(dateTo)); // Date to
            ps.setTime(4, Time.valueOf(vremedo)); // Time to

            // Set conditional parameters
            int parameterIndex = 5;
            if (filterByTipSmetka) {
                ps.setString(parameterIndex, tipSmetka); // Add tipSmetka to the query
                parameterIndex++;
            }
            if (filterByShifrav) {
                ps.setInt(parameterIndex, Integer.parseInt(vrabotenshifra)); // Add shifrav to the query
                parameterIndex++;
            }

            // Execute query
            ResultSet rs = ps.executeQuery();

            // Clear previous data
            rezultatiAdmin.clear();

            // Add results to the ObservableList
            while (rs.next()) {
                String[] row = new String[]{
                        rs.getString("ddv"),
                        rs.getString("ddv_vrednost"),
                };
                rezultatiAdmin.add(row);
            }

            // Close resources
            rs.close();
            ps.close();
            conn.close();

            // Update TableView
            rightAdminResultTable.setItems(rezultatiAdmin);
            rightAdminResultTable.refresh();
        } catch (SQLException e) {
            showAlert(e.getMessage());
        }
    }


    private void setupRightSmetkaTable() {
        try {
            rightAdminResultTable.getColumns().clear();
            rightAdminResultTable.getItems().clear();
            // Create columns
            TableColumn<String[], String> artiklColumn = new TableColumn<>("Артикл");
            artiklColumn.prefWidthProperty().bind(rightAdminResultTable.widthProperty().multiply(0.30)); // 30%

            TableColumn<String[], String> kolicinaColumn = new TableColumn<>("Количина");
            kolicinaColumn.prefWidthProperty().bind(rightAdminResultTable.widthProperty().multiply(0.10)); // 10%

            TableColumn<String[], String> cenaColumn = new TableColumn<>("Цена");
            cenaColumn.prefWidthProperty().bind(rightAdminResultTable.widthProperty().multiply(0.10)); // 15%

            TableColumn<String[], String> vkupnoColumn = new TableColumn<>("Вкупно");
            vkupnoColumn.prefWidthProperty().bind(rightAdminResultTable.widthProperty().multiply(0.15)); // 15%

            TableColumn<String[], String> datumColumn = new TableColumn<>("Дата");
            datumColumn.prefWidthProperty().bind(rightAdminResultTable.widthProperty().multiply(0.18)); // 15%

            TableColumn<String[], String> vremecolumm = new TableColumn<>("Време");
            vremecolumm.prefWidthProperty().bind(rightAdminResultTable.widthProperty().multiply(0.14)); // 15%

            // Set cell value factories
            artiklColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0]));
            kolicinaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1]));
            cenaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[2]));
            vkupnoColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[3]));
            datumColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[4]));
            vremecolumm.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[5]));

            // Bind font size dynamically based on table width
            rightAdminResultTable.styleProperty().bind(Bindings.concat("-fx-font-size: ",
                    rightAdminResultTable.widthProperty().divide(35).asString(), "px;"));

            // Add columns to the TableView
            rightAdminResultTable.getColumns().addAll(artiklColumn, kolicinaColumn, cenaColumn, vkupnoColumn, datumColumn, vremecolumm);
        }catch (IndexOutOfBoundsException e){
            showAlert("Грешка додека се креира табелата. Обиди се повторно!");
            rightAdminResultTable.getColumns().clear();
            rightAdminResultTable.getItems().clear();
        }
    }


    private void setupRightArtikliTable() {
        try {
            rightAdminResultTable.getColumns().clear();
            rightAdminResultTable.getItems().clear();
            // Create columns
            TableColumn<String[], String> artiklColumn = new TableColumn<>("Артикл");
            artiklColumn.prefWidthProperty().bind(rightAdminResultTable.widthProperty().multiply(0.25)); // 30%
            TableColumn<String[], String> kolicinaColumn = new TableColumn<>("Количина");
            kolicinaColumn.prefWidthProperty().bind(rightAdminResultTable.widthProperty().multiply(0.25)); // 30%
            // Set cell value factories
            artiklColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0]));
            kolicinaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1]));

            // Bind font size dynamically based on table width
            rightAdminResultTable.styleProperty().bind(Bindings.concat("-fx-font-size: ",
                    rightAdminResultTable.widthProperty().divide(35).asString(), "px;"));

            // Add columns to the TableView
            rightAdminResultTable.getColumns().addAll(artiklColumn, kolicinaColumn);
        }catch (IndexOutOfBoundsException e){
            showAlert("Грешка додека се креира табелата. Обиди се повторно!");
            rightAdminResultTable.getColumns().clear();
            rightAdminResultTable.getItems().clear();
        }
    }
    private void setupRightDeleteArtikliTable() {
        try {
            rightAdminResultTable.getColumns().clear();
            rightAdminResultTable.getItems().clear();

            // Create columns
            TableColumn<String[], String> artiklColumn = new TableColumn<>("Артикл");
            artiklColumn.prefWidthProperty().bind(rightAdminResultTable.widthProperty().multiply(0.40)); // 30%
            TableColumn<String[], String> kolicinaColumn = new TableColumn<>("Количина");
            kolicinaColumn.prefWidthProperty().bind(rightAdminResultTable.widthProperty().multiply(0.20)); // 30%
            TableColumn<String[], String> vremeColumn = new TableColumn<>("Време");
            vremeColumn.prefWidthProperty().bind(rightAdminResultTable.widthProperty().multiply(0.20)); // 30%
            TableColumn<String[], String> masaColumn = new TableColumn<>("Маса");
            masaColumn.prefWidthProperty().bind(rightAdminResultTable.widthProperty().multiply(0.15)); // 30%

            // Set cell value factories
            artiklColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0]));
            kolicinaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1]));
            vremeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[2]));
            masaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[3]));

            // Bind font size dynamically based on table width
            rightAdminResultTable.styleProperty().bind(Bindings.concat("-fx-font-size: ",
                    rightAdminResultTable.widthProperty().divide(35).asString(), "px;"));
            // Add columns to the TableView
            rightAdminResultTable.getColumns().addAll(artiklColumn, kolicinaColumn, vremeColumn, masaColumn);
        }catch (IndexOutOfBoundsException e){
            showAlert("Грешка додека се креира табелата. Обиди се повторно!");
            rightAdminResultTable.getColumns().clear();
            rightAdminResultTable.getItems().clear();
        }
    }
    private void setupRightDDVTable() {
        try {
            rightAdminResultTable.getColumns().clear();
            rightAdminResultTable.getItems().clear();

            // Create columns
            TableColumn<String[], String> ddvcolumn = new TableColumn<>("ДДВ %");
            ddvcolumn.prefWidthProperty().bind(rightAdminResultTable.widthProperty().multiply(0.25)); // 30%
            TableColumn<String[], String> sumaddvcolumn = new TableColumn<>("Сума");
            sumaddvcolumn.prefWidthProperty().bind(rightAdminResultTable.widthProperty().multiply(0.25)); // 30%

            // Set cell value factories
            ddvcolumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0]));
            sumaddvcolumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1]));

            // Bind font size dynamically based on table width
            rightAdminResultTable.styleProperty().bind(Bindings.concat("-fx-font-size: ",
                    rightAdminResultTable.widthProperty().divide(35).asString(), "px;"));
            // Add columns to the TableView
            rightAdminResultTable.getColumns().addAll(ddvcolumn, sumaddvcolumn);
        }catch (IndexOutOfBoundsException e){
            showAlert("Грешка додека се креира табелата. Обиди се повторно!");
            rightAdminResultTable.getColumns().clear();
            rightAdminResultTable.getItems().clear();
        }
    }

    private void executeArtikliQuery(LocalDate dateFrom, LocalDate dateTo, String vremeod, String vremedo,String vrabotenshifra,String tipSmetka) {
        try {
            // Ensure the time string is in the format HH:MM:SS
            if (vremeod.length() == 5) {
                vremeod += ":00"; // Append seconds
            }
            if (vremedo.length() == 5) {
                vremedo += ":59"; // Append seconds
            }

            // Base query
            String query = """
            select a.naziv, sum(kolicina) as kolicina
            from smetka as s
            inner join stavkanasmetka as sn on s.id = sn.smetkaid
            inner join artikl as a on a.id = sn.artiklid
            WHERE (Datum >= ?::timestamp + ?::time)
              AND (Datum <= ?::timestamp + ?::time)
              AND s.vrabotenime is not null
        """;

            // Dynamically add conditions
            boolean filterByTipSmetka = !tipSmetka.equals("Сите");
            boolean filterByShifrav = !vrabotenshifra.equals("сите");

            if (filterByTipSmetka) {
                query += " AND s.tip = ?";
            }
            if (filterByShifrav) {
                query += " AND s.vrabotenshifra = ?";
            }

            // Add grouping and ordering
            query += """
            group by a.naziv
            order by kolicina DESC;
        """;

            // Establish database connection
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);

            // Set the common parameters
            ps.setDate(1, Date.valueOf(dateFrom)); // Date from
            ps.setTime(2, Time.valueOf(vremeod)); // Time from
            ps.setDate(3, Date.valueOf(dateTo)); // Date to
            ps.setTime(4, Time.valueOf(vremedo)); // Time to

            // Set conditional parameters
            int parameterIndex = 5;
            if (filterByTipSmetka) {
                ps.setString(parameterIndex++, tipSmetka); // Add tipSmetka to the query
            }
            if (filterByShifrav) {
                ps.setInt(parameterIndex++, Integer.parseInt(vrabotenshifra)); // Add shifrav to the query
            }

            // Execute query
            ResultSet rs = ps.executeQuery();

            // Clear previous data
            rezultatiAdmin.clear();

            // Add results to the ObservableList
            while (rs.next()) {
                String[] row = new String[]{
                        rs.getString("naziv"),
                        rs.getString("kolicina"),
                };
                rezultatiAdmin.add(row);
            }

            // Close resources
            rs.close();
            ps.close();
            conn.close();

            // Update TableView
            rightAdminResultTable.setItems(rezultatiAdmin);
            rightAdminResultTable.refresh();
        } catch (SQLException e) {
            showAlert(e.getMessage());
        }
    }

    private void executeDeleteArtikliQuery(LocalDate dateFrom, LocalDate dateTo, String vremeod, String vremedo,String vrabotenshifra) {
        try {
            // Ensure the time string is in the format HH:MM:SS
            if (vremeod.length() == 5) {
                vremeod += ":00"; // Append seconds
            }
            if (vremedo.length() == 5) {
                vremedo += ":59"; // Append seconds
            }

            // Base query
            String query = """
            select a.Naziv,i.kolicina,TO_CHAR(i.Datum, 'HH24:MI:SS') as datum,i.Masa
            from izbrishani as i
            inner join Artikl as a on i.artiklid = a.id
            inner join Vraboten V on i.VrabotenShifra = V.Shifra
            WHERE (datum >= ?::timestamp + ?::time)
              AND (datum <= ?::timestamp + ?::time)
        """;

            // Dynamically add conditions
            boolean filterByShifrav = !vrabotenshifra.equals("сите");

            if (filterByShifrav) {
                query += " AND i.vrabotenshifra = ?";
            }

            // Add grouping and ordering
            query += """
            order by i.kolicina DESC;
        """;

            // Establish database connection
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);

            // Set the common parameters
            ps.setDate(1, Date.valueOf(dateFrom)); // Date from
            ps.setTime(2, Time.valueOf(vremeod)); // Time from
            ps.setDate(3, Date.valueOf(dateTo)); // Date to
            ps.setTime(4, Time.valueOf(vremedo)); // Time to

            // Set conditional parameters
            if (filterByShifrav) {
                ps.setInt(5,Integer.parseInt(vrabotenshifra)); // Add shifrav to the query
            }

            // Execute query
            ResultSet rs = ps.executeQuery();

            // Clear previous data
            rezultatiAdmin.clear();

            // Add results to the ObservableList
            while (rs.next()) {
                String[] row = new String[]{
                        rs.getString("naziv"),
                        rs.getString("kolicina"),
                        rs.getString("datum"),
                        rs.getString("masa")
                };
                rezultatiAdmin.add(row);
            }

            // Close resources
            rs.close();
            ps.close();
            conn.close();

            // Update TableView
            rightAdminResultTable.setItems(rezultatiAdmin);
            rightAdminResultTable.refresh();
        } catch (SQLException e) {
            showAlert(e.getMessage());
        }
    }

    private void executeQueryResult(String smetkaid) {
        try {
            // SQL query with explicit type casting for date comparison
            String query = """
                select a.Naziv,sn.Kolicina,a.Cena as cena,a.Cena*sn.Kolicina as vkupno,DATE(Datum) as datum,TO_CHAR(vreme, 'HH24:MI:SS') AS vreme,v.ime as ime,SmetkaId,s.vrabotenshifra as vrabotenshifra
                from smetka as s
                left outer join vraboten as v on s.vrabotenshifra = v.shifra
                inner join stavkanasmetka as sn on s.Id = sn.SmetkaId
                inner join artikl as a on sn.ArtiklId = a.Id
                where SmetkaId = ?;
            """;

            // Establish a connection to the database
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);

            // Set the parameters for the query
            ps.setInt(1, Integer.parseInt(smetkaid));

            // Execute the query
            ResultSet rs = ps.executeQuery();
            // Clear previous data in the table
            rezultatiAdmin.clear();
            // Add results to the ObservableList
            while (rs.next()) {
                String[] row = new String[]{
                        rs.getString("naziv"),
                        rs.getString("kolicina"),
                        rs.getString("cena"),
                        rs.getString("vkupno"),
                        rs.getString("datum"),
                        rs.getString("vreme"),
                        rs.getString("ime"),
                        rs.getString("SmetkaId"),
                        rs.getString("vrabotenshifra")
                };
                rezultatiAdmin.add(row);
            }
            // Close the connections
            rs.close();
            ps.close();
            conn.close();
            rightAdminResultTable.setItems(rezultatiAdmin);
            rightAdminResultTable.refresh();
        } catch (SQLException e) {
            showAlert(e.getMessage());
        }
    }

    static boolean checkAdmin(String proverka) {
        return Admin.checkAdmin(proverka);
    }

    public void showEmployeeOverview(LocalDate dateFrom, LocalDate dateTo, String timeFrom, String timeTo, String employeeCode,String employeeName) {

        if (timeFrom.length() == 5) {
            timeFrom += ":00"; // Append seconds
        }
        if (timeTo.length() == 5) {
            timeTo += ":59"; // Append seconds
        }

        // SQL query
        String query = """
            SELECT SUM(Vkupno) AS Suma
            FROM Smetka
            WHERE (Datum >= ?::timestamp + ?::time)
              AND (Datum <= ?::timestamp + ?::time)
              AND tip = ?
        """;

        String deletedItemsQuery = """
        SELECT a.Naziv, i.kolicina, i.Datum,i.masa
        FROM izbrishani i
        JOIN Artikl a ON i.ArtiklId = a.Id
        WHERE (Datum >= ?::timestamp + ?::time)
              AND (Datum <= ?::timestamp + ?::time)
    """;

        boolean filterByShifrav = !employeeCode.equals("сите");

        if (filterByShifrav) {
            query += " AND vrabotenshifra = ?";
            deletedItemsQuery += " AND vrabotenshifra = ?";
        }

        double fiscalTotal = 0;
        double invoiceTotal = 0;
        double karticaTotal = 0;
        List<String[]> deletedItems = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Query for fiscal
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setDate(1, Date.valueOf(dateFrom)); // Date from
                ps.setTime(2, Time.valueOf(timeFrom)); // Time from
                ps.setDate(3, Date.valueOf(dateTo)); // Date to
                ps.setTime(4, Time.valueOf(timeTo)); // Time to
                ps.setString(5, "фискална");
                if (filterByShifrav) {
                    ps.setInt(6, Integer.parseInt(employeeCode));
                }

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    fiscalTotal = rs.getDouble("Suma");
                }
            }
            // Query for invoice
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setDate(1, Date.valueOf(dateFrom)); // Date from
                ps.setTime(2, Time.valueOf(timeFrom)); // Time from
                ps.setDate(3, Date.valueOf(dateTo)); // Date to
                ps.setTime(4, Time.valueOf(timeTo)); // Time to
                ps.setString(5, "фактура");
                if (filterByShifrav) {
                    ps.setInt(6, Integer.parseInt(employeeCode));
                }

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    invoiceTotal = rs.getDouble("Suma");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setDate(1, Date.valueOf(dateFrom)); // Date from
                ps.setTime(2, Time.valueOf(timeFrom)); // Time from
                ps.setDate(3, Date.valueOf(dateTo)); // Date to
                ps.setTime(4, Time.valueOf(timeTo)); // Time to
                ps.setString(5, "картичка");
                if (filterByShifrav) {
                    ps.setInt(6, Integer.parseInt(employeeCode));
                }

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    karticaTotal = rs.getDouble("Suma");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(deletedItemsQuery)) {
                ps.setDate(1, Date.valueOf(dateFrom)); // Date from
                ps.setTime(2, Time.valueOf(timeFrom)); // Time from
                ps.setDate(3, Date.valueOf(dateTo)); // Date to
                ps.setTime(4, Time.valueOf(timeTo));
                if (filterByShifrav) {
                    ps.setInt(5, Integer.parseInt(employeeCode));
                }

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String naziv = rs.getString("Naziv");
                    int kolicina = rs.getInt("Kolicina");
                    String datum = rs.getTimestamp("Datum").toString();
                    int masa = rs.getInt("Masa");
                    deletedItems.add(new String[]{naziv, String.valueOf(kolicina), datum, String.valueOf(masa)});
                }
            }
        } catch (Exception e) {
            showAlert(e.getMessage());
        }
        double total = fiscalTotal + invoiceTotal + karticaTotal;

        // Create the popup stage
        Stage stage = new Stage();
        stage.setTitle("Преглед по вработен - " + employeeName);

        // Create the layout
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-padding: 10;");

        // Add a title
        Label titleLabel = new Label("Преглед по вработен - " + employeeName);
        titleLabel.setStyle("-fx-font-size: 18;-fx-font-weight: bold;");

        titleLabel.setTextAlignment(TextAlignment.CENTER);

        // Add sections with borders for the table
        GridPane detailsPane = createSection("Детали за временски период",
                new String[]{"Датум од:", dateFrom.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "       до:   " + dateTo.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))},
                new String[]{"Време од:", timeFrom + "            до:   " + timeTo});
        GridPane fiscalPane = createSection("Фискален промет:   " + String.format("%.2f",fiscalTotal));
        GridPane invoicePane = createSection("Фактура промет:     " + String.format("%.2f", invoiceTotal));
        GridPane karticaPane = createSection("Картица промет:     " + String.format("%.2f", karticaTotal));
        GridPane totalPane = createSection("Вкупен промет:       " + String.format("%.2f", total));

        // Create table for deleted items
        TableView<String[]> deletedItemsTable = new TableView<>();
        TableColumn<String[], String> nazivCol = new TableColumn<>("Назив");
        nazivCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));

        TableColumn<String[], String> kolicinaCol = new TableColumn<>("Количина");
        kolicinaCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));

        TableColumn<String[], String> datumCol = new TableColumn<>("Датум");
        datumCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));

        TableColumn<String[], String> masaCol = new TableColumn<>("Маса");
        masaCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[3]));

        deletedItemsTable.getColumns().addAll(nazivCol, kolicinaCol, datumCol, masaCol);
        deletedItemsTable.getItems().addAll(deletedItems);

        VBox deletedItemsSection = new VBox();
        deletedItemsSection.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-padding: 10;");
        deletedItemsSection.setSpacing(10); // Add spacing between rows

        Label deletedItemsLabel = new Label("Избришани ставки:");
        deletedItemsLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        deletedItemsSection.getChildren().add(deletedItemsLabel);

        for (String[] item : deletedItems) {
            String formattedTime;
            try {
                String timestamp = item[2];
                formattedTime = timestamp.substring(11, 19); // Get the time part from the string (position 11 to 18)

            } catch (IndexOutOfBoundsException e) {
                showAlert(e.getMessage());
                formattedTime = item[2]; // Use the original value if extraction fails
            }
            String itemDetails = String.format("%s | -%s | %s | Маса: %s",
                    item[0], item[1], formattedTime, item[3]);

            Label itemLabel = new Label(itemDetails);
            itemLabel.setStyle("-fx-font-size: 14;"); // Style each label
            deletedItemsSection.getChildren().add(itemLabel);
        }
        Button printButton = new Button("Печати");
        printButton.setAlignment(Pos.BOTTOM_LEFT);
        // Combine all elements in the layout
        VBox layout = new VBox();
        layout.setSpacing(5); // Add spacing between sections
        layout.setStyle("-fx-font-size: 16;");
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(titleLabel, detailsPane, fiscalPane, invoicePane,karticaPane, totalPane, deletedItemsSection,printButton);


        double finalFiscalTotal = fiscalTotal;
        double finalInvoiceTotal = invoiceTotal;
        double finalKarticaTotal = karticaTotal;
        String finalTimeFrom = timeFrom;
        String finalTimeTo = timeTo;
        printButton.setOnAction(_ ->{
            PrinterService printerService = new PrinterService();
            printerService.printEmployeeOverview(employeeName, finalFiscalTotal, finalInvoiceTotal, finalKarticaTotal,dateFrom,dateTo, finalTimeFrom, finalTimeTo);
        });

        // Wrap the layout in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true); // Ensure the content stretches to the width of the ScrollPane
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Never show horizontal scrollbar
        Scene scene = new Scene(scrollPane, 400, 600);
        stage.setScene(scene);
        stage.show();
    }

    // Helper function to create a section with borders
    private GridPane createSection(String title, String[]... rows) {
        GridPane section = new GridPane();
        section.setPadding(new Insets(10));
        section.setHgap(10);
        section.setVgap(5);
        section.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-padding: 10;");
        Label titleLabel = new Label(title);
        section.add(titleLabel, 0, 0, 2, 1); // Title spans two columns

        int rowIndex = 1;
        for (String[] row : rows) {
            Label keyLabel = new Label(row[0]);
            Label valueLabel = new Label(row[1]);

            section.add(keyLabel, 0, rowIndex);
            section.add(valueLabel, 1, rowIndex);
            rowIndex++;
        }
        return section;
    }
    public static void setMaxInputLength(TextInputControl inputControl, int maxLength) {
        TextFormatter<String> formatter = new TextFormatter<>(change ->
                change.getControlNewText().length() <= maxLength ? change : null
        );
        inputControl.setTextFormatter(formatter);
    }

    //ALERTS za info
    static void showAlertInformation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информација");
        alert.setContentText(message);
        alert.showAndWait();
    }
    static void enableMacedonianKeyboard(Scene scene) {
        Map<String, String> macedonianCharMap = createMacedonianCharMap();

        scene.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            Node focusedNode = scene.getFocusOwner();
            if (focusedNode instanceof TextInputControl) {
                TextInputControl textInput = (TextInputControl) focusedNode;

                String input = event.getCharacter();
                String mappedChar = macedonianCharMap.get(input);

                if (mappedChar != null) {
                    event.consume(); // Prevent default input
                    textInput.appendText(mappedChar);
                }
            }
        });
    }

    private static Map<String, String> createMacedonianCharMap() {
        Map<String, String> macedonianCharMap = new HashMap<>();

        // Define key mappings (standard Macedonian keyboard layout)
        macedonianCharMap.put(";", "Ч");
        macedonianCharMap.put("[","Ш");
        macedonianCharMap.put("]","Ѓ");
        macedonianCharMap.put("'","Ќ");
        macedonianCharMap.put("q", "љ");
        macedonianCharMap.put("w", "њ");
        macedonianCharMap.put("e", "е");
        macedonianCharMap.put("r", "р");
        macedonianCharMap.put("t", "т");
        macedonianCharMap.put("y", "ѕ");
        macedonianCharMap.put("u", "у");
        macedonianCharMap.put("i", "и");
        macedonianCharMap.put("o", "о");
        macedonianCharMap.put("p", "п");
        macedonianCharMap.put("a", "а");
        macedonianCharMap.put("s", "с");
        macedonianCharMap.put("d", "д");
        macedonianCharMap.put("f", "ф");
        macedonianCharMap.put("g", "г");
        macedonianCharMap.put("h", "х");
        //macedonianCharMap.put("j", "ј");
        macedonianCharMap.put("k", "к");
        macedonianCharMap.put("l", "л");
        macedonianCharMap.put("z", "з");
        macedonianCharMap.put("x", "џ");
        macedonianCharMap.put("c", "ц");
        macedonianCharMap.put("v", "в");
        macedonianCharMap.put("b", "б");
        macedonianCharMap.put("n", "н");
        macedonianCharMap.put("m", "м");

        // Uppercase mappings
        macedonianCharMap.put("Q", "Љ");
        macedonianCharMap.put("W", "Њ");
        macedonianCharMap.put("E", "Е");
        macedonianCharMap.put("R", "Р");
        macedonianCharMap.put("T", "Т");
        macedonianCharMap.put("Y", "Ѕ");
        macedonianCharMap.put("U", "У");
        macedonianCharMap.put("I", "И");
        macedonianCharMap.put("O", "О");
        macedonianCharMap.put("P", "П");
        macedonianCharMap.put("A", "А");
        macedonianCharMap.put("S", "С");
        macedonianCharMap.put("D", "Д");
        macedonianCharMap.put("F", "Ф");
        macedonianCharMap.put("G", "Г");
        macedonianCharMap.put("H", "Х");
        //macedonianCharMap.put("J", "Ј");
        macedonianCharMap.put("K", "К");
        macedonianCharMap.put("L", "Л");
        macedonianCharMap.put("Z", "З");
        macedonianCharMap.put("X", "Џ");
        macedonianCharMap.put("C", "Ц");
        macedonianCharMap.put("V", "В");
        macedonianCharMap.put("B", "Б");
        macedonianCharMap.put("N", "Н");
        macedonianCharMap.put("M", "М");

        return macedonianCharMap;
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::closePool));
        launch(args);
    }
}
