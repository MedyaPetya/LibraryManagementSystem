package org.example.repositories;

import org.example.config.DatabaseConnection;
import org.example.models.Book;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookRepository implements IBookRepository {
    private final Connection con;

    public BookRepository() {
        this.con = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT b.id, b.title, b.author, b.price, c.name as cat_name, b.is_available " +
                "FROM books b JOIN categories c ON b.category_id = c.id ORDER BY b.id";
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getDouble("price"),
                        rs.getString("cat_name"),
                        rs.getBoolean("is_available")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return books;
    }

    @Override
    public List<Book> getBooksByCategory(String categoryName) {
        return getAllBooks().stream()
                .filter(b -> b.getCategory().equalsIgnoreCase(categoryName))
                .collect(Collectors.toList());
    }

    @Override
    public boolean borrowBook(int userId, int bookId) {
        try {
            con.setAutoCommit(false);

            // Validation: Check for overdue books
            String checkDebt = "SELECT COUNT(*) FROM loans WHERE user_id = ? AND return_date IS NULL AND loan_date < (CURRENT_DATE - 14)";
            PreparedStatement debtStmt = con.prepareStatement(checkDebt);
            debtStmt.setInt(1, userId);
            ResultSet rsDebt = debtStmt.executeQuery();
            if(rsDebt.next() && rsDebt.getInt(1) > 0) {
                System.out.println("ERROR: You have overdue books! Return them first.");
                con.rollback();
                return false;
            }

            // Validation: Check availability
            PreparedStatement checkStmt = con.prepareStatement("SELECT is_available FROM books WHERE id = ?");
            checkStmt.setInt(1, bookId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getBoolean("is_available")) {
                PreparedStatement update = con.prepareStatement("UPDATE books SET is_available = FALSE WHERE id = ?");
                update.setInt(1, bookId);
                update.executeUpdate();

                PreparedStatement loan = con.prepareStatement("INSERT INTO loans (user_id, book_id) VALUES (?, ?)");
                loan.setInt(1, userId);
                loan.setInt(2, bookId);
                loan.executeUpdate();

                con.commit();
                System.out.println("SUCCESS: Book borrowed successfully.");
                return true;
            } else {
                System.out.println("ERROR: Book is not available.");
                con.rollback();
                return false;
            }
        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    @Override
    public void returnBook(int userId, int bookId) {
        try {
            con.setAutoCommit(false);

            String findLoan = "SELECT loan_date FROM loans WHERE user_id = ? AND book_id = ? AND return_date IS NULL";
            PreparedStatement stmt = con.prepareStatement(findLoan);
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LocalDate loanDate = rs.getDate("loan_date").toLocalDate();
                LocalDate returnDate = LocalDate.now();

                // Business Logic: Fine calculation
                long daysBetween = ChronoUnit.DAYS.between(loanDate, returnDate);
                double fine = 0.0;
                if (daysBetween > 14) {
                    fine = (daysBetween - 14) * 1.0;
                    System.out.println("WARNING: Overdue by " + (daysBetween - 14) + " days. Fine: $" + fine);
                }

                PreparedStatement updateLoan = con.prepareStatement(
                        "UPDATE loans SET return_date = CURRENT_DATE, fine_amount = ? WHERE user_id = ? AND book_id = ? AND return_date IS NULL"
                );
                updateLoan.setDouble(1, fine);
                updateLoan.setInt(2, userId);
                updateLoan.setInt(3, bookId);
                updateLoan.executeUpdate();

                PreparedStatement updateBook = con.prepareStatement("UPDATE books SET is_available = TRUE WHERE id = ?");
                updateBook.setInt(1, bookId);
                updateBook.executeUpdate();

                con.commit();
                System.out.println("SUCCESS: Book returned.");
            } else {
                System.out.println("ERROR: No active loan found for this book.");
                con.rollback();
            }
        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    @Override
    public void getFullBorrowingInfo() {
        String sql = "SELECT * FROM library_report WHERE status = 'Active'";

        System.out.println("\n--- LIBRARY REPORT ---");
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("User: %-15s | Book: %-20s | Category: %-10s | Date: %s\n",
                        rs.getString("user_name"),
                        rs.getString("book_title"),
                        rs.getString("category"),
                        rs.getDate("loan_date"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public int registerUser(String name, String email) {
        String sql = "INSERT INTO users (name, email) VALUES (?, ?) RETURNING id";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Registration Error: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public void addNewBook(String title, String author, double price, int categoryId) {
        String sql = "INSERT INTO books (title, author, price, category_id, is_available) VALUES (?, ?, ?, ?, TRUE)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setDouble(3, price);
            stmt.setInt(4, categoryId);
            stmt.executeUpdate();
            System.out.println("SUCCESS: New book added to library.");
        } catch (SQLException e) { e.printStackTrace(); }
    }
}