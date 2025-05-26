package com.example.demo6;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.*;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient StringProperty description;
    private transient DoubleProperty amount;
    private transient ObjectProperty<LocalDateTime> dateTime;

    public Transaction(String description, double amount, LocalDateTime dateTime) {
        this.description = new SimpleStringProperty(description);
        this.amount = new SimpleDoubleProperty(amount);
        this.dateTime = new SimpleObjectProperty<>(dateTime);
    }

    // Getters
    public String getDescription() { return description.get(); }
    public double getAmount() { return amount.get(); }
    public LocalDateTime getDateTime() { return dateTime.get(); }

    // Property getters
    public StringProperty descriptionProperty() { return description; }
    public DoubleProperty amountProperty() { return amount; }
    public ObjectProperty<LocalDateTime> dateTimeProperty() { return dateTime; }

    // Formatted properties
    public StringProperty formattedDateProperty() {
        return new SimpleStringProperty(dateTime.get().toLocalDate().toString());
    }

    public StringProperty formattedTimeProperty() {
        return new SimpleStringProperty(
                dateTime.get().format(DateTimeFormatter.ofPattern("hh:mm a"))
        );
    }

    // Custom serialization
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(getDescription());
        oos.writeObject(getAmount());
        oos.writeObject(getDateTime());
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        String desc = (String) ois.readObject();
        double amt = (double) ois.readObject();
        LocalDateTime dt = (LocalDateTime) ois.readObject();

        this.description = new SimpleStringProperty(desc);
        this.amount = new SimpleDoubleProperty(amt);
        this.dateTime = new SimpleObjectProperty<>(dt);
    }
}

class Summary {
    private final StringProperty month;
    private final DoubleProperty income;
    private final DoubleProperty expense;

    public Summary(String month, double income, double expense) {
        this.month = new SimpleStringProperty(month);
        this.income = new SimpleDoubleProperty(income);
        this.expense = new SimpleDoubleProperty(expense);
    }

    public StringProperty monthProperty() {
        return month;
    }

    public DoubleProperty incomeProperty() {
        return income;
    }

    public DoubleProperty expenseProperty() {
        return expense;
    }

    public String getMonth() {
        return month.get();
    }

    public double getIncome() {
        return income.get();
    }

    public double getExpense() {
        return expense.get();
    }
}