# Frontend quản lý sản phẩm

Frontend Vue 3 + Vite + Pinia cho hệ thống quản lý sản phẩm. Giao diện đã được Việt hóa, dùng định dạng ngày/số `vi-VN` và tiền tệ `VND`, đồng thời hỗ trợ danh mục responsive, CRUD, bộ lọc, phân trang, xóa mềm, trạng thái loading/empty/error và xử lý xung đột optimistic locking.

## Chạy local

```powershell
npm install
$env:VITE_API_BASE_URL = "http://localhost:18080"
npm run dev
```

Mở `http://localhost:5173`.

## Build production

```powershell
npm run build
```

Bản build production được Nginx phục vụ thông qua Docker Compose ở thư mục gốc. Trình duyệt gọi backend qua `VITE_API_BASE_URL`, vì vậy giá trị này phải là URL mà trình duyệt truy cập được, không dùng tên service Docker.
