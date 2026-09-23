ALTER TABLE products
    ADD COLUMN barcode VARCHAR(50),
    ADD COLUMN cost_price NUMERIC(15, 2),
    ADD COLUMN minimum_stock INTEGER,
    ADD COLUMN image_url VARCHAR(1000);

UPDATE products
SET barcode = CASE product_code
        WHEN 'PRD-0001' THEN '8938501230001'
        WHEN 'PRD-0002' THEN '8938501230002'
        WHEN 'PRD-0003' THEN '8938501230003'
        WHEN 'PRD-0004' THEN '8938501230004'
        WHEN 'PRD-0005' THEN '8938501230005'
    END,
    cost_price = CASE product_code
        WHEN 'PRD-0001' THEN 1650000
        WHEN 'PRD-0002' THEN 690000
        WHEN 'PRD-0003' THEN 2450000
        WHEN 'PRD-0004' THEN 8900000
        WHEN 'PRD-0005' THEN 3650000
    END,
    minimum_stock = CASE product_code
        WHEN 'PRD-0001' THEN 20
        WHEN 'PRD-0002' THEN 30
        WHEN 'PRD-0003' THEN 15
        WHEN 'PRD-0004' THEN 8
        WHEN 'PRD-0005' THEN 10
    END
WHERE is_deleted = FALSE;
