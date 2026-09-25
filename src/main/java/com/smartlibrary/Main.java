package com.smartlibrary;

import com.smartlibrary.controller.BaseController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private Stage primaryStage;
    private BorderPane rootLayout;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Smart Library Management System");
        initRootLayout();
        showLoginView();
    }

    public void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/view/RootLayout.fxml"));
            rootLayout = loader.load();

            BaseController controller = loader.getController();
            if (controller != null) {
                controller.setMain(this);
            }

            Scene scene = new Scene(rootLayout, 900, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource(fxmlPath));
            javafx.scene.Node view = loader.load();
            rootLayout.setCenter(view);

            Object controller = loader.getController();
            if (controller instanceof BaseController) {
                ((BaseController) controller).setMain(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showLoginView() {
        setView("/view/Login.fxml");
    }

    public void showSignupView() {
        setView("/view/Signup.fxml");
    }

    public void showLibrarianDashboard() {
        setView("/view/LibrarianDashboard.fxml");
    }

    public void showStudentDashboard() {
        setView("/view/StudentDashboard.fxml");
    }

    public void openLabDemoWindow() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/view/LabComponentsDemo.fxml"));
            javafx.scene.layout.AnchorPane page = loader.load();

            Stage demoStage = new Stage();
            demoStage.setTitle("Lab Components Demo");
            demoStage.initModality(Modality.NONE);
            demoStage.initOwner(primaryStage);
            Scene scene = new Scene(page, 800, 600);
            demoStage.setScene(scene);

            BaseController controller = loader.getController();
            if (controller != null) {
                controller.setMain(this);
            }

            demoStage.setOnHidden(e -> primaryStage.show());
            primaryStage.hide();
            demoStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
