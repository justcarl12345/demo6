package com.example.demo6;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserStorage {
    private static final String USERS_FILE = "users.dat";
    private static final String TRANSACTIONS_DIR = "transactions/";

    public static void saveUsers(Map<String, String> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.err.println("Failed to save users: " + e.toString());
            e.printStackTrace();
        }
    }

    public static Map<String, String> loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return new HashMap<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            return (Map<String, String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load users: " + e.toString());
            return new HashMap<>();
        }
    }

    public static void saveTransactions(String username, ObservableList<Transaction> transactions) {
        try {
            Files.createDirectories(Paths.get(TRANSACTIONS_DIR));

            Path tempFile = Paths.get(TRANSACTIONS_DIR + username + ".tmp");
            Path finalFile = Paths.get(TRANSACTIONS_DIR + username + ".dat");

            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(tempFile))) {
                oos.writeObject(new ArrayList<>(transactions));
            }

            Files.move(tempFile, finalFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("Failed to save transactions for " + username + ": " + e.toString());
            e.printStackTrace();
        }
    }

    public static void deleteTransactions(String username) {
        try {
            Path filePath = Paths.get(TRANSACTIONS_DIR + username + ".dat");
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to delete transactions for " + username + ": " + e.getMessage());
        }
    }

    public static ObservableList<Transaction> loadTransactions(String username) {
        Path filePath = Paths.get(TRANSACTIONS_DIR + username + ".dat");
        if (!Files.exists(filePath)) return FXCollections.observableArrayList();

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filePath))) {
            List<Transaction> loaded = (List<Transaction>) ois.readObject();
            return FXCollections.observableArrayList(loaded);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load transactions for " + username + ": " + e.toString());
            return FXCollections.observableArrayList();
        }
    }
}