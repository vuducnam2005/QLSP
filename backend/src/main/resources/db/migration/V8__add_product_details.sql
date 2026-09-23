ALTER TABLE products
    ADD COLUMN category VARCHAR(100),
    ADD COLUMN brand VARCHAR(100),
    ADD COLUMN supplier VARCHAR(255),
    ADD COLUMN unit VARCHAR(50),
    ADD COLUMN warehouse_location VARCHAR(100),
    ADD COLUMN warranty_months INTEGER;

UPDATE products
SET category = CASE product_code
        WHEN 'PRD-0001' THEN 'Thiết bị văn phòng'
        WHEN 'PRD-0002' THEN 'Phụ kiện máy tính'
        WHEN 'PRD-0003' THEN 'Thiết bị văn phòng'
        WHEN 'PRD-0004' THEN 'Màn hình'
        WHEN 'PRD-0005' THEN 'Âm thanh'
    END,
    brand = CASE product_code
        WHEN 'PRD-0001' THEN 'K87 Studio'
        WHEN 'PRD-0002' THEN 'Logitech'
        WHEN 'PRD-0003' THEN 'Baseus'
        WHEN 'PRD-0004' THEN 'ViewSonic'
        WHEN 'PRD-0005' THEN 'Soundcore'
    END,
    supplier = CASE product_code
        WHEN 'PRD-0001' THEN 'Nhà phân phối KeyLab'
        WHEN 'PRD-0002' THEN 'Công ty Logitech Việt Nam'
        WHEN 'PRD-0003' THEN 'Nhà phân phối Baseus'
        WHEN 'PRD-0004' THEN 'Nhà phân phối màn hình Minh Phát'
        WHEN 'PRD-0005' THEN 'Nhà phân phối Soundcore'
    END,
    unit = 'cái',
    warehouse_location = CASE product_code
        WHEN 'PRD-0001' THEN 'Kệ A-01'
        WHEN 'PRD-0002' THEN 'Kệ A-02'
        WHEN 'PRD-0003' THEN 'Kệ B-01'
        WHEN 'PRD-0004' THEN 'Kệ C-01'
        WHEN 'PRD-0005' THEN 'Kệ B-03'
    END,
    warranty_months = CASE product_code
        WHEN 'PRD-0001' THEN 24
        WHEN 'PRD-0002' THEN 12
        WHEN 'PRD-0003' THEN 18
        WHEN 'PRD-0004' THEN 24
        WHEN 'PRD-0005' THEN 18
    END
WHERE is_deleted = FALSE;
