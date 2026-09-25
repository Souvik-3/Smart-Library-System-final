package com.smartlibrary.controller;

import com.smartlibrary.model.Book;
import com.smartlibrary.model.LibraryManager;
import com.smartlibrary.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StudentController extends BaseController implements Initializable {

    @FXML private Label welcomeLabel;

    @FXML private TableView<Book> availableTable;
    @FXML private TableColumn<Book, String> colAvailId;
    @FXML private TableColumn<Book, String> colAvailTitle;
    @FXML private TableColumn<Book, String> colAvailAuthor;

    @FXML private TableView<Book> borrowedTable;
    @FXML private TableColumn<Book, String> colBorrId;
    @FXML private TableColumn<Book, String> colBorrTitle;
    @FXML private TableColumn<Book, String> colBorrAuthor;

    @FXML private TextField searchField;

    private ObservableList<Book> availableBooks;
    private ObservableList<Book> borrowedBooks;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colAvailId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAvailTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAvailAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));

        colBorrId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBorrTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colBorrAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
    }

    @Override
    public void setMain(com.smartlibrary.Main mainApp) {
        super.setMain(mainApp);
        User currentUser = LibraryManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getName());
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
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().toLowerCase();
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
    private void handleLogout(ActionEvent event) {
        LibraryManager.getInstance().logout();
        mainApp.showLoginView();
    }

    @FXML
    private void handleBorrowBook(ActionEvent event) {
        Book selected = availableTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a book from the Available Books table to borrow.", Alert.AlertType.WARNING);
            return;
        }

        String studentId = LibraryManager.getInstance().getCurrentUser().getId();
        String result = LibraryManager.getInstance().borrowBookAsStudent(selected, studentId);
        
        if ("Success".equals(result)) {
            showAlert("Success", "You have successfully borrowed: " + selected.getTitle(), Alert.AlertType.INFORMATION);
            refreshTables();
        } else {
            showAlert("Borrow Failed", result, Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleReturnBook(ActionEvent event) {
        Book selected = borrowedTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a book from your Borrowed Books table to return.", Alert.AlertType.WARNING);
            return;
        }

        String studentId = LibraryManager.getInstance().getCurrentUser().getId();
        LibraryManager.getInstance().returnBookAsStudent(selected, studentId);
        
        showAlert("Returned", "You have returned: " + selected.getTitle(), Alert.AlertType.INFORMATION);
        refreshTables();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
