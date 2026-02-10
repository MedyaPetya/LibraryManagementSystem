-- СКРИПТ СОЗДАНИЯ БАЗЫ ДАННЫХ
-- Запустите этот код в pgAdmin (Query Tool)

DROP VIEW IF EXISTS library_report;
DROP TABLE IF EXISTS loans;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS categories;

CREATE TABLE categories (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       role VARCHAR(20) CHECK (role IN ('ADMIN', 'CLIENT')) DEFAULT 'CLIENT',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE books (
                       id SERIAL PRIMARY KEY,
                       title VARCHAR(150) NOT NULL,
                       author VARCHAR(100) NOT NULL,
                       price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
                       category_id INT REFERENCES categories(id),
                       is_available BOOLEAN DEFAULT TRUE
);

CREATE TABLE loans (
                       id SERIAL PRIMARY KEY,
                       user_id INT REFERENCES users(id) ON DELETE CASCADE,
                       book_id INT REFERENCES books(id) ON DELETE CASCADE,
                       loan_date DATE DEFAULT CURRENT_DATE,
                       return_date DATE,
                       fine_amount DECIMAL(10, 2) DEFAULT 0.00 CHECK (fine_amount >= 0)
);

CREATE VIEW library_report AS
SELECT
    l.id AS loan_id,
    u.name AS user_name,
    u.email,
    b.title AS book_title,
    b.author,
    c.name AS category,
    l.loan_date,
    l.return_date,
    CASE
        WHEN l.return_date IS NULL THEN 'Active'
        ELSE 'Returned'
        END AS status
FROM loans l
         JOIN users u ON l.user_id = u.id
         JOIN books b ON l.book_id = b.id
         JOIN categories c ON b.category_id = c.id;

-- SEED DATA
INSERT INTO categories (name) VALUES ('Fiction'), ('Science'), ('IT / Programming'), ('History'), ('Business');
INSERT INTO users (name, email, role) VALUES ('Super Admin', 'admin@library.com', 'ADMIN'), ('John Doe', 'john@test.com', 'CLIENT');
INSERT INTO books (title, author, price, category_id, is_available) VALUES
                                                                        ('Clean Code', 'Robert Martin', 50.00, 3, TRUE),
                                                                        ('The Great Gatsby', 'F. Scott Fitzgerald', 15.00, 1, TRUE);