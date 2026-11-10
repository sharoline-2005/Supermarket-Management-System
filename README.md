# Supermarket Management System

A menu-driven Java 8 console application using JDBC and MySQL for supermarket inventory and billing.

## Features

1. Add products with ID, name, category, price, quantity, and expiry date.
2. Display all products.
3. Search products by product ID or category.
4. Update product price and stock quantity.
5. Delete products.
6. Generate a bill containing multiple products.
7. Apply discounts and update stock after successful billing.

## Technologies

- Java 8
- JDBC
- MySQL
- Maven
- MySQL Connector/J

## Requirements

- JDK 8 or later
- MySQL Server
- Maven (if building from the command line)

## Database setup

1. Start MySQL.
2. Open MySQL Workbench or a MySQL command-line client.
3. Run the SQL script in `database/supermarket.sql`.
4. The script creates the `supermarket` database and `products` table.

## Configure the database connection

Open:

`src/main/java/SupermarketManagementSystem.java`

Update these constants to match your MySQL setup:

```java
private static final String URL =
    "jdbc:mysql://localhost:3306/supermarket?useSSL=false&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "root";
```

Do not upload real database passwords to a public GitHub repository. For a personal project, use a local password and keep it out of committed source code.

## Run using Maven

From the project root directory:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass=SupermarketManagementSystem
```

If the Maven exec command is not available in your setup, add the Maven Exec Plugin or run the compiled class from your IDE.

## Run using an IDE

1. Import this folder as a Maven project.
2. Wait for Maven to download MySQL Connector/J.
3. Run `SupermarketManagementSystem.java`.
4. Select an option from the console menu.

## Discount rules

- Purchase total of Rs. 5,000 or more: 10% discount.
- Purchase total of Rs. 2,000 to Rs. 4,999.99: 5% discount.
- Purchase total below Rs. 2,000: no discount.

These are example rules and can be modified in the `generateBill()` method.

## Billing and stock

The billing operation uses a database transaction. Stock is checked and reduced only when the entire bill succeeds. If an item is missing or stock is insufficient, the transaction is rolled back.

## Project structure

```text
Supermarket-Management-System/
├── database/
│   └── supermarket.sql
├── src/
│   └── main/
│       └── java/
│           └── SupermarketManagementSystem.java
├── .gitignore
├── pom.xml
└── README.md
```
