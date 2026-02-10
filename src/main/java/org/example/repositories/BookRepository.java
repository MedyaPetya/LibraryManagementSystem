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
        // JOIN, чтобы сразу получить название категории вместо ID
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

    // Использование LAMBDA для фильтрации (Требование итерации 2)
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

            // Проверка: Есть ли у юзера уже просроченные книги? (Бизнес-логика)
            String checkDebt = "SELECT COUNT(*) FROM loans WHERE user_id = ? AND return_date IS NULL AND loan_date < (CURRENT_DATE - 14)";
            PreparedStatement debtStmt = con.prepareStatement(checkDebt);
            debtStmt.setInt(1, userId);
            ResultSet rsDebt = debtStmt.executeQuery();
            if(rsDebt.next() && rsDebt.getInt(1) > 0) {
                System.out.println(" БЛОКИРОВКА: У вас есть просроченные книги! Сначала верните их.");
                con.rollback();
                return false;
            }

            // Стандартная выдача
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
                System.out.println(" Книга выдана!");
                return true;
            } else {
                System.out.println(" Книга недоступна.");
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

            // 1. Ищем активный займ и дату выдачи
            String findLoan = "SELECT loan_date FROM loans WHERE user_id = ? AND book_id = ? AND return_date IS NULL";
            PreparedStatement stmt = con.prepareStatement(findLoan);
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LocalDate loanDate = rs.getDate("loan_date").toLocalDate();
                LocalDate returnDate = LocalDate.now();

                // БИЗНЕС-ЛОГИКА: Расчет штрафа (если > 14 дней, то $1 за день)
                long daysBetween = ChronoUnit.DAYS.between(loanDate, returnDate);
                double fine = 0.0;
                if (daysBetween > 14) {
                    fine = (daysBetween - 14) * 1.0;
                    System.out.println("⚠ ВНИМАНИЕ: Просрочка " + (daysBetween - 14) + " дн. Штраф: $" + fine);
                }

                // 2. Закрываем займ
                PreparedStatement updateLoan = con.prepareStatement(
                        "UPDATE loans SET return_date = CURRENT_DATE, fine_amount = ? WHERE user_id = ? AND book_id = ? AND return_date IS NULL"
                );
                updateLoan.setDouble(1, fine);
                updateLoan.setInt(2, userId);
                updateLoan.setInt(3, bookId);
                updateLoan.executeUpdate();

                // 3. Освобождаем книгу
                PreparedStatement updateBook = con.prepareStatement("UPDATE books SET is_available = TRUE WHERE id = ?");
                updateBook.setInt(1, bookId);
                updateBook.executeUpdate();

                con.commit();
                System.out.println(" Книга возвращена. Спасибо!");
            } else {
                System.out.println(" Запись о займе не найдена.");
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
        // ПРОФЕССИОНАЛЬНЫЙ ПОДХОД: Используем SQL VIEW 'library_report', которое мы создали
        // Это чище, чем писать огромный JOIN здесь
        String sql = "SELECT * FROM library_report WHERE status = 'Active'";

        System.out.println("\n=== ОТЧЕТ БИБЛИОТЕКИ (SQL VIEW) ===");
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("Читатель: %-15s | Книга: %-20s | Категория: %-10s | Взята: %s\n",
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
            System.out.println("Ошибка регистрации: " + e.getMessage());
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
            System.out.println(" Книга добавлена.");
        } catch (SQLException e) { e.printStackTrace(); }
    }
}