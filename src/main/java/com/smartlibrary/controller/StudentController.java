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
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.net.URL;
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
        // Setup Search Choice
        searchChoice.setItems(FXCollections.observableArrayList("Library catalog", "Title", "Author"));
        searchChoice.setValue("Library catalog");

        // Setup Available Books Table
        colAvailTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAvailAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colAvailStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup Borrowed Books Table (Mix of real and dummy data to match OPAC screenshot)
        colBorrTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colBorrAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        
        // Dummy values for visual matching of the assignment screenshot
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
            welcomeLabel.setText("Hello, " + currentUser.getName().toUpperCase());
            breadcrumbNameLabel.setText(currentUser.getName().toUpperCase());
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
        
        // Update label count
        checkedOutCountLabel.setText(borrowedBooks.size() + " Item(s) checked out");
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().toLowerCase();
        
        // Switch to the Available Books tab to show results
        mainTabPane.getSelectionModel().select(1);

        if (query.isEmpty()) {
            refreshTables();
            return;
        }

        ObservableList<Book> filtered = FXCollections.observableArrayList(
            availableBooks.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(query) || b.getAuthor().toLowerCase().contains(query))
                .collect(Collectors.toList())
        );
        availableTable.setItems(filtered);
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
            // Automatically switch back to Checked Out tab to show the new book
            mainTabPane.getSelectionModel().select(0);
        } else {
            showAlert("Borrow Failed", result, Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleReturnBook(ActionEvent event) {
        // Tied to the OPAC "Renew" buttons for functionality purposes
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
