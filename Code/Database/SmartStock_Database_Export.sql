-- SmartStock database export for easy grading
-- Target database: PostgreSQL

DROP VIEW IF EXISTS public.vw_inventory_details CASCADE;
DROP TABLE IF EXISTS public.inventory_movements CASCADE;
DROP TABLE IF EXISTS public.sale_items CASCADE;
DROP TABLE IF EXISTS public.sales CASCADE;
DROP TABLE IF EXISTS public.inventory CASCADE;
DROP TABLE IF EXISTS public.product_barcodes CASCADE;
DROP TABLE IF EXISTS public.user_locations CASCADE;
DROP TABLE IF EXISTS public.users CASCADE;
DROP TABLE IF EXISTS public.role_permissions CASCADE;
DROP TABLE IF EXISTS public.permissions CASCADE;
DROP TABLE IF EXISTS public.roles CASCADE;
DROP TABLE IF EXISTS public.products CASCADE;
DROP TABLE IF EXISTS public.categories CASCADE;
DROP TABLE IF EXISTS public.locations CASCADE;

CREATE TABLE public.categories (
    category_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE public.locations (
    location_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE public.roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE public.permissions (
    permission_id SERIAL PRIMARY KEY,
    permission_key VARCHAR(100) NOT NULL UNIQUE,
    permission_name VARCHAR(100) NOT NULL
);

CREATE TABLE public.role_permissions (
    role_id INTEGER NOT NULL,
    permission_id INTEGER NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES public.roles(role_id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES public.permissions(permission_id) ON DELETE CASCADE
);

CREATE TABLE public.users (
    user_id SERIAL PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) UNIQUE,
    password_hash TEXT NOT NULL,
    role_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (role_id) REFERENCES public.roles(role_id) ON DELETE SET NULL
);

CREATE TABLE public.products (
    product_id SERIAL PRIMARY KEY,
    category_id INTEGER,
    name VARCHAR(150) NOT NULL,
    sku VARCHAR(100) NOT NULL UNIQUE,
    barcode VARCHAR(100),
    description TEXT,
    cost_price NUMERIC(10,2) DEFAULT 0.00 NOT NULL,
    price NUMERIC(10,2) DEFAULT 0.00 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (category_id) REFERENCES public.categories(category_id) ON DELETE SET NULL
);

CREATE TABLE public.product_barcodes (
    product_barcode_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id INTEGER NOT NULL,
    barcode VARCHAR(100) NOT NULL UNIQUE,
    FOREIGN KEY (product_id) REFERENCES public.products(product_id) ON DELETE CASCADE
);

CREATE TABLE public.inventory (
    inventory_id SERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL,
    location_id INTEGER NOT NULL,
    quantity_on_hand INTEGER DEFAULT 0 NOT NULL,
    reorder_level INTEGER DEFAULT 0 NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (product_id, location_id),
    FOREIGN KEY (product_id) REFERENCES public.products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES public.locations(location_id) ON DELETE CASCADE
);

CREATE TABLE public.inventory_movements (
    movement_id SERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL,
    location_id INTEGER NOT NULL,
    change_qty INTEGER NOT NULL,
    reason VARCHAR(50) NOT NULL,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (product_id) REFERENCES public.products(product_id) ON DELETE RESTRICT,
    FOREIGN KEY (location_id) REFERENCES public.locations(location_id) ON DELETE RESTRICT
);

CREATE TABLE public.user_locations (
    user_location_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    location_id INTEGER NOT NULL,
    UNIQUE (user_id, location_id),
    FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES public.locations(location_id) ON DELETE CASCADE
);

CREATE TABLE public.sales (
    sale_id SERIAL PRIMARY KEY,
    location_id INTEGER NOT NULL,
    user_id INTEGER,
    total_amount NUMERIC(10,2) DEFAULT 0.00 NOT NULL,
    status VARCHAR(20) DEFAULT 'COMPLETED' NOT NULL,
    payment_method VARCHAR(20) DEFAULT 'CASH' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (location_id) REFERENCES public.locations(location_id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE SET NULL
);

CREATE TABLE public.sale_items (
    sale_item_id SERIAL PRIMARY KEY,
    sale_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(10,2) NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES public.sales(sale_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES public.products(product_id) ON DELETE RESTRICT
);

CREATE INDEX idx_products_name ON public.products(name);
CREATE INDEX idx_products_sku ON public.products(sku);
CREATE INDEX idx_product_barcodes_product_id ON public.product_barcodes(product_id);
CREATE INDEX idx_inventory_product_id ON public.inventory(product_id);
CREATE INDEX idx_inventory_location_id ON public.inventory(location_id);
CREATE INDEX idx_inventory_movements_product_id ON public.inventory_movements(product_id);
CREATE INDEX idx_inventory_movements_location_id ON public.inventory_movements(location_id);
CREATE INDEX idx_inventory_movements_created_at ON public.inventory_movements(created_at);
CREATE INDEX idx_sales_location_id ON public.sales(location_id);
CREATE INDEX idx_sales_user_id ON public.sales(user_id);
CREATE INDEX idx_sales_created_at ON public.sales(created_at);
CREATE INDEX idx_sale_items_sale_id ON public.sale_items(sale_id);
CREATE INDEX idx_sale_items_product_id ON public.sale_items(product_id);

CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_products_updated_at
    BEFORE UPDATE ON public.products
    FOR EACH ROW
EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_inventory_updated_at
    BEFORE UPDATE ON public.inventory
    FOR EACH ROW
EXECUTE FUNCTION public.set_updated_at();

CREATE OR REPLACE FUNCTION public.create_inventory_for_new_product()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.inventory (product_id, location_id, quantity_on_hand, reorder_level)
    SELECT NEW.product_id, l.location_id, 0, 0
    FROM public.locations l;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_create_inventory_for_new_product
    AFTER INSERT ON public.products
    FOR EACH ROW
EXECUTE FUNCTION public.create_inventory_for_new_product();

CREATE OR REPLACE FUNCTION public.create_inventory_for_new_location()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.inventory (product_id, location_id, quantity_on_hand, reorder_level)
    SELECT p.product_id, NEW.location_id, 0, 0
    FROM public.products p;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_create_inventory_for_new_location
    AFTER INSERT ON public.locations
    FOR EACH ROW
EXECUTE FUNCTION public.create_inventory_for_new_location();

CREATE OR REPLACE VIEW public.vw_inventory_details AS
SELECT
    p.product_id,
    p.name,
    p.sku,
    p.price,
    i.location_id,
    i.quantity_on_hand
FROM public.products p
LEFT JOIN public.inventory i
    ON p.product_id = i.product_id;

INSERT INTO public.roles (role_id, role_name, description) VALUES
    (1, 'ADMIN', 'Full access'),
    (2, 'MANAGER', 'Store management'),
    (3, 'CASHIER', 'POS access');

INSERT INTO public.permissions (permission_id, permission_key, permission_name) VALUES
    (1, 'MAKE_SALE', 'Make Sale'),
    (2, 'NEW_ITEM', 'Add Item'),
    (3, 'EDIT_ITEM', 'Edit Item'),
    (4, 'EMPLOYEE_MANAGEMENT', 'Employee Management'),
    (5, 'ROLE_MANAGEMENT', 'Roles & Permission'),
    (6, 'CHANGE_STORE', 'Change Store'),
    (7, 'VIEW_REPORTS', 'View Reports'),
    (8, 'VIEW_SALES', 'View Sales'),
    (9, 'VIEW_INVENTORY', 'View Inventory');

INSERT INTO public.role_permissions (role_id, permission_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9),
    (2, 1), (2, 2), (2, 3), (2, 6), (2, 7), (2, 8), (2, 9),
    (3, 1), (3, 8), (3, 9);

INSERT INTO public.locations (location_id, name, address) VALUES
    (1, 'Main Store', 'Head Office'),
    (2, 'Store 2', 'Second Branch');

INSERT INTO public.categories (category_id, name) VALUES
    (1, 'General'),
    (2, 'Beverages'),
    (3, 'Snacks'),
    (4, 'Electronics');

INSERT INTO public.users (user_id, full_name, username, email, password_hash, role_id) VALUES
    (1, 'Admin User', 'admin', 'admin@smartstock.com', 'Admin123!', 1),
    (2, 'Manager User', 'manager1', 'manager@smartstock.com', 'Manager123!', 2),
    (3, 'Cashier User', 'cashier1', 'cashier@smartstock.com', 'Cashier123!', 3);

INSERT INTO public.user_locations (user_id, location_id) VALUES
    (1, 1), (1, 2), (2, 1), (3, 1);

INSERT INTO public.products (product_id, category_id, name, sku, barcode, description, cost_price, price) VALUES
    (1, 1, 'Desk Fan', 'PRD-010', '100000001', 'Portable desk fan', 15.00, 25.00),
    (2, 1, 'Notebook', 'PRD-011', '100000002', 'A5 notebook', 2.00, 4.50),
    (3, 3, 'Chips', 'PRD-012', '100000003', 'Salted chips', 1.00, 2.50),
    (4, 2, 'Soda', 'PRD-013', '100000004', 'Soft drink bottle', 0.80, 1.75);

INSERT INTO public.product_barcodes (product_id, barcode) VALUES
    (1, '100000001'),
    (2, '100000002'),
    (3, '100000003'),
    (4, '100000004');

INSERT INTO public.inventory (product_id, location_id, quantity_on_hand, reorder_level) VALUES
    (1, 1, 25, 5),
    (1, 2, 10, 5),
    (2, 1, 50, 10),
    (2, 2, 30, 10),
    (3, 1, 40, 8),
    (3, 2, 20, 8),
    (4, 1, 60, 12),
    (4, 2, 35, 12);

INSERT INTO public.inventory_movements (product_id, location_id, change_qty, reason, note) VALUES
    (1, 1, 25, 'OPENING_STOCK', 'Initial stock load'),
    (1, 2, 10, 'OPENING_STOCK', 'Initial stock load'),
    (2, 1, 50, 'OPENING_STOCK', 'Initial stock load'),
    (2, 2, 30, 'OPENING_STOCK', 'Initial stock load'),
    (3, 1, 40, 'OPENING_STOCK', 'Initial stock load'),
    (3, 2, 20, 'OPENING_STOCK', 'Initial stock load'),
    (4, 1, 60, 'OPENING_STOCK', 'Initial stock load'),
    (4, 2, 35, 'OPENING_STOCK', 'Initial stock load');

SELECT setval('public.roles_role_id_seq', (SELECT MAX(role_id) FROM public.roles));
SELECT setval('public.permissions_permission_id_seq', (SELECT MAX(permission_id) FROM public.permissions));
SELECT setval('public.locations_location_id_seq', (SELECT MAX(location_id) FROM public.locations));
SELECT setval('public.categories_category_id_seq', (SELECT MAX(category_id) FROM public.categories));
SELECT setval('public.users_user_id_seq', (SELECT MAX(user_id) FROM public.users));
SELECT setval('public.products_product_id_seq', (SELECT MAX(product_id) FROM public.products));
SELECT setval('public.inventory_inventory_id_seq', (SELECT MAX(inventory_id) FROM public.inventory));
SELECT setval('public.inventory_movements_movement_id_seq', (SELECT MAX(movement_id) FROM public.inventory_movements));
SELECT setval('public.user_locations_user_location_id_seq', (SELECT MAX(user_location_id) FROM public.user_locations));
