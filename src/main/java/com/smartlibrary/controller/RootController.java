package com.smartlibrary.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class RootController extends BaseController {

    @FXML
    private void handleNew(ActionEvent event) {
        if (mainApp != null) {
            mainApp.openLabDemoWindow();
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Platform.exit();
    }
}
