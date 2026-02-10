package org.example.repositories;

import org.example.models.Book;
import java.util.List;

public interface IBookRepository {
    List<Book> getAllBooks();
    List<Book> getBooksByCategory(String categoryName); // Новый метод (Lambda filter)
    boolean borrowBook(int userId, int bookId);
    void returnBook(int userId, int bookId);
    void getFullBorrowingInfo(); // Использует SQL View
    int registerUser(String name, String email);
    void addNewBook(String title, String author, double price, int categoryId);
}