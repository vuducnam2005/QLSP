CREATE TABLE inventory_snapshots (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0),
    price NUMERIC(15, 2) NOT NULL CHECK (price >= 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'LOW_STOCK', 'INACTIVE')),
    inventory_value NUMERIC(20, 2) NOT NULL CHECK (inventory_value >= 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_inventory_snapshots_product_date UNIQUE (product_id, snapshot_date)
);

CREATE INDEX idx_inventory_snapshots_date ON inventory_snapshots (snapshot_date);
CREATE INDEX idx_inventory_snapshots_product_date ON inventory_snapshots (product_id, snapshot_date);
