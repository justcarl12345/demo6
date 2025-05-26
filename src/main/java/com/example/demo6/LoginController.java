package com.example.demo6;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.collections.transformation.FilteredList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.input.KeyCode;

import java.util.Map;

public class LoginController {
    @FXML private ListView<String> accountListView;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField searchField;
    @FXML private Button loginButton;
    @FXML private Button signupButton;
    @FXML private Button deleteButton;
    @FXML private Label statusLabel;

    private Map<String, String> userDatabase = UserStorage.loadUsers();
    private ObservableList<String> accountList;      // <--- Add this
    private FilteredList<String> filteredAccounts;

    @FXML
    private void initialize() {
        statusLabel.setText("");
        statusLabel.setMinHeight(Region.USE_PREF_SIZE);
        passwordField.setOnKeyPressed(this::handleEnterKey);

        // Initialize accountList and filteredAccounts correctly
        accountList = FXCollections.observableArrayList(userDatabase.keySet());
        filteredAccounts = new FilteredList<>(accountList);
        accountListView.setItems(filteredAccounts);

        // Set custom cell factory for avatars
        accountListView.setCellFactory(lv -> new ListCell<String>() {
            private final ImageView avatarView = new ImageView();
            private final Circle clip = new Circle(15, 15, 15);

            {
                avatarView.setFitWidth(30);
                avatarView.setFitHeight(30);
                avatarView.setClip(clip);
                setGraphic(avatarView);
            }

            @Override
            protected void updateItem(String account, boolean empty) {
                super.updateItem(account, empty);
                if (empty || account == null) {
                    setText(null);
                    avatarView.setImage(null);
                } else {
                    setText(account);
                    avatarView.setImage(createAvatarImage(account));
                }
            }
        });

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredAccounts.setPredicate(account ->
                    newVal == null || newVal.isEmpty() ||
                            account.toLowerCase().contains(newVal.toLowerCase()));
        });

        // Account selection handler
        accountListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        usernameField.setText(newVal);
                        passwordField.requestFocus();
                        deleteButton.setDisable(false);
                    } else {
                        deleteButton.setDisable(true);
                    }
                }
        );

        deleteButton.setDisable(true);
    }

    private Image createAvatarImage(String username) {
        // Placeholder avatar - in a real app you might generate one based on username
        try {
            return new Image(getClass().getResourceAsStream("/com/example/demo6/default-avatar.png"));
        } catch (Exception e) {
            // Fallback to empty image if avatar not found
            return null;
        }
    }

    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onLogin(null);
        }
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required.");
            return;
        }

        if (userDatabase.containsKey(username) && userDatabase.get(username).equals(password)) {
            showSuccess("Welcome, " + username + "!");
            UserSession.setLoggedInUser(username);
            clearFields();
            SceneManager.switchTo("main-view");
        } else {
            showError("Invalid username or password.");
        }
    }

    @FXML
    private void onSignup(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Choose a username and password.");
            return;
        }

        if (userDatabase.containsKey(username)) {
            showError("Username already exists.");
        } else {
            userDatabase.put(username, password);
            UserStorage.saveUsers(userDatabase);
            accountList.add(username);     // <--- changed here
            showSuccess("Account created. You can now log in.");
            clearFields();
        }
    }

    @FXML
    private void onDeleteAccount(ActionEvent event) {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            showError("No account selected");
            return;
        }

        if (!userDatabase.containsKey(username)) {
            showError("Account doesn't exist");
            return;
        }

        // Confirm deletion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Account");
        alert.setContentText("Are you sure you want to delete account '" + username + "'? All transaction data will be lost.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            userDatabase.remove(username);
            UserStorage.saveUsers(userDatabase);
            UserStorage.deleteTransactions(username);
            accountList.remove(username);   // <--- changed here
            clearFields();
            showSuccess("Account '" + username + "' deleted");
        }
    }

    private void showError(String message) {
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setText(message);
    }

    private void showSuccess(String message) {
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setText(message);
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        accountListView.getSelectionModel().clearSelection();
        deleteButton.setDisable(true);
    }
}

class UserSession {
    private static String loggedInUser;

    public static void setLoggedInUser(String username) {
        loggedInUser = username;
    }

    public static String getLoggedInUser() {
        return loggedInUser;
    }

    public static void clear() {
        loggedInUser = null;
    }
}