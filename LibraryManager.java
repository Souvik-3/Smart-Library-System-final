package com.smartlibrary.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LibraryManager {
    private static LibraryManager instance;
    private List<User> users;
    private List<Book> books;
    private User currentUser;

    private LibraryManager() {
        users = new ArrayList<>();
        books = new ArrayList<>();
        loadDemoData();
        loadDemoBooks();
    }

    public static LibraryManager getInstance() {
        if (instance == null) {
            instance = new LibraryManager();
        }
        return instance;
    }

    private void loadDemoData() {
        try {
            File file = new File("src/main/resources/data/students.json");
            if (file.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                users = mapper.readValue(file, new TypeReference<ArrayList<User>>(){});
            } else {
                try (InputStream is = getClass().getResourceAsStream("/data/students.json")) {
                    if (is != null) {
                        ObjectMapper mapper = new ObjectMapper();
                        users = mapper.readValue(is, new TypeReference<ArrayList<User>>(){});
                    }
                }
            }
            if (users == null) {
                users = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback
            users.add(new User("L101", "Admin", "admin", "LIBRARIAN"));
        }
    }

    private void saveDemoData() {
        try {
            // Using absolute or relative path to the workspace src directory so it actually persists in source code for demo
            File file = new File("src/main/resources/data/students.json");
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, users);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDemoBooks() {
        try {
            File file = new File("src/main/resources/data/books.json");
            if (file.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                books = mapper.readValue(file, new TypeReference<ArrayList<Book>>(){});
            } else {
                try (InputStream is = getClass().getResourceAsStream("/data/books.json")) {
                    if (is != null) {
                        ObjectMapper mapper = new ObjectMapper();
                        books = mapper.readValue(is, new TypeReference<ArrayList<Book>>(){});
                    }
                }
            }
            if (books == null) {
                books = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            books = new ArrayList<>();
        }
    }

    private void saveBooks() {
        try {
            File file = new File("src/main/resources/data/books.json");
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, books);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User authenticate(String id, String password) {
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

    public List<User> getUsers() { return users; }
    public List<Book> getBooks() { return books; }

    public void addBook(Book book) {
        books.add(book);
        saveBooks();
    }

    public void removeBook(Book book) {
        books.remove(book);
        saveBooks();
    }

    public void updateBook(Book oldBook, Book newBook) {
        int index = books.indexOf(oldBook);
        if (index != -1) {
            books.set(index, newBook);
            saveBooks();
        }
    }

    public void addUser(User user) {
        users.add(user);
        saveDemoData();
    }

    public void issueBook(Book book, String studentId) {
        if ("Available".equals(book.getStatus())) {
            book.setStatus("Issued");
            book.setIssuedTo(studentId);
            saveBooks();
        }
    }

    public void returnBook(Book book) {
        book.setStatus("Available");
        book.setIssuedTo("");
        saveBooks();
    }

    public String borrowBookAsStudent(Book book, String studentId) {
        long borrowedCount = books.stream()
                .filter(b -> studentId.equals(b.getIssuedTo()))
                .count();
        
        if (borrowedCount >= 3) {
            return "Limit Reached: You can only borrow a maximum of 3 books at a time.";
        }
        if (!"Available".equals(book.getStatus())) {
            return "Error: This book is currently not available.";
        }
        
        book.setStatus("Issued");
        book.setIssuedTo(studentId);
        saveBooks();
        return "Success";
    }

    public void returnBookAsStudent(Book book, String studentId) {
        if (studentId.equals(book.getIssuedTo())) {
            book.setStatus("Available");
            book.setIssuedTo("");
            saveBooks();
        }
    }
}
