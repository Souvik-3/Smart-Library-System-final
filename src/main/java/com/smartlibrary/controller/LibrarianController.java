package com.smartlibrary.controller;

import com.smartlibrary.model.Book;
import com.smartlibrary.model.LibraryManager;
import com.smartlibrary.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class LibrarianController extends BaseController implements Initializable {

    // Book Tab
    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> colId;
    @FXML private TableColumn<Book, String> colTitle;
    @FXML private TableColumn<Book, String> colAuthor;
    @FXML private TableColumn<Book, String> colStatus;
    @FXML private TableColumn<Book, String> colIssuedTo;

    @FXML private TextField searchField;
    @FXML private TextField bookIdField;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField issueToField;

    // Member Tab
    @FXML private TableView<User> memberTable;
    @FXML private TableColumn<User, String> colMemberId;
    @FXML private TableColumn<User, String> colMemberName;
    @FXML private TableColumn<User, String> colMemberRole;
    
    @FXML private TextField newMemberIdField;
    @FXML private TextField newMemberNameField;
    @FXML private PasswordField newMemberPassField;
    @FXML private ChoiceBox<String> newMemberRoleBox;

    private ObservableList<Book> bookList;
    private ObservableList<User> memberList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Books
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colIssuedTo.setCellValueFactory(new PropertyValueFactory<>("issuedTo"));

        // Members
        colMemberId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMemberName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMemberRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        newMemberRoleBox.setItems(FXCollections.observableArrayList("STUDENT", "LIBRARIAN"));
        newMemberRoleBox.setValue("STUDENT");

        refreshTable();
        refreshMembers();
        
        // Auto-fill fields when a book is selected for easy editing
        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                bookIdField.setText(newVal.getId());
                titleField.setText(newVal.getTitle());
                authorField.setText(newVal.getAuthor());
            }
        });
    }

    private void refreshTable() {
        bookList = FXCollections.observableArrayList(LibraryManager.getInstance().getBooks());
        bookTable.setItems(bookList);
    }
    
    private void refreshMembers() {
        memberList = FXCollections.observableArrayList(LibraryManager.getInstance().getUsers());
        memberTable.setItems(memberList);
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().toLowerCase();
        if (query.isEmpty()) {
            refreshTable();
            return;
        }
        ObservableList<Book> filtered = FXCollections.observableArrayList(
            LibraryManager.getInstance().getBooks().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(query) || b.getAuthor().toLowerCase().contains(query))
                .collect(Collectors.toList())
        );
        bookTable.setItems(filtered);
    }

    @FXML
    private void handleAddBook(ActionEvent event) {
        String id = bookIdField.getText().trim();
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        if (!id.isEmpty() && !title.isEmpty()) {
            Book b = new Book(id, title, author, "Available", "");
            LibraryManager.getInstance().addBook(b);
            refreshTable();
            clearFields();
        } else {
            showAlert("Error", "ID and Title are required.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditBook(ActionEvent event) {
        Book selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Select a book to edit first.", Alert.AlertType.WARNING);
            return;
        }
        
        String id = bookIdField.getText().trim();
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        
        if (!id.isEmpty() && !title.isEmpty()) {
            Book newBook = new Book(id, title, author, selected.getStatus(), selected.getIssuedTo());
            LibraryManager.getInstance().updateBook(selected, newBook);
            refreshTable();
            clearFields();
            showAlert("Success", "Book updated.", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Error", "ID and Title are required.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRemoveBook(ActionEvent event) {
        Book selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            LibraryManager.getInstance().removeBook(selected);
            refreshTable();
            clearFields();
        }
    }

    @FXML
    private void handleIssueBook(ActionEvent event) {
        Book selected = bookTable.getSelectionModel().getSelectedItem();
        String studentId = issueToField.getText().trim();
        if (selected != null && !studentId.isEmpty() && "Available".equals(selected.getStatus())) {
            LibraryManager.getInstance().issueBook(selected, studentId);
            refreshTable();
        } else {
            showAlert("Issue Error", "Book not selected, student ID missing, or book not available.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleReturnBook(ActionEvent event) {
        Book selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected != null && "Issued".equals(selected.getStatus())) {
            LibraryManager.getInstance().returnBook(selected);
            refreshTable();
        } else {
            showAlert("Return Error", "Book not selected or not issued.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleRegisterMember(ActionEvent event) {
        String id = newMemberIdField.getText().trim();
        String name = newMemberNameField.getText().trim();
        String pass = newMemberPassField.getText();
        String role = newMemberRoleBox.getValue();
        
        if (id.isEmpty() || name.isEmpty() || pass.isEmpty() || role == null) {
            showAlert("Error", "All fields are required to register a member.", Alert.AlertType.ERROR);
            return;
        }
        
        if (LibraryManager.getInstance().getUsers().stream().anyMatch(u -> u.getId().equals(id))) {
            showAlert("Error", "User ID already exists.", Alert.AlertType.ERROR);
            return;
        }
        
        User u = new User(id, name, pass, role);
        LibraryManager.getInstance().addUser(u);
        refreshMembers();
        
        newMemberIdField.clear();
        newMemberNameField.clear();
        newMemberPassField.clear();
        showAlert("Success", "Member registered successfully.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        LibraryManager.getInstance().logout();
        mainApp.showLoginView();
    }

    private void clearFields() {
        bookIdField.clear();
        titleField.clear();
        authorField.clear();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
