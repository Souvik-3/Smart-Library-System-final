package com.smartlibrary.controller;

import com.smartlibrary.model.Book;
import com.smartlibrary.model.LibraryManager;
import com.smartlibrary.model.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StudentController extends BaseController implements Initializable {

    // Top Section
    @FXML private Label welcomeLabel;
    @FXML private Label breadcrumbNameLabel;
    @FXML private ChoiceBox<String> searchChoice;
    @FXML private TextField searchField;
    
    // Main Content
    @FXML private TabPane mainTabPane;
    @FXML private Label checkedOutCountLabel;
    @FXML private Tab checkedOutTab;
    @FXML private Tab availableBooksTab;

    // Checked Out Table
    @FXML private TableView<Book> borrowedTable;
    @FXML private TableColumn<Book, String> colBorrTitle;
    @FXML private TableColumn<Book, String> colBorrAuthor;
    @FXML private TableColumn<Book, String> colBorrDue;
    @FXML private TableColumn<Book, String> colBorrCall;
    @FXML private TableColumn<Book, String> colBorrRenew;
    @FXML private TableColumn<Book, String> colBorrFines;

    // Available Books Table (Search Results)
    @FXML private TableView<Book> availableTable;
    @FXML private TableColumn<Book, String> colAvailTitle;
    @FXML private TableColumn<Book, String> colAvailAuthor;
    @FXML private TableColumn<Book, String> colAvailStatus;

    private ObservableList<Book> availableBooks;
    private ObservableList<Book> borrowedBooks;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchChoice.setItems(FXCollections.observableArrayList("Library catalog", "Title", "Author"));
        searchChoice.setValue("Library catalog");

        colAvailTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAvailAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colAvailStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colBorrTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colBorrAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        
        colBorrDue.setCellValueFactory(cellData -> new SimpleStringProperty("17/10/2026"));
        colBorrCall.setCellValueFactory(cellData -> new SimpleStringProperty("005.1 INT"));
        colBorrRenew.setCellValueFactory(cellData -> new SimpleStringProperty("Not renewable"));
        colBorrFines.setCellValueFactory(cellData -> new SimpleStringProperty("No"));
    }

    @Override
    public void setMain(com.smartlibrary.Main mainApp) {
        super.setMain(mainApp);
        User currentUser = LibraryManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Hello, " + currentUser.getName());
            breadcrumbNameLabel.setText(currentUser.getName());
        }
        refreshTables();
    }

    private void refreshTables() {
        String studentId = LibraryManager.getInstance().getCurrentUser().getId();

        availableBooks = FXCollections.observableArrayList(
            LibraryManager.getInstance().getBooks().stream()
                .filter(b -> "Available".equals(b.getStatus()))
                .collect(Collectors.toList())
        );

        borrowedBooks = FXCollections.observableArrayList(
            LibraryManager.getInstance().getBooks().stream()
                .filter(b -> studentId.equals(b.getIssuedTo()))
                .collect(Collectors.toList())
        );

        availableTable.setItems(availableBooks);
        borrowedTable.setItems(borrowedBooks);
        checkedOutCountLabel.setText(borrowedBooks.size() + " Item(s) checked out");
    }

    @FXML
    private void handleSidebarClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String feature = (String) clickedButton.getUserData();
        
        mainTabPane.getTabs().clear();
        
        if ("Summary".equals(feature)) {
            mainTabPane.getTabs().addAll(checkedOutTab, availableBooksTab);
            mainTabPane.getSelectionModel().select(checkedOutTab);
        } else {
            Tab featureTab = new Tab(feature);
            VBox contentBox = new VBox(20);
            contentBox.setPadding(new Insets(20));
            contentBox.setStyle("-fx-background-color: white;");
            
            switch (feature) {
                case "Personal details": buildPersonalDetailsView(contentBox); break;
                case "Charges": buildChargesView(contentBox); break;
                case "Lists":
                case "Tags": buildListsAndTagsView(contentBox); break;
                case "Change password": buildChangePasswordView(contentBox); break;
                case "Purchase suggestions": buildPurchaseSuggestionsView(contentBox); break;
                case "Messaging": buildMessagingView(contentBox); break;
                default:
                    Label lbl = new Label("The '" + feature + "' feature is currently under construction.");
                    lbl.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d;");
                    contentBox.setAlignment(Pos.CENTER);
                    contentBox.getChildren().add(lbl);
                    break;
            }
            
            featureTab.setContent(contentBox);
            mainTabPane.getTabs().add(featureTab);
        }
    }

    // --- DYNAMIC UI BUILDER METHODS ---

    private void buildPersonalDetailsView(VBox box) {
        Label title = new Label("Personal Details (Demo)");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // RadioButtons
        Label genderLabel = new Label("Gender:");
        ToggleGroup genderGroup = new ToggleGroup();
        RadioButton rbMale = new RadioButton("Male"); rbMale.setToggleGroup(genderGroup);
        RadioButton rbFemale = new RadioButton("Female"); rbFemale.setToggleGroup(genderGroup);
        HBox genderBox = new HBox(10, rbMale, rbFemale);
        Label genderResult = new Label();
        genderGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) genderResult.setText("Selected: " + ((RadioButton)newV).getText());
        });

        // CheckBoxes
        Label hobbiesLabel = new Label("Hobbies:");
        CheckBox chkReading = new CheckBox("Reading");
        CheckBox chkGaming = new CheckBox("Gaming");
        Button submitHobbies = new Button("Submit");
        Label hobbyResult = new Label();
        submitHobbies.setOnAction(e -> {
            StringBuilder sb = new StringBuilder("Selected: ");
            if (chkReading.isSelected()) sb.append("Reading ");
            if (chkGaming.isSelected()) sb.append("Gaming ");
            hobbyResult.setText(sb.toString());
        });
        HBox hobbiesBox = new HBox(10, chkReading, chkGaming, submitHobbies);

        // ChoiceBox & ComboBox
        Label colorLbl = new Label("Favorite Color:");
        ChoiceBox<String> colorChoice = new ChoiceBox<>(FXCollections.observableArrayList("Red", "Green", "Blue"));
        colorChoice.setOnAction(e -> {
            if ("Red".equals(colorChoice.getValue())) box.setStyle("-fx-background-color: #ffcccc;");
            else if ("Green".equals(colorChoice.getValue())) box.setStyle("-fx-background-color: #ccffcc;");
            else if ("Blue".equals(colorChoice.getValue())) box.setStyle("-fx-background-color: #ccccff;");
        });
        
        Label countryLbl = new Label("Country:");
        ComboBox<String> countryCombo = new ComboBox<>(FXCollections.observableArrayList("Bangladesh", "USA", "UK"));

        // DatePicker & ColorPicker
        Label dobLbl = new Label("Date of Birth:");
        DatePicker dobPicker = new DatePicker();
        Label dobResult = new Label();
        dobPicker.setOnAction(e -> {
            if (dobPicker.getValue() != null) {
                dobResult.setText("DOB: " + dobPicker.getValue().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            }
        });

        Label cpLbl = new Label("Profile Theme:");
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setOnAction(e -> {
            String hex = String.format("#%02X%02X%02X",
                (int)(colorPicker.getValue().getRed() * 255),
                (int)(colorPicker.getValue().getGreen() * 255),
                (int)(colorPicker.getValue().getBlue() * 255));
            title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + hex + ";");
        });

        box.getChildren().addAll(
            title, new Separator(),
            genderLabel, genderBox, genderResult, new Separator(),
            hobbiesLabel, hobbiesBox, hobbyResult, new Separator(),
            new HBox(20, new VBox(5, colorLbl, colorChoice), new VBox(5, countryLbl, countryCombo)), new Separator(),
            new HBox(20, new VBox(5, dobLbl, dobPicker, dobResult), new VBox(5, cpLbl, colorPicker))
        );
    }

    private void buildChargesView(VBox box) {
        Label title = new Label("Charges & Fines (Demo)");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // ProgressBar
        Label pLabel = new Label("Pay Fine Target:");
        TextField targetField = new TextField(); targetField.setPromptText("Target Amount");
        Button btnAcc = new Button("Pay 10");
        ProgressBar pBar = new ProgressBar(0); pBar.setPrefWidth(300);
        Label pResult = new Label("Paid: 0");
        
        final int[] currentSum = {0};
        btnAcc.setOnAction(e -> {
            try {
                int target = Integer.parseInt(targetField.getText());
                currentSum[0] += 10;
                pBar.setProgress((double) currentSum[0] / target);
                pResult.setText("Paid: " + currentSum[0]);
                if (currentSum[0] >= target) pBar.setProgress(1.0);
            } catch (NumberFormatException ex) {}
        });

        // Slider
        Label sLabel = new Label("Dynamic Font Resizer:");
        Slider slider = new Slider(10, 36, 12);
        Label sResult = new Label("Resize me!");
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            sResult.setStyle("-fx-font-size: " + newVal.doubleValue() + "px;");
        });

        // Spinner
        Label spLabel = new Label("Quantity Spinner:");
        Spinner<Integer> spinner = new Spinner<>();
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
        Button spBtn = new Button("Show");
        Label spResult = new Label();
        spBtn.setOnAction(e -> spResult.setText("Quantity: " + spinner.getValue()));

        box.getChildren().addAll(
            title, new Separator(),
            pLabel, new HBox(10, targetField, btnAcc), pBar, pResult, new Separator(),
            sLabel, slider, sResult, new Separator(),
            spLabel, new HBox(10, spinner, spBtn), spResult
        );
    }

    private void buildListsAndTagsView(VBox box) {
        Label title = new Label("Lists & Tags (Demo)");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // ListView
        Label lvLabel = new Label("ListView Selection:");
        ListView<String> listView = new ListView<>(FXCollections.observableArrayList("Apple", "Banana", "Cherry"));
        listView.setPrefHeight(100);
        Label lvResult = new Label("Selected: None");
        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) lvResult.setText("Selected: " + newVal);
        });

        // TreeView
        Label tvLabel = new Label("TreeView Hierarchy:");
        TreeItem<String> root = new TreeItem<>("Categories");
        root.getChildren().addAll(new TreeItem<>("Fiction"), new TreeItem<>("Science"));
        TreeView<String> treeView = new TreeView<>(root);
        treeView.setPrefHeight(120);
        Label tvResult = new Label("Selected Node: None");
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) tvResult.setText("Selected Node: " + newVal.getValue());
        });

        box.getChildren().addAll(title, new Separator(), lvLabel, listView, lvResult, new Separator(), tvLabel, treeView, tvResult);
    }

    private void buildChangePasswordView(VBox box) {
        Label title = new Label("Change Password (Demo)");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        PasswordField pwdField = new PasswordField(); pwdField.setPromptText("Enter new password");
        TextField pwdVisibleField = new TextField(); pwdVisibleField.setPromptText("Enter new password");
        pwdVisibleField.setManaged(false); pwdVisibleField.setVisible(false);
        pwdVisibleField.textProperty().bindBidirectional(pwdField.textProperty());
        
        StackPane stack = new StackPane(pwdField, pwdVisibleField);
        Button toggleBtn = new Button("Show");
        toggleBtn.setOnAction(e -> {
            if (pwdField.isVisible()) {
                pwdField.setVisible(false); pwdField.setManaged(false);
                pwdVisibleField.setVisible(true); pwdVisibleField.setManaged(true);
                toggleBtn.setText("Hide");
            } else {
                pwdVisibleField.setVisible(false); pwdVisibleField.setManaged(false);
                pwdField.setVisible(true); pwdField.setManaged(true);
                toggleBtn.setText("Show");
            }
        });

        box.getChildren().addAll(title, new Separator(), new Label("New Password:"), new HBox(10, stack, toggleBtn));
    }

    private void buildPurchaseSuggestionsView(VBox box) {
        Label title = new Label("Purchase Suggestions (Demo)");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button browseBtn = new Button("Browse Book Cover Image");
        ImageView imageView = new ImageView();
        imageView.setFitWidth(200); imageView.setFitHeight(200); imageView.setPreserveRatio(true);

        browseBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg"));
            File file = chooser.showOpenDialog(null);
            if (file != null) {
                imageView.setImage(new Image(file.toURI().toString()));
            }
        });

        box.getChildren().addAll(title, new Separator(), browseBtn, imageView);
    }

    private void buildMessagingView(VBox box) {
        Label title = new Label("Messaging & Alerts (Demo)");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextArea textArea = new TextArea(); textArea.setPromptText("Write a message to the librarian...");
        textArea.setPrefHeight(100);
        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> textArea.clear());

        Label alertLabel = new Label("Test Alert Dialogs:");
        Button btnInfo = new Button("Info Alert");
        btnInfo.setOnAction(e -> showAlert("Information", "This is an info alert.", Alert.AlertType.INFORMATION));
        
        Button btnWarning = new Button("Warning Alert");
        btnWarning.setOnAction(e -> showAlert("Warning", "This is a warning alert.", Alert.AlertType.WARNING));

        box.getChildren().addAll(title, new Separator(), new Label("Message:"), textArea, clearBtn, new Separator(), alertLabel, new HBox(10, btnInfo, btnWarning));
    }

    // --- END DYNAMIC UI BUILDER METHODS ---

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().trim();
        
        if (!mainTabPane.getTabs().contains(availableBooksTab)) {
            mainTabPane.getTabs().clear();
            mainTabPane.getTabs().addAll(checkedOutTab, availableBooksTab);
        }
        mainTabPane.getSelectionModel().select(availableBooksTab);

        if (query.isEmpty()) {
            refreshTables();
            return;
        }
        
        availableTable.setPlaceholder(new Label("Searching online..."));
        
        LibraryManager.getInstance().getExecutorService().submit(() -> {
            try {
                String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://openlibrary.org/search.json?q=" + encodedQuery + "&limit=10"))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.body());
                JsonNode docs = rootNode.path("docs");
                
                int fetchedCount = 0;
                if (docs.isArray()) {
                    for (JsonNode doc : docs) {
                        String title = doc.path("title").asText("Unknown Title");
                        String author = "Unknown Author";
                        if (doc.path("author_name").isArray() && doc.path("author_name").size() > 0) {
                            author = doc.path("author_name").get(0).asText();
                        }
                        String id = "API-STUDENT-" + System.currentTimeMillis() + "-" + fetchedCount;
                        
                        boolean exists = LibraryManager.getInstance().getBooks().stream()
                                .anyMatch(b -> b.getTitle().equalsIgnoreCase(title));
                                
                        if (!exists) {
                            Book newBook = new Book(id, title, author, "Available", "");
                            LibraryManager.getInstance().addBook(newBook);
                        }
                        fetchedCount++;
                        Thread.sleep(10);
                    }
                }
                
                Platform.runLater(() -> {
                    refreshTables();
                    
                    ObservableList<Book> filtered = FXCollections.observableArrayList(
                        availableBooks.stream()
                            .filter(b -> b.getTitle().toLowerCase().contains(query.toLowerCase()) || b.getAuthor().toLowerCase().contains(query.toLowerCase()))
                            .collect(Collectors.toList())
                    );
                    availableTable.setItems(filtered);
                    availableTable.setPlaceholder(new Label("No content found"));
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    availableTable.setPlaceholder(new Label("Error fetching books"));
                    showAlert("Error", "Failed to fetch online books: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        });
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        logoutLogic();
    }
    
    @FXML
    private void handleLogout(ActionEvent event) {
        logoutLogic();
    }
    
    private void logoutLogic() {
        LibraryManager.getInstance().logout();
        mainApp.showLoginView();
    }

    @FXML
    private void handleBorrowBook(ActionEvent event) {
        Book selected = availableTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a book from the Search Results to borrow.", Alert.AlertType.WARNING);
            return;
        }

        String studentId = LibraryManager.getInstance().getCurrentUser().getId();
        String result = LibraryManager.getInstance().borrowBookAsStudent(selected, studentId);
        
        if ("Success".equals(result)) {
            showAlert("Success", "You have successfully borrowed: " + selected.getTitle(), Alert.AlertType.INFORMATION);
            refreshTables();
            mainTabPane.getSelectionModel().select(0);
        } else {
            showAlert("Borrow Failed", result, Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleReturnBook(ActionEvent event) {
        Book selected = borrowedTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a checked out book to renew/return.", Alert.AlertType.WARNING);
            return;
        }

        String studentId = LibraryManager.getInstance().getCurrentUser().getId();
        LibraryManager.getInstance().returnBookAsStudent(selected, studentId);
        
        showAlert("Returned/Renewed", "You have returned: " + selected.getTitle(), Alert.AlertType.INFORMATION);
        refreshTables();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
