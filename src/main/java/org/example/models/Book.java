package org.example.models;

public class Book {
    private int id;
    private String title;
    private String author;
    private double price;
    private String category;
    private boolean isAvailable;

    public Book(int id, String title, String author, double price, String category, boolean isAvailable) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
        this.category = category;
        this.isAvailable = isAvailable;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return String.format("[%s] ID:%d \"%s\" - %s ($%.2f) | Available: %s",
                category, id, title, author, price, (isAvailable ? "YES" : "NO"));
    }
}