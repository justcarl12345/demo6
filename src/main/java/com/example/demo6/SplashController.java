package com.example.demo6;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;

public class SplashController {

    @FXML
    private ProgressBar progressBar;

    @FXML
    public void initialize() {
        new Thread(() -> {
            try {
                Thread.sleep(3000); // splash duration
                Platform.runLater(() -> {
                    // ⬇️ Ensure it switches to login-view, not main-view
                    SceneManager.switchTo("login.fxml");
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
