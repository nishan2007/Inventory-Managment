USE smartstock_dev;

INSERT INTO products (name, sku, price) VALUES
('USB Cable', 'USB-001', 9.99),
('Wireless Mouse', 'MOUSE-002', 24.99);

INSERT INTO locations (name, address)
VALUES ('Main Warehouse', NULL);

INSERT INTO categories (name) VALUES
                                  ('Electronics'),
                                  ('Office Supplies'),
                                  ('Food & Beverage');


-- payment fields such as cash/card/cheque
ALTER TABLE sales
    ADD payment_method ENUM('CASH','CARD','CHEQUE') NULL,
    ADD amount_paid DECIMAL(10,2) NULL,
    ADD payment_reference VARCHAR(100) NULL;