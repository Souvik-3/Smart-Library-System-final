package com.smartlibrary.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class LabComponentsDemoController extends BaseController implements Initializable {

    @FXML private AnchorPane mainPane;

    @FXML private ListView<String> fruitListView;
    @FXML private Label fruitLabel;

    @FXML private TreeView<String> categoryTreeView;
    @FXML private Label treeLabel;

    @FXML private TextField progressInput;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    private int currentSum = 0;

    @FXML private Slider fontSlider;
    @FXML private Label sliderLabel;

    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Label spinnerLabel;

    @FXML private TextArea noteArea;

    @FXML private PasswordField pwdField;
    @FXML private TextField pwdVisibleField;
    @FXML private Button togglePwdBtn;

    @FXML private ImageView imageView;

    @FXML private ToggleGroup genderGroup;
    @FXML private ToggleGroup levelGroup;
    @FXML private Label genderLabel;

    @FXML private CheckBox chkReading;
    @FXML private CheckBox chkGaming;
    @FXML private CheckBox chkTraveling;
    @FXML private Label hobbyLabel;

    @FXML private ChoiceBox<String> colorChoiceBox;
    @FXML private ComboBox<String> countryComboBox;

    @FXML private DatePicker dobPicker;
    @FXML private Label dobLabel;

    @FXML private ColorPicker colorPicker;
    @FXML private Label coloredLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fruitListView.setItems(FXCollections.observableArrayList("Apple", "Banana", "Cherry", "Mango"));
        fruitListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) fruitLabel.setText("Selected Fruit: " + newVal);
        });

        TreeItem<String> root = new TreeItem<>("Categories");
        TreeItem<String> c1 = new TreeItem<>("Fiction");
        TreeItem<String> c2 = new TreeItem<>("Science");
        root.getChildren().addAll(c1, c2);
        categoryTreeView.setRoot(root);
        categoryTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) treeLabel.setText("Selected Node: " + newVal.getValue());
        });

        fontSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            sliderLabel.setStyle("-fx-font-size: " + newVal.doubleValue() + "px;");
        });

        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));

        colorChoiceBox.setItems(FXCollections.observableArrayList("Red", "Green", "Blue"));
        colorChoiceBox.setOnAction(e -> {
            String color = colorChoiceBox.getValue();
            if ("Red".equals(color)) mainPane.setStyle("-fx-background-color: #ffcccc;");
            else if ("Green".equals(color)) mainPane.setStyle("-fx-background-color: #ccffcc;");
            else if ("Blue".equals(color)) mainPane.setStyle("-fx-background-color: #ccccff;");
        });

        countryComboBox.setItems(FXCollections.observableArrayList("Bangladesh", "USA", "UK", "Canada"));

        colorPicker.setOnAction(e -> {
            String hex = String.format("#%02X%02X%02X",
                (int)(colorPicker.getValue().getRed() * 255),
                (int)(colorPicker.getValue().getGreen() * 255),
                (int)(colorPicker.getValue().getBlue() * 255));
            coloredLabel.setStyle("-fx-text-fill: " + hex + ";");
        });

        dobPicker.setOnAction(e -> {
            if (dobPicker.getValue() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                dobLabel.setText("DOB: " + dobPicker.getValue().format(formatter));
            }
        });

        pwdVisibleField.textProperty().bindBidirectional(pwdField.textProperty());
        pwdVisibleField.setManaged(false);
        pwdVisibleField.setVisible(false);
    }

    @FXML private void handleAccumulate() {
        try {
            int target = Integer.parseInt(progressInput.getText());
            currentSum++;
            double progress = (double) currentSum / target;
            progressBar.setProgress(progress);
            progressLabel.setText("Sum: " + currentSum);
            if (currentSum >= target) {
                progressBar.setProgress(1.0);
            }
        } catch (NumberFormatException e) { }
    }

    @FXML private void handleSpinnerShow() {
        spinnerLabel.setText("Quantity: " + quantitySpinner.getValue());
    }

    @FXML private void handleShowInfo() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Information"); a.setContentText("This is an info alert."); a.showAndWait();
    }

    @FXML private void handleShowWarning() {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Warning"); a.setContentText("This is a warning alert."); a.showAndWait();
    }

    @FXML private void handleShowError() {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setContentText("This is an error alert."); a.showAndWait();
    }

    @FXML private void handleClearNote() {
        noteArea.clear();
    }

    @FXML private void handleTogglePassword() {
        if (pwdField.isVisible()) {
            pwdField.setVisible(false);
            pwdField.setManaged(false);
            pwdVisibleField.setVisible(true);
            pwdVisibleField.setManaged(true);
            togglePwdBtn.setText("Hide");
        } else {
            pwdVisibleField.setVisible(false);
            pwdVisibleField.setManaged(false);
            pwdField.setVisible(true);
            pwdField.setManaged(true);
            togglePwdBtn.setText("Show");
        }
    }

    @FXML private void handleBrowseImage() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            imageView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML private void handleChangeImageFromResource() {
        try {
            Image img = new Image(getClass().getResourceAsStream("/images/default.jpg"));
            imageView.setImage(img);
        } catch (Exception e) {
            System.out.println("Image not found in resources");
        }
    }

    @FXML private void handleGenderSelection() {
        RadioButton selected = (RadioButton) genderGroup.getSelectedToggle();
        if (selected != null) genderLabel.setText("Gender: " + selected.getText());
    }

    @FXML private void handleHobbySubmit() {
        StringBuilder sb = new StringBuilder("Hobbies: ");
        if (chkReading.isSelected()) sb.append("Reading ");
        if (chkGaming.isSelected()) sb.append("Gaming ");
        if (chkTraveling.isSelected()) sb.append("Traveling ");
        hobbyLabel.setText(sb.toString());
    }
}
