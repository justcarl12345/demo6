package com.example.demo6;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class SceneManager {
    private static Stage primaryStage;
    private static FXMLLoader currentLoader;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static FXMLLoader getCurrentLoader() {
        return currentLoader;
    }

    public static void switchTo(String fxmlFileName) {
        switchTo(fxmlFileName, 800, 600);
    }

    public static void switchTo(String fxmlFileName, int width, int height) {
        if (primaryStage != null) {
            switchScene(primaryStage, fxmlFileName, width, height);
        }
    }

    public static void switchScene(Stage stage, String fxmlFileName, int width, int height) {
        if (!fxmlFileName.endsWith(".fxml")) {
            fxmlFileName += ".fxml";
        }

        String fxmlPath = "/com/example/demo6/" + fxmlFileName;
        URL fxmlUrl = SceneManager.class.getResource(fxmlPath);

        if (fxmlUrl == null) {
            System.err.println("FXML file not found: " + fxmlPath);
            return;
        }

        try {
            currentLoader = new FXMLLoader(fxmlUrl);
            Parent root = currentLoader.load();
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load FXML: " + fxmlFileName);
            e.printStackTrace();
        }
    }
}