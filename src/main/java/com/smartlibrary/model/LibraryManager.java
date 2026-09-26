package com.smartlibrary.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibraryManager {
    private static LibraryManager instance;
    private List<User> users;
    private List<Book> books;
    private User currentUser;
    
    // Thread pool for concurrency
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private LibraryManager() {
        users = new ArrayList<>();
        books = new ArrayList<>();
        
        // Initialize database asynchronously to demonstrate concurrency
        executorService.submit(() -> {
            DatabaseManager.initializeDatabase();
            loadUsersFromDB();
            loadBooksFromDB();
        });
        
        // Wait briefly for demo purposes to ensure lists populate initially
        try { Thread.sleep(500); } catch (InterruptedException e) {}
    }

    public static LibraryManager getInstance() {
        if (instance == null) {
            instance = new LibraryManager();
        }
        return instance;
    }
    
    public ExecutorService getExecutorService() {
        return executorService;
    }

    private synchronized void loadUsersFromDB() {
        users.clear();
        String query = "SELECT * FROM Users";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(new User(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private synchronized void loadBooksFromDB() {
        books.clear();
        String query = "SELECT * FROM Books";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                books.add(new Book(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("status"),
                        rs.getString("issuedTo")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User authenticate(String id, String password) {
        // Refresh users in case it changed elsewhere
        loadUsersFromDB();
        for (User u : users) {
            if (u.getId().equals(id) && u.getPassword().equals(password)) {
                currentUser = u;
                return u;
            }
        }
        return null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    public List<User> getUsers() { 
        loadUsersFromDB();
        return users; 
    }
    
    public List<Book> getBooks() { 
        loadBooksFromDB();
        return books; 
    }

    public void addBook(Book book) {
        String sql = "INSERT INTO Books(id, title, author, status, issuedTo) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getId());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setString(4, book.getStatus());
            pstmt.setString(5, book.getIssuedTo());
            pstmt.executeUpdate();
            books.add(book);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeBook(Book book) {
        String sql = "DELETE FROM Books WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getId());
            pstmt.executeUpdate();
            books.remove(book);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBook(Book oldBook, Book newBook) {
        String sql = "UPDATE Books SET title = ?, author = ?, status = ?, issuedTo = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newBook.getTitle());
            pstmt.setString(2, newBook.getAuthor());
            pstmt.setString(3, newBook.getStatus());
            pstmt.setString(4, newBook.getIssuedTo());
            pstmt.setString(5, oldBook.getId());
            pstmt.executeUpdate();
            
            int index = books.indexOf(oldBook);
            if (index != -1) {
                books.set(index, newBook);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addUser(User user) {
        String sql = "INSERT INTO Users(id, name, password, role) VALUES(?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getRole());
            pstmt.executeUpdate();
            users.add(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void issueBook(Book book, String studentId) {
        if ("Available".equals(book.getStatus())) {
            Book updated = new Book(book.getId(), book.getTitle(), book.getAuthor(), "Issued", studentId);
            updateBook(book, updated);
        }
    }

    public void returnBook(Book book) {
        Book updated = new Book(book.getId(), book.getTitle(), book.getAuthor(), "Available", "");
        updateBook(book, updated);
    }

    public String borrowBookAsStudent(Book book, String studentId) {
        loadBooksFromDB(); // Ensure fresh count
        long borrowedCount = books.stream()
                .filter(b -> studentId.equals(b.getIssuedTo()))
                .count();
        
        if (borrowedCount >= 3) {
            return "Limit Reached: You can only borrow a maximum of 3 books at a time.";
        }
        if (!"Available".equals(book.getStatus())) {
            return "Error: This book is currently not available.";
        }
        
        Book updated = new Book(book.getId(), book.getTitle(), book.getAuthor(), "Issued", studentId);
        updateBook(book, updated);
        return "Success";
    }

    public void returnBookAsStudent(Book book, String studentId) {
        if (studentId.equals(book.getIssuedTo())) {
            Book updated = new Book(book.getId(), book.getTitle(), book.getAuthor(), "Available", "");
            updateBook(book, updated);
        }
    }
}
