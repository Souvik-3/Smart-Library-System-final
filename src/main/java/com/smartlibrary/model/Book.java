package com.smartlibrary.model;

public class Book {
    private String id;
    private String title;
    private String author;
    private String status; // "Available", "Issued"
    private String issuedTo; // User ID

    public Book() {}

    public Book(String id, String title, String author, String status, String issuedTo) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.status = status;
        this.issuedTo = issuedTo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getIssuedTo() { return issuedTo; }
    public void setIssuedTo(String issuedTo) { this.issuedTo = issuedTo; }
}
