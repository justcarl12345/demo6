package com.example.demo6;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        SceneManager.setStage(stage);

        stage.setOnCloseRequest(e -> {
            if (UserSession.getLoggedInUser() != null) {
                FXMLLoader loader = SceneManager.getCurrentLoader();
                if (loader != null && loader.getController() instanceof HelloController) {
                    ((HelloController) loader.getController()).saveData();
                }
            }
        });

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/demo6/splash.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Personal Expenses Tracker");
        stage.setScene(scene);
        stage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
}