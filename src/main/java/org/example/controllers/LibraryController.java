package org.example.controllers;

import org.example.repositories.BookRepository;
import org.example.repositories.IBookRepository;
import java.util.Scanner;

public class LibraryController {
    private final IBookRepository repo = new BookRepository();
    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        System.out.println("Welcome to Library System v2.0");
        System.out.println("------------------------------");
        System.out.println("WHO ARE YOU?");
        System.out.println("1. ADMIN (Manage books, view reports)");
        System.out.println("2. CLIENT (Borrow, return books)");
        System.out.print("Select role: ");

        if (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Exiting.");
            return;
        }
        int role = scanner.nextInt();
        scanner.nextLine(); // fix buffer

        if (role == 1) {
            // --- SECURITY CHECK ---
            System.out.print("Enter Admin Password: ");
            String password = scanner.nextLine();

            // HARDCODED PASSWORD for demonstration
            if ("admin123".equals(password)) {
                System.out.println("ACCESS GRANTED.");
                runAdminMenu();
            } else {
                System.out.println("ACCESS DENIED. Incorrect password.");
            }
            // ----------------------
        } else if (role == 2) {
            runClientMenu();
        } else {
            System.out.println("Invalid role selected.");
        }
    }

    // --- MENU FOR CLIENTS ---
    private void runClientMenu() {
        while (true) {
            System.out.println("\n--- CLIENT MENU ---");
            System.out.println("1. List all books");
            System.out.println("2. Filter by Category");
            System.out.println("3. Borrow a book");
            System.out.println("4. Return a book");
            System.out.println("5. Register (New User)");
            System.out.println("0. Exit");
            System.out.print("Selection: ");

            if (!scanner.hasNextInt()) { scanner.next(); continue; }
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    repo.getAllBooks().forEach(System.out::println);
                    break;
                case 2:
                    System.out.print("Enter Category (Fiction/Science/IT): ");
                    String cat = scanner.nextLine();
                    repo.getBooksByCategory(cat).forEach(System.out::println);
                    break;
                case 3:
                    System.out.print("Your User ID: ");
                    int uid = scanner.nextInt();
                    System.out.print("Book ID: ");
                    int bid = scanner.nextInt();
                    repo.borrowBook(uid, bid);
                    break;
                case 4:
                    System.out.print("Your User ID: ");
                    int rUid = scanner.nextInt();
                    System.out.print("Book ID: ");
                    int rBid = scanner.nextInt();
                    repo.returnBook(rUid, rBid);
                    break;
                case 5:
                    System.out.print("Enter Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter Email: ");
                    String email = scanner.nextLine();
                    int newId = repo.registerUser(name, email);
                    System.out.println("SUCCESS! Your new User ID is: " + newId);
                    break;
                case 0: return;
                default: System.out.println("Invalid command.");
            }
        }
    }

    // --- MENU FOR ADMINS ---
    private void runAdminMenu() {
        while (true) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. List all books");
            System.out.println("2. View Borrowing Report (JOIN)");
            System.out.println("3. Add New Book");
            System.out.println("0. Exit");
            System.out.print("Selection: ");

            if (!scanner.hasNextInt()) { scanner.next(); continue; }
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    repo.getAllBooks().forEach(System.out::println);
                    break;
                case 2:
                    repo.getFullBorrowingInfo();
                    break;
                case 3:
                    System.out.print("Book Title: ");
                    String title = scanner.nextLine();
                    System.out.print("Author: ");
                    String auth = scanner.nextLine();
                    System.out.print("Price: ");
                    double price = scanner.nextDouble();
                    System.out.print("Category ID (1-Fiction, 2-Science, 3-IT): ");
                    int catId = scanner.nextInt();
                    repo.addNewBook(title, auth, price, catId);
                    break;
                case 0: return;
                default: System.out.println("Invalid command.");
            }
        }
    }
}