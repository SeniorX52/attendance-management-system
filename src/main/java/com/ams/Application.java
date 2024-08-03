package com.ams;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import models.Database;

import java.io.IOException;
import java.util.Objects;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Attendance Management System");
        Image icon=new Image(Objects.requireNonNull(getClass().getResource("/images/Book.png")).toExternalForm());
        stage.getIcons().add(icon);
        stage.setMaximized(true);
        SceneManager.primaryStage=stage;
        SceneManager.switchToLogin();
    }

    public static void main(String[] args) {
        launch();
    }
}