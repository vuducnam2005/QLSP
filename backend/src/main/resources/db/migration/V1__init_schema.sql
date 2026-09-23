CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(15, 2) NOT NULL CHECK (price >= 0),
    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_products_product_code UNIQUE (product_code)
);

CREATE INDEX idx_products_name ON products (name);
CREATE INDEX idx_products_status_deleted ON products (status, is_deleted);

INSERT INTO products (product_code, name, description, price, stock_quantity, status)
VALUES
    ('PRD-0001', 'Mechanical Keyboard K87', 'Compact hot-swappable mechanical keyboard with RGB backlight.', 89.90, 120, 'ACTIVE'),
    ('PRD-0002', 'Wireless Mouse M720', 'Ergonomic wireless mouse with silent clicks and multi-device support.', 39.50, 250, 'ACTIVE'),
    ('PRD-0003', 'USB-C Docking Station', '12-in-1 USB-C docking station for office and hybrid work setups.', 129.00, 75, 'ACTIVE'),
    ('PRD-0004', '27-inch 4K Monitor', 'Professional 4K IPS monitor with USB-C video input.', 449.99, 30, 'ACTIVE'),
    ('PRD-0005', 'Noise Cancelling Headphones', 'Over-ear headphones with adaptive active noise cancellation.', 199.00, 60, 'INACTIVE');
