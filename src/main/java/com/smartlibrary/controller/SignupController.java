package com.smartlibrary.controller;

import com.smartlibrary.model.LibraryManager;
import com.smartlibrary.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.RadioButton;
import javafx.fxml.Initializable;
import javafx.application.Platform;
import java.net.URL;
import java.util.ResourceBundle;

public class SignupController extends BaseController implements Initializable {

    @FXML private TextField nameField;
    @FXML private TextField idField;
    @FXML private PasswordField passwordField;
    @FXML private RadioButton radioLibrarian;
    @FXML private RadioButton radioStudent;
    @FXML private Label errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            if (nameField.getParent() != null) {
                nameField.getParent().requestFocus();
            }
        });
    }

    @FXML
    private void handleSignupAction(ActionEvent event) {
        String name = nameField.getText().trim();
        String id = idField.getText().trim();
        String pass = passwordField.getText();
        String role = radioLibrarian.isSelected() ? "LIBRARIAN" : "STUDENT";

        if (name.isEmpty() || id.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }

        // Check if ID exists
        if (LibraryManager.getInstance().getUsers().stream().anyMatch(u -> u.getId().equals(id))) {
            errorLabel.setText("User ID already exists.");
            return;
        }

        User newUser = new User(id, name, pass, role);
        LibraryManager.getInstance().addUser(newUser);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Account Created");
        alert.setContentText("Your account has been successfully created. You can now log in.");
        alert.showAndWait();

        mainApp.showLoginView();
    }

    @FXML
    private void handleBackAction(ActionEvent event) {
        mainApp.showLoginView();
    }
}
