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
import javafx.scene.input.KeyEvent;
import javafx.fxml.Initializable;
import javafx.application.Platform;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController extends BaseController implements Initializable {

    @FXML
    private TextField userIdField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private RadioButton radioLibrarian;

    @FXML
    private RadioButton radioStudent;

    @FXML
    private Label errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            if (userIdField.getParent() != null) {
                userIdField.getParent().requestFocus();
            }
        });
    }

    @FXML
    private void handleLoginAction(ActionEvent event) {
        attemptLogin();
    }

    @FXML
    private void handleEnterPressed(KeyEvent event) {
        if (event.getCode().toString().equals("ENTER")) {
            attemptLogin();
        }
    }

    @FXML
    private void handleSignupAction(ActionEvent event) {
        mainApp.showSignupView();
    }

    private void attemptLogin() {
        String id = userIdField.getText().trim();
        String pass = passwordField.getText();
        String selectedRole = radioLibrarian.isSelected() ? "LIBRARIAN" : "STUDENT";

        if (id.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please enter User ID and Password.");
            return;
        }

        User user = LibraryManager.getInstance().authenticate(id, pass);
        if (user != null) {
            if (!user.getRole().equalsIgnoreCase(selectedRole)) {
                errorLabel.setText("Role mismatch. Please select the correct role tab.");
                return;
            }
            errorLabel.setText("");
            if ("LIBRARIAN".equalsIgnoreCase(user.getRole())) {
                mainApp.showLibrarianDashboard();
            } else {
                mainApp.showStudentDashboard();
            }
        } else {
            errorLabel.setText("Invalid credentials.");
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("Login Failed");
            alert.setContentText("Invalid ID or Password.");
            alert.showAndWait();
        }
    }
}
