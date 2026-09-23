-- Keep the bundled demo catalog consistent with the Vietnamese VND UI.
UPDATE products
SET name = 'Bàn phím cơ K87',
    description = 'Bàn phím cơ hot-swap nhỏ gọn với đèn nền RGB.',
    price = 2247500.00
WHERE product_code = 'PRD-0001'
  AND name = 'Mechanical Keyboard K87'
  AND is_deleted = FALSE;

UPDATE products
SET name = 'Chuột không dây M720',
    description = 'Chuột không dây công thái học, nút bấm êm và hỗ trợ nhiều thiết bị.',
    price = 987500.00
WHERE product_code = 'PRD-0002'
  AND name = 'Wireless Mouse M720'
  AND is_deleted = FALSE;

UPDATE products
SET name = 'Đế cắm USB-C đa năng',
    description = 'Đế cắm USB-C 12 trong 1 cho môi trường làm việc văn phòng và kết hợp.',
    price = 3225000.00
WHERE product_code = 'PRD-0003'
  AND name = 'USB-C Docking Station'
  AND is_deleted = FALSE;

UPDATE products
SET name = 'Màn hình 4K 27 inch',
    description = 'Màn hình IPS 4K chuyên nghiệp với đầu vào hình ảnh USB-C.',
    price = 11249750.00
WHERE product_code = 'PRD-0004'
  AND name = '27-inch 4K Monitor'
  AND is_deleted = FALSE;

UPDATE products
SET name = 'Tai nghe chống ồn',
    description = 'Tai nghe chụp tai với tính năng chống ồn chủ động thích ứng.',
    price = 4975000.00
WHERE product_code = 'PRD-0005'
  AND name = 'Noise Cancelling Headphones'
  AND is_deleted = FALSE;
