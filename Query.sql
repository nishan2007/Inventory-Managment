-- =========================================
-- SMARTSTOCK POSTGRESQL SCHEMA
-- MATCHED TO CURRENT MAKEASALE.JAVA
-- =========================================

DROP TABLE IF EXISTS inventory_movements CASCADE;
DROP TABLE IF EXISTS sale_items CASCADE;
DROP TABLE IF EXISTS sales CASCADE;
DROP TABLE IF EXISTS inventory CASCADE;
DROP TABLE IF EXISTS user_locations CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS locations CASCADE;

-- =========================================
-- LOCATIONS
-- =========================================
CREATE TABLE locations (
                           location_id SERIAL PRIMARY KEY,
                           name VARCHAR(100) NOT NULL UNIQUE,
                           address TEXT,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================
-- CATEGORIES
-- =========================================
CREATE TABLE categories (
                            category_id SERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL UNIQUE,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================
-- PRODUCTS
-- MakeASale expects: product_id, name, sku, price
-- =========================================
CREATE TABLE products (
                          product_id SERIAL PRIMARY KEY,
                          category_id INT REFERENCES categories(category_id) ON DELETE SET NULL,
                          name VARCHAR(150) NOT NULL,
                          sku VARCHAR(100) NOT NULL UNIQUE,
                          barcode VARCHAR(100),
                          description TEXT,
                          cost_price NUMERIC(10,2) NOT NULL DEFAULT 0.00,
                          price NUMERIC(10,2) NOT NULL DEFAULT 0.00,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_sku ON products(sku);

-- =========================================
-- ROLES
-- =========================================
CREATE TABLE roles (
                       role_id SERIAL PRIMARY KEY,
                       role_name VARCHAR(50) NOT NULL UNIQUE,
                       description TEXT
);

-- =========================================
-- USERS
-- =========================================
CREATE TABLE users (
                       user_id SERIAL PRIMARY KEY,
                       full_name VARCHAR(150) NOT NULL,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       email VARCHAR(150) UNIQUE,
                       password_hash TEXT NOT NULL,
                       role_id INT REFERENCES roles(role_id) ON DELETE SET NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================
-- USER LOCATIONS
-- =========================================
CREATE TABLE user_locations (
                                user_location_id SERIAL PRIMARY KEY,
                                user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                                location_id INT NOT NULL REFERENCES locations(location_id) ON DELETE CASCADE,
                                UNIQUE (user_id, location_id)
);

-- =========================================
-- INVENTORY
-- MakeASale expects:
-- inventory_id, product_id, location_id, quantity_on_hand, reorder_level
-- negative inventory allowed
-- =========================================
CREATE TABLE inventory (
                           inventory_id SERIAL PRIMARY KEY,
                           product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
                           location_id INT NOT NULL REFERENCES locations(location_id) ON DELETE CASCADE,
                           quantity_on_hand INT NOT NULL DEFAULT 0,
                           reorder_level INT NOT NULL DEFAULT 0,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           UNIQUE (product_id, location_id)
);

CREATE INDEX idx_inventory_product_id ON inventory(product_id);
CREATE INDEX idx_inventory_location_id ON inventory(location_id);

-- =========================================
-- SALES
-- MakeASale inserts:
-- location_id, total_amount, status, payment_method
-- =========================================
CREATE TABLE sales (
                       sale_id SERIAL PRIMARY KEY,
                       location_id INT NOT NULL REFERENCES locations(location_id) ON DELETE RESTRICT,
                       user_id INT REFERENCES users(user_id) ON DELETE SET NULL,
                       total_amount NUMERIC(10,2) NOT NULL DEFAULT 0.00,
                       status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
                       payment_method VARCHAR(20) NOT NULL DEFAULT 'CASH',
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sales_location_id ON sales(location_id);
CREATE INDEX idx_sales_user_id ON sales(user_id);
CREATE INDEX idx_sales_created_at ON sales(created_at);

-- =========================================
-- SALE ITEMS
-- MakeASale inserts ONLY:
-- sale_id, product_id, quantity, unit_price
-- so line_total is not included here
-- =========================================
CREATE TABLE sale_items (
                            sale_item_id SERIAL PRIMARY KEY,
                            sale_id INT NOT NULL REFERENCES sales(sale_id) ON DELETE CASCADE,
                            product_id INT NOT NULL REFERENCES products(product_id) ON DELETE RESTRICT,
                            quantity INT NOT NULL,
                            unit_price NUMERIC(10,2) NOT NULL
);

CREATE INDEX idx_sale_items_sale_id ON sale_items(sale_id);
CREATE INDEX idx_sale_items_product_id ON sale_items(product_id);

-- =========================================
-- INVENTORY MOVEMENTS
-- MUST match current Java exactly:
-- product_id, location_id, change_qty, reason, note
-- =========================================
CREATE TABLE inventory_movements (
                                     movement_id SERIAL PRIMARY KEY,
                                     product_id INT NOT NULL REFERENCES products(product_id) ON DELETE RESTRICT,
                                     location_id INT NOT NULL REFERENCES locations(location_id) ON DELETE RESTRICT,
                                     change_qty INT NOT NULL,
                                     reason VARCHAR(50) NOT NULL,
                                     note TEXT,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inventory_movements_product_id ON inventory_movements(product_id);
CREATE INDEX idx_inventory_movements_location_id ON inventory_movements(location_id);
CREATE INDEX idx_inventory_movements_created_at ON inventory_movements(created_at);

-- =========================================
-- UPDATED_AT FUNCTION
-- =========================================
CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_inventory_updated_at
    BEFORE UPDATE ON inventory
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- =========================================
-- AUTO CREATE INVENTORY FOR NEW PRODUCT
-- =========================================
CREATE OR REPLACE FUNCTION create_inventory_for_new_product()
    RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO inventory (product_id, location_id, quantity_on_hand, reorder_level)
    SELECT NEW.product_id, l.location_id, 0, 0
    FROM locations l;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_create_inventory_for_new_product
    AFTER INSERT ON products
    FOR EACH ROW
EXECUTE FUNCTION create_inventory_for_new_product();

-- =========================================
-- AUTO CREATE INVENTORY FOR NEW LOCATION
-- =========================================
CREATE OR REPLACE FUNCTION create_inventory_for_new_location()
    RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO inventory (product_id, location_id, quantity_on_hand, reorder_level)
    SELECT p.product_id, NEW.location_id, 0, 0
    FROM products p;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_create_inventory_for_new_location
    AFTER INSERT ON locations
    FOR EACH ROW
EXECUTE FUNCTION create_inventory_for_new_location();

-- =========================================
-- HELPFUL VIEW FOR SEARCH / DEBUGGING
-- =========================================
CREATE OR REPLACE VIEW vw_inventory_details AS
SELECT
    p.product_id,
    p.name,
    p.sku,
    p.price,
    i.location_id,
    i.quantity_on_hand
FROM products p
         LEFT JOIN inventory i
                   ON p.product_id = i.product_id;

-- =========================================
-- SEED DATA
-- =========================================
INSERT INTO roles (role_name, description) VALUES
                                               ('ADMIN', 'Full access'),
                                               ('MANAGER', 'Store management'),
                                               ('CASHIER', 'POS access');

INSERT INTO locations (name, address) VALUES
                                          ('Main Store', 'Head Office'),
                                          ('Store 2', 'Second Branch');

INSERT INTO categories (name) VALUES
                                  ('General'),
                                  ('Beverages'),
                                  ('Snacks'),
                                  ('Electronics');

INSERT INTO users (full_name, username, email, password_hash, role_id) VALUES
                                                                           ('Admin User', 'admin', 'admin@smartstock.com', 'change_this_password_hash', 1),
                                                                           ('Manager User', 'manager1', 'manager@smartstock.com', 'change_this_password_hash', 2),
                                                                           ('Cashier User', 'cashier1', 'cashier@smartstock.com', 'change_this_password_hash', 3);

INSERT INTO user_locations (user_id, location_id) VALUES
                                                      (1, 1),
                                                      (1, 2),
                                                      (2, 1),
                                                      (3, 1);

INSERT INTO products (category_id, name, sku, barcode, description, cost_price, price) VALUES
                                                                                           (1, 'Desk Fan', 'PRD-010', '100000001', 'Portable desk fan', 15.00, 25.00),
                                                                                           (1, 'Notebook', 'PRD-011', '100000002', 'A5 notebook', 2.00, 4.50),
                                                                                           (3, 'Chips', 'PRD-012', '100000003', 'Salted chips', 1.00, 2.50),
                                                                                           (2, 'Soda', 'PRD-013', '100000004', 'Soft drink bottle', 0.80, 1.75);

UPDATE inventory SET quantity_on_hand = 25, reorder_level = 5 WHERE product_id = 1 AND location_id = 1;
UPDATE inventory SET quantity_on_hand = 10, reorder_level = 5 WHERE product_id = 1 AND location_id = 2;
UPDATE inventory SET quantity_on_hand = 50, reorder_level = 10 WHERE product_id = 2 AND location_id = 1;
UPDATE inventory SET quantity_on_hand = 30, reorder_level = 10 WHERE product_id = 2 AND location_id = 2;
UPDATE inventory SET quantity_on_hand = 40, reorder_level = 8 WHERE product_id = 3 AND location_id = 1;
UPDATE inventory SET quantity_on_hand = 20, reorder_level = 8 WHERE product_id = 3 AND location_id = 2;
UPDATE inventory SET quantity_on_hand = 60, reorder_level = 12 WHERE product_id = 4 AND location_id = 1;
UPDATE inventory SET quantity_on_hand = 35, reorder_level = 12 WHERE product_id = 4 AND location_id = 2;

INSERT INTO inventory_movements (product_id, location_id, change_qty, reason, note)
SELECT product_id, location_id, quantity_on_hand, 'OPENING_STOCK', 'Initial stock load'
FROM inventory
WHERE quantity_on_hand <> 0;