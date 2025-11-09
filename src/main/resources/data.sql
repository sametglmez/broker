-- ROLES
INSERT INTO roles (id, name) VALUES (1, 'ADMIN');
INSERT INTO roles (id, name) VALUES (2, 'CUSTOMER');

-- CUSTOMERS
    INSERT INTO customers (id, name) VALUES (101, 'Customer A');
INSERT INTO customers (id, name) VALUES (102, 'Customer B');



-- ASSETS
INSERT INTO assets (customer_id, asset_name, size, usable_size) VALUES
(101, 'TRY', 10000.00, 10000.00),
(101, 'ASELS', 200.00, 200.00),
(102, 'TRY', 5000.00, 5000.00),
(102, 'THYAO', 150.00, 150.00);

-- ORDERS
INSERT INTO orders (customer_id, asset_name, order_side, size, price, status, create_date) VALUES
(101, 'ASELS', 'BUY', 10.00, 50.00, 'PENDING', CURRENT_TIMESTAMP),
(101, 'TRY', 'SELL', 1000.00, 1.00, 'PENDING', CURRENT_TIMESTAMP),
(102, 'THYAO', 'BUY', 5.00, 100.00, 'PENDING', CURRENT_TIMESTAMP);
