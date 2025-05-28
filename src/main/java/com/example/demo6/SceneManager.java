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
        switchTo(fxmlFileName, primaryStage.getWidth(), primaryStage.getHeight());
    }

    public static void switchTo(String fxmlFileName, double width, double height) {
        if (primaryStage != null) {
            boolean wasFullScreen = primaryStage.isFullScreen();

            try {
                String fxmlPath = "/com/example/demo6/" +
                        (fxmlFileName.endsWith(".fxml") ? fxmlFileName : fxmlFileName + ".fxml");
                URL fxmlUrl = SceneManager.class.getResource(fxmlPath);

                if (fxmlUrl == null) {
                    System.err.println("FXML file not found: " + fxmlPath);
                    return;
                }

                currentLoader = new FXMLLoader(fxmlUrl);
                Parent root = currentLoader.load();
                Scene scene = new Scene(root, width, height);
                primaryStage.setScene(scene);

                if (wasFullScreen) {
                    primaryStage.setFullScreen(true);
                }

            } catch (IOException e) {
                System.err.println("Failed to load FXML: " + fxmlFileName);
                e.printStackTrace();
            }
        }
    }
}