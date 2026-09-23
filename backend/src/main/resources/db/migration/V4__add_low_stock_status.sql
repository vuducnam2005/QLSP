ALTER TABLE products DROP CONSTRAINT IF EXISTS products_status_check;

UPDATE products
SET status = 'LOW_STOCK'
WHERE stock_quantity < 10;

ALTER TABLE products
    ADD CONSTRAINT products_status_check
    CHECK (status IN ('ACTIVE', 'LOW_STOCK', 'INACTIVE'));
