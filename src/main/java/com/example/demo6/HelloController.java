package com.example.demo6;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HelloController {
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField amountField;
    @FXML
    private Button addIncomeButton;
    @FXML
    private Button addExpenseButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Label userLabel;
    @FXML
    private Label balanceLabel;
    @FXML
    private ToggleButton darkModeToggle;
    @FXML
    private LineChart<String, Number> transactionChart;
    @FXML
    private CategoryAxis monthAxis;
    @FXML
    private NumberAxis moneyAxis;
    @FXML
    private TableView<Transaction> transactionTable;
    @FXML
    private TableColumn<Transaction, String> dateColumn;
    @FXML
    private TableColumn<Transaction, String> descriptionColumn;
    @FXML
    private TableColumn<Transaction, Double> amountColumn;
    @FXML
    private TableColumn<Transaction, String> timeColumn;
    @FXML
    private TableColumn<Transaction, String> typeColumn;
    @FXML
    private TableView<Summary> summaryTable;
    @FXML
    private TableColumn<Summary, String> summaryMonthColumn;
    @FXML
    private TableColumn<Summary, Double> summaryIncomeColumn;
    @FXML
    private TableColumn<Summary, Double> summaryExpenseColumn;
    @FXML
    private ProgressBar budgetProgressBar;
    @FXML
    private Label budgetSpentLabel;
    @FXML
    private Label budgetTotalLabel;
    @FXML
    private Label budgetPercentLabel;
    @FXML
    private Label monthIncomeLabel;
    @FXML
    private Label monthExpenseLabel;
    @FXML
    private Label monthSavingsLabel;
    @FXML
    private ListView<String> recentTransactionsList;
    @FXML
    private VBox budgetBox;
    @FXML
    private VBox statsBox;
    @FXML
    private VBox recentTransactionsBox;

    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private ObservableList<Summary> summaries = FXCollections.observableArrayList();
    private double balance = 0.0;
    private final StringProperty currentUser = new SimpleStringProperty();
    private boolean darkMode = false;
    private double monthlyBudget = 2500.0;

    @FXML
    public void initialize() {
        String loggedUser = UserSession.getLoggedInUser();
        currentUser.set(loggedUser);

        if (userLabel != null && loggedUser != null) {
            userLabel.setText(loggedUser);
        }

        if (loggedUser != null) {
            transactions = UserStorage.loadTransactions(loggedUser);
        }

        setupTransactionTable();
        setupSummaryTable();
        initializeChart();
        setupDashboard();

        deleteButton.setDisable(true);
        transactionTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> deleteButton.setDisable(newVal == null)
        );

        calculateBalance();
        updateDashboard();

        darkModeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            darkMode = newVal;
            applyDarkMode();
        });
    }

    private void setupDashboard() {
        recentTransactionsList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    if (item.contains("+")) {
                        setStyle("-fx-text-fill: #4CAF50;");
                    } else {
                        setStyle("-fx-text-fill: #f44336;");
                    }
                }
            }
        });
    }

    private void updateDashboard() {
        double monthlyIncome = transactions.stream()
                .filter(t -> isCurrentMonth(t) && t.getAmount() > 0)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double monthlyExpense = transactions.stream()
                .filter(t -> isCurrentMonth(t) && t.getAmount() < 0)
                .mapToDouble(t -> Math.abs(t.getAmount()))
                .sum();

        double monthlySavings = monthlyIncome - monthlyExpense;
        double budgetProgress = monthlyExpense / monthlyBudget;

        budgetProgressBar.setProgress(budgetProgress);
        budgetSpentLabel.setText(String.format("₱%.2f", monthlyExpense));
        budgetTotalLabel.setText(String.format("₱%.2f", monthlyBudget));
        budgetPercentLabel.setText(String.format("(%.0f%%)", budgetProgress * 100));

        monthIncomeLabel.setText(String.format("₱%.2f", monthlyIncome));
        monthExpenseLabel.setText(String.format("₱%.2f", monthlyExpense));
        monthSavingsLabel.setText(String.format("₱%.2f", monthlySavings));

        recentTransactionsList.setItems(FXCollections.observableArrayList(
                transactions.stream()
                        .sorted((t1, t2) -> t2.getDateTime().compareTo(t1.getDateTime()))
                        .limit(5)
                        .map(t -> String.format("%s: %s₱%.2f - %s",
                                t.getDateTime().format(DateTimeFormatter.ofPattern("MMM dd")),
                                t.getAmount() >= 0 ? "+" : "",
                                Math.abs(t.getAmount()),
                                t.getDescription()))
                        .collect(Collectors.toList())
        ));
    }

    private boolean isCurrentMonth(Transaction t) {
        return t.getDateTime().getMonth() == LocalDateTime.now().getMonth() &&
                t.getDateTime().getYear() == LocalDateTime.now().getYear();
    }

    private void setupTransactionTable() {
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().formattedDateProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().formattedTimeProperty());
        typeColumn.setCellValueFactory(cellData -> {
            double amount = cellData.getValue().getAmount();
            return new SimpleStringProperty(amount >= 0 ? "Income" : "Expense");
        });
        transactionTable.setItems(transactions);
    }

    private void setupSummaryTable() {
        summaryMonthColumn.setCellValueFactory(cellData -> cellData.getValue().monthProperty());
        summaryIncomeColumn.setCellValueFactory(cellData -> cellData.getValue().incomeProperty().asObject());
        summaryExpenseColumn.setCellValueFactory(cellData -> cellData.getValue().expenseProperty().asObject());
        summaryTable.setItems(summaries);
        updateSummaryTable();
    }

    private void updateSummaryTable() {
        Map<Month, Double[]> monthlyData = new HashMap<>();
        for (Transaction t : transactions) {
            Month month = t.getDateTime().getMonth();
            double amount = t.getAmount();
            monthlyData.putIfAbsent(month, new Double[]{0.0, 0.0});
            Double[] values = monthlyData.get(month);
            if (amount >= 0) {
                values[0] += amount;
            } else {
                values[1] += Math.abs(amount);
            }
        }
        summaries.clear();
        for (Month month : Month.values()) {
            Double[] values = monthlyData.getOrDefault(month, new Double[]{0.0, 0.0});
            if (values[0] > 0 || values[1] > 0) {
                summaries.add(new Summary(month.name(), values[0], values[1]));
            }
        }
    }

    private void initializeChart() {
        transactionChart.setTitle("Monthly Income vs Expenses");
        monthAxis.setLabel("Month");
        moneyAxis.setLabel("Amount (₱)");
        updateChart();
    }

    private void updateChart() {
        transactionChart.getData().clear();
        Map<Month, Double[]> monthlyData = new HashMap<>();
        for (Transaction t : transactions) {
            Month month = t.getDateTime().getMonth();
            double amount = t.getAmount();
            monthlyData.putIfAbsent(month, new Double[]{0.0, 0.0});
            Double[] values = monthlyData.get(month);
            if (amount >= 0) {
                values[0] += amount;
            } else {
                values[1] += Math.abs(amount);
            }
        }
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");
        for (Month month : Month.values()) {
            String monthName = month.toString();
            Double[] values = monthlyData.getOrDefault(month, new Double[]{0.0, 0.0});
            incomeSeries.getData().add(new XYChart.Data<>(monthName, values[0]));
            expenseSeries.getData().add(new XYChart.Data<>(monthName, values[1]));
        }
        transactionChart.getData().addAll(incomeSeries, expenseSeries);
    }

    private void calculateBalance() {
        balance = transactions.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
        if (balanceLabel != null) {
            balanceLabel.setText(String.format("Current Balance: ₱%.2f", balance));
        }
    }

    @FXML
    private void handleAddIncome() {
        addTransaction(true);
    }

    @FXML
    private void handleAddExpense() {
        addTransaction(false);
    }

    @FXML
    private void handleDeleteTransaction() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            transactions.remove(selected);
            balance -= selected.getAmount();
            updateChart();
            updateSummaryTable();
            calculateBalance();
            saveData();
            updateDashboard();
        }
    }

    private void addTransaction(boolean isIncome) {
        String description = descriptionField.getText().trim();
        String amountText = amountField.getText().trim();

        if (description.isEmpty() || amountText.isEmpty()) {
            showAlert("Error", "Please fill all fields");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showAlert("Error", "Amount must be positive");
                return;
            }

            if (!isIncome) amount = -amount;

            Transaction transaction = new Transaction(description, amount, LocalDateTime.now());
            transactions.add(transaction);
            balance += amount;

            descriptionField.clear();
            amountField.clear();

            updateChart();
            updateSummaryTable();
            calculateBalance();
            saveData();
            updateDashboard();

            if (darkMode) {
                applyDarkMode();
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid amount format");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void saveData() {
        if (currentUser.get() != null) {
            UserStorage.saveTransactions(currentUser.get(), transactions);
        }
    }

    @FXML
    private void toggleDarkMode() {
        darkMode = !darkMode;
        applyDarkMode();
    }

    private void applyDarkMode() {
        if (darkMode) {
            String darkTableStyle = "-fx-control-inner-background: #2d2d2d; " +
                    "-fx-background-color: #2d2d2d; " +
                    "-fx-text-fill: white; " +
                    "-fx-table-cell-border-color: #444;";

            transactionTable.setStyle(darkTableStyle);
            summaryTable.setStyle(darkTableStyle);
            styleAllTableText(transactionTable, true);
            styleAllTableText(summaryTable, true);

            transactionChart.setStyle("-fx-background-color: #1e1e1e;");
            monthAxis.setStyle("-fx-tick-label-fill: white; -fx-text-fill: white;");
            moneyAxis.setStyle("-fx-tick-label-fill: white; -fx-text-fill: white;");
            transactionChart.getXAxis().setStyle("-fx-tick-label-fill: white;");
            transactionChart.getYAxis().setStyle("-fx-tick-label-fill: white;");
            transactionChart.lookup(".chart-title").setStyle("-fx-text-fill: white;");
            transactionChart.lookup(".chart-legend").setStyle("-fx-text-fill: white;");

            descriptionField.setStyle("-fx-background-color: #3d3d3d; -fx-text-fill: white;");
            amountField.setStyle("-fx-background-color: #3d3d3d; -fx-text-fill: white;");
            darkModeToggle.setText("Light Mode");

            String darkPanelStyle = "-fx-background-color: #2d2d2d; -fx-text-fill: white;";
            budgetBox.setStyle(darkPanelStyle);
            statsBox.setStyle(darkPanelStyle);
            recentTransactionsBox.setStyle(darkPanelStyle);
            recentTransactionsList.setStyle("-fx-control-inner-background: #2d2d2d; -fx-text-fill: white;");
            budgetProgressBar.setStyle("-fx-accent: #4CAF50;");

            styleAllLabels(true);
        } else {
            resetTableStyles(transactionTable);
            resetTableStyles(summaryTable);

            transactionChart.setStyle("-fx-background-color: pink;");
            monthAxis.setStyle("");
            moneyAxis.setStyle("");
            transactionChart.getXAxis().setStyle("");
            transactionChart.getYAxis().setStyle("");

            descriptionField.setStyle("");
            amountField.setStyle("");
            darkModeToggle.setText("Dark Mode");

            budgetBox.setStyle("");
            statsBox.setStyle("");
            recentTransactionsBox.setStyle("");
            recentTransactionsList.setStyle("");
            budgetProgressBar.setStyle("");

            styleAllLabels(false);
        }
    }

    private void styleAllTableText(TableView<?> table, boolean darkMode) {
        table.lookupAll(".column-header").forEach(node ->
                node.setStyle(darkMode ?
                        "-fx-background-color: #1e1e1e; -fx-text-fill: white;" :
                        ""));

        table.lookupAll(".table-cell").forEach(node ->
                node.setStyle(darkMode ?
                        "-fx-text-fill: white; -fx-border-color: #444;" :
                        ""));

        table.lookupAll(".table-row-cell").forEach(node ->
                node.setStyle(darkMode ?
                        "-fx-background-color: #2d2d2d; -fx-text-fill: white;" :
                        ""));
    }

    private void styleAllLabels(boolean darkMode) {
        Parent root = transactionTable.getScene().getRoot();
        root.lookupAll(".label").forEach(node ->
                node.setStyle(darkMode ? "-fx-text-fill: white;" : ""));

        if (balanceLabel != null) {
            balanceLabel.setStyle(darkMode ? "-fx-text-fill: white;" : "-fx-font-weight: bold; -fx-font-size: 14px;");
        }
    }

    private void resetTableStyles(TableView<?> table) {
        table.setStyle("");
        Node tableView = table.lookup(".table-view");
        if (tableView != null) {
            tableView.setStyle("");
        }
        table.lookupAll(".column-header").forEach(node -> node.setStyle(""));
        table.lookupAll(".table-cell").forEach(node -> node.setStyle(""));
        table.lookupAll(".table-row-cell").forEach(node -> node.setStyle(""));
    }

    @FXML
    private void handleLogout() {
        saveData();
        UserSession.clear();
        currentUser.set(null);
        SceneManager.switchTo("login.fxml");
    }
}