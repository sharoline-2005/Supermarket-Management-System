import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Supermarket Management System
 * Java 8 + JDBC + MySQL
 */
public class SupermarketManagementSystem {

    private static final String URL =
            "jdbc:mysql://localhost:3306/supermarket?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    // Change this to your own MySQL password.
    private static final String PASSWORD = "root";

    private static final Scanner SC = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection connection =
                     DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.println("Connected to supermarket database.");

            while (true) {
                printMenu();
                int choice = readInt("Enter your choice: ");

                try {
                    switch (choice) {
                        case 1:
                            addProduct(connection);
                            break;
                        case 2:
                            displayProducts(connection);
                            break;
                        case 3:
                            searchProduct(connection);
                            break;
                        case 4:
                            updateProduct(connection);
                            break;
                        case 5:
                            deleteProduct(connection);
                            break;
                        case 6:
                            generateBill(connection);
                            break;
                        case 7:
                            System.out.println("Thank you for using the system.");
                            return;
                        default:
                            System.out.println("Invalid choice. Select 1 to 7.");
                    }
                } catch (SQLException | IllegalArgumentException e) {
                    System.out.println("Operation failed: " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.out.println("Could not connect to MySQL: " + e.getMessage());
            System.out.println("Check that MySQL is running and your URL, username,");
            System.out.println("password, database, and MySQL Connector/J are correct.");
        }
    }

    private static void printMenu() {
        System.out.println("\n===== SUPERMARKET MANAGEMENT SYSTEM =====");
        System.out.println("1. Add product");
        System.out.println("2. Display all products");
        System.out.println("3. Search by product ID or category");
        System.out.println("4. Update product price and quantity");
        System.out.println("5. Delete product");
        System.out.println("6. Generate customer bill");
        System.out.println("7. Exit");
    }

    // 1. Add a new product.
    private static void addProduct(Connection con) throws SQLException {
        int id = readInt("Product ID: ");
        System.out.print("Product name: ");
        String name = SC.nextLine().trim();
        System.out.print("Category: ");
        String category = SC.nextLine().trim();
        double price = readDouble("Price: ");
        int quantity = readInt("Quantity: ");
        System.out.print("Expiry date (YYYY-MM-DD): ");
        Date expiryDate = Date.valueOf(SC.nextLine().trim());

        if (name.isEmpty() || category.isEmpty() || price < 0 || quantity < 0) {
            throw new IllegalArgumentException("Enter valid product details.");
        }

        String sql = "INSERT INTO products "
                + "(product_id, name, category, price, quantity, expiry_date) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setString(3, category);
            ps.setDouble(4, price);
            ps.setInt(5, quantity);
            ps.setDate(6, expiryDate);

            ps.executeUpdate();
            System.out.println("Product added successfully.");
        }
    }

    // 2. Display all products.
    private static void displayProducts(Connection con) throws SQLException {
        String sql = "SELECT * FROM products ORDER BY product_id";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            printProductHeader();
            boolean found = false;

            while (rs.next()) {
                found = true;
                printProduct(rs);
            }

            if (!found) {
                System.out.println("No products found.");
            }
        }
    }

    // 3. Search by product ID or category.
    private static void searchProduct(Connection con) throws SQLException {
        System.out.println("1. Search by product ID");
        System.out.println("2. Search by category");
        int choice = readInt("Choose search type: ");

        String sql;
        Object searchValue;

        if (choice == 1) {
            sql = "SELECT * FROM products WHERE product_id = ?";
            searchValue = readInt("Enter product ID: ");
        } else if (choice == 2) {
            sql = "SELECT * FROM products WHERE category = ? ORDER BY name";
            System.out.print("Enter category: ");
            searchValue = SC.nextLine().trim();
        } else {
            System.out.println("Invalid search choice.");
            return;
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (searchValue instanceof Integer) {
                ps.setInt(1, (Integer) searchValue);
            } else {
                ps.setString(1, (String) searchValue);
            }

            try (ResultSet rs = ps.executeQuery()) {
                printProductHeader();
                boolean found = false;

                while (rs.next()) {
                    found = true;
                    printProduct(rs);
                }

                if (!found) {
                    System.out.println("No matching product found.");
                }
            }
        }
    }

    // 4. Update price and stock quantity.
    private static void updateProduct(Connection con) throws SQLException {
        int id = readInt("Enter product ID: ");
        double price = readDouble("Enter new price: ");
        int quantity = readInt("Enter new stock quantity: ");

        if (price < 0 || quantity < 0) {
            throw new IllegalArgumentException("Price and quantity cannot be negative.");
        }

        String sql = "UPDATE products SET price = ?, quantity = ? WHERE product_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, price);
            ps.setInt(2, quantity);
            ps.setInt(3, id);

            int rows = ps.executeUpdate();
            System.out.println(rows > 0
                    ? "Product updated successfully."
                    : "Product ID not found.");
        }
    }

    // 5. Delete a product.
    private static void deleteProduct(Connection con) throws SQLException {
        int id = readInt("Enter product ID to delete: ");
        String sql = "DELETE FROM products WHERE product_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);

            int rows = ps.executeUpdate();
            System.out.println(rows > 0
                    ? "Product deleted successfully."
                    : "Product ID not found.");
        }
    }

    // 6. Generate a bill for multiple products and reduce stock safely.
    private static void generateBill(Connection con) throws SQLException {
        Map<Integer, Integer> cart = new LinkedHashMap<Integer, Integer>();

        System.out.println("\nEnter product IDs and quantities.");
        System.out.println("Enter product ID 0 when you have finished.");

        while (true) {
            int id = readInt("Product ID: ");
            if (id == 0) {
                break;
            }

            int quantity = readInt("Quantity: ");
            if (quantity <= 0) {
                System.out.println("Quantity must be greater than zero.");
                continue;
            }

            int previousQuantity = cart.containsKey(id) ? cart.get(id) : 0;
            cart.put(id, previousQuantity + quantity);
        }

        if (cart.isEmpty()) {
            System.out.println("No products selected. Bill cancelled.");
            return;
        }

        boolean originalAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);

        try {
            double total = 0.0;

            System.out.println("\n=============== CUSTOMER BILL ===============");
            System.out.printf("%-8s %-18s %8s %8s %10s%n",
                    "ID", "Product", "Price", "Qty", "Amount");

            for (Map.Entry<Integer, Integer> item : cart.entrySet()) {
                int id = item.getKey();
                int requestedQuantity = item.getValue();

                String selectSql =
                        "SELECT name, price, quantity FROM products "
                                + "WHERE product_id = ? FOR UPDATE";

                try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                    ps.setInt(1, id);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Product ID " + id + " was not found.");
                        }

                        String name = rs.getString("name");
                        double price = rs.getDouble("price");
                        int available = rs.getInt("quantity");

                        if (requestedQuantity > available) {
                            throw new SQLException("Insufficient stock for " + name
                                    + ". Available quantity: " + available);
                        }

                        double amount = price * requestedQuantity;
                        total += amount;

                        System.out.printf("%-8d %-18s %8.2f %8d %10.2f%n",
                                id, name, price, requestedQuantity, amount);

                        String updateSql =
                                "UPDATE products SET quantity = quantity - ? "
                                        + "WHERE product_id = ?";

                        try (PreparedStatement update =
                                     con.prepareStatement(updateSql)) {
                            update.setInt(1, requestedQuantity);
                            update.setInt(2, id);
                            update.executeUpdate();
                        }
                    }
                }
            }

            double discountRate;
            if (total >= 5000) {
                discountRate = 0.10;
            } else if (total >= 2000) {
                discountRate = 0.05;
            } else {
                discountRate = 0.0;
            }

            double discount = total * discountRate;
            double finalAmount = total - discount;

            System.out.println("---------------------------------------------");
            System.out.printf("Subtotal:       Rs. %.2f%n", total);
            System.out.printf("Discount:       Rs. %.2f (%.0f%%)%n",
                    discount, discountRate * 100);
            System.out.printf("Final amount:   Rs. %.2f%n", finalAmount);
            System.out.println("=============================================");

            con.commit();
            System.out.println("Bill generated and stock updated successfully.");

        } catch (SQLException e) {
            con.rollback();
            System.out.println("Billing cancelled. No stock was changed.");
            throw e;
        } finally {
            con.setAutoCommit(originalAutoCommit);
        }
    }

    private static int readInt(String prompt) {
        System.out.print(prompt);
        int value = SC.nextInt();
        SC.nextLine(); // Consume the newline.
        return value;
    }

    private static double readDouble(String prompt) {
        System.out.print(prompt);
        double value = SC.nextDouble();
        SC.nextLine(); // Consume the newline.
        return value;
    }

    private static void printProductHeader() {
        System.out.printf("%-8s %-20s %-15s %-10s %-10s %-12s%n",
                "ID", "NAME", "CATEGORY", "PRICE", "QUANTITY", "EXPIRY");
        System.out.println("--------------------------------------------------------------------------");
    }

    private static void printProduct(ResultSet rs) throws SQLException {
        System.out.printf("%-8d %-20s %-15s %-10.2f %-10d %-12s%n",
                rs.getInt("product_id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getDouble("price"),
                rs.getInt("quantity"),
                rs.getDate("expiry_date"));
    }
}
