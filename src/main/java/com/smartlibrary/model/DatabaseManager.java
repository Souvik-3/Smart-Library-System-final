package com.smartlibrary.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:smartlibrary.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            String createUsersTable = "CREATE TABLE IF NOT EXISTS Users (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT, " +
                    "password TEXT, " +
                    "role TEXT)";
            
            String createBooksTable = "CREATE TABLE IF NOT EXISTS Books (" +
                    "id TEXT PRIMARY KEY, " +
                    "title TEXT, " +
                    "author TEXT, " +
                    "status TEXT, " +
                    "issuedTo TEXT)";
            
            stmt.execute(createUsersTable);
            stmt.execute(createBooksTable);
            
            // Insert admin if not exists
            String insertAdmin = "INSERT OR IGNORE INTO Users (id, name, password, role) " +
                    "VALUES ('L101', 'Admin', 'admin', 'LIBRARIAN')";
            stmt.execute(insertAdmin);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
