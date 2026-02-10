package org.example.controllers;

import org.example.repositories.BookRepository;
import org.example.repositories.IBookRepository;
import java.util.Scanner;

public class LibraryController {
    private final IBookRepository repo = new BookRepository();
    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        System.out.println("Келси братишка кітап оқы");
        while (true) {
            System.out.println("\n1. Все книги");
            System.out.println("2. Книги по Категории (Fiction, Science...)");
            System.out.println("3. Взять книгу");
            System.out.println("4. Вернуть книгу");
            System.out.println("5. Отчет (Кто что читает)");
            System.out.println("6. Регистрация");
            System.out.println("7. Добавить книгу (Admin)");
            System.out.println("0. Выход");
            System.out.print("Выбор: ");

            if (!scanner.hasNextInt()) { scanner.next(); continue; }
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    repo.getAllBooks().forEach(System.out::println);
                    break;
                case 2:
                    System.out.print("Введите категорию (Fiction/Science/History): ");
                    String cat = scanner.nextLine();
                    repo.getBooksByCategory(cat).forEach(System.out::println);
                    break;
                case 3:
                    System.out.print("Ваш ID: ");
                    int uid = scanner.nextInt();
                    System.out.print("ID Книги: ");
                    int bid = scanner.nextInt();
                    repo.borrowBook(uid, bid);
                    break;
                case 4:
                    System.out.print("Ваш ID: ");
                    int rUid = scanner.nextInt();
                    System.out.print("ID Книги: ");
                    int rBid = scanner.nextInt();
                    repo.returnBook(rUid, rBid);
                    break;
                case 5:
                    repo.getFullBorrowingInfo();
                    break;
                case 6:
                    System.out.print("Имя: ");
                    String name = scanner.nextLine();
                    System.out.print("Email: ");
                    String email = scanner.nextLine();
                    System.out.println("Ваш новый ID: " + repo.registerUser(name, email));
                    break;
                case 7:
                    System.out.print("Название: ");
                    String title = scanner.nextLine();
                    System.out.print("Автор: ");
                    String auth = scanner.nextLine();
                    System.out.print("Цена: ");
                    double price = scanner.nextDouble();
                    System.out.print("ID Категории (1-Fiction, 2-Science, 3-IT): ");
                    int catId = scanner.nextInt();
                    repo.addNewBook(title, auth, price, catId);
                    break;
                case 0: return;
            }
        }
    }
}