CREATE DATABASE IF NOT EXISTS supermarket;
USE supermarket;

CREATE TABLE IF NOT EXISTS products (
    product_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    expiry_date DATE NOT NULL
);

-- Optional sample records
INSERT INTO products (product_id, name, category, price, quantity, expiry_date)
VALUES
(101, 'Rice', 'Grocery', 1000.00, 20, '2027-12-31'),
(102, 'Sugar', 'Grocery', 50.00, 50, '2027-06-30'),
(103, 'Cooking Oil', 'Grocery', 250.00, 30, '2027-03-31')
ON DUPLICATE KEY UPDATE
    name = VALUES(name);
