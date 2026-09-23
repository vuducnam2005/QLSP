CREATE TABLE settings (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    low_stock_threshold INTEGER NOT NULL DEFAULT 10 CHECK (low_stock_threshold >= 0),
    product_code_prefix VARCHAR(20) NOT NULL DEFAULT 'PRD-' CHECK (length(product_code_prefix) BETWEEN 1 AND 20),
    allow_negative_stock BOOLEAN NOT NULL DEFAULT FALSE,
    workspace_name VARCHAR(120) NOT NULL DEFAULT 'Không gian làm việc',
    currency VARCHAR(3) NOT NULL DEFAULT 'VND' CHECK (currency IN ('VND', 'USD')),
    date_format VARCHAR(20) NOT NULL DEFAULT 'DD/MM/YYYY' CHECK (date_format IN ('DD/MM/YYYY', 'MM/DD/YYYY', 'YYYY-MM-DD')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_settings_username UNIQUE (username)
);

ALTER TABLE products DROP CONSTRAINT IF EXISTS products_stock_quantity_check;
ALTER TABLE inventory_snapshots DROP CONSTRAINT IF EXISTS inventory_snapshots_stock_quantity_check;
ALTER TABLE inventory_snapshots DROP CONSTRAINT IF EXISTS inventory_snapshots_inventory_value_check;
