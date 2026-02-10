Library Management System (Java JDBC + PostgreSQL)

Описание

Консольное приложение для управления библиотекой. Реализовано в рамках Assignment 1 & 2.
Проект использует Layered Architecture и Singleton Pattern для подключения к БД.

Требования

Java 17+

PostgreSQL

Maven

Инструкция по запуску

Шаг 1: Подготовка Базы Данных

Создайте базу данных с именем library_db.

Откройте файл database.sql (находится в корне этого проекта).

Выполните скрипт в pgAdmin или консоли psql.

Шаг 2: Настройка подключения

В файле src/main/java/org/example/config/DatabaseConnection.java проверьте настройки:

URL: jdbc:postgresql://localhost:5432/library_db

Username: postgres

Password: ВашПароль

Шаг 3: Запуск

Запустите класс org.example.Main.

Функционал

Admin: Добавление книг.

Client: Регистрация, поиск книг по категориям, взятие книг.

System: Автоматический расчет штрафов за просрочку (Business Logic).