# E-commerce Backend

Backend cho shop điện tử (điện thoại, laptop), viết bằng **Spring Boot**.
Dự án làm theo kiểu **monolith**, tập trung vào luồng **Đặt hàng → Thanh toán → Giao hàng → Hoàn tiền**.

## Công nghệ
- Java 17, Spring Boot 4.0.3, Maven
- Spring Data JPA + SQL Server
- Spring Security + JWT
- JUnit 5, Mockito

## Làm được gì
- Đăng ký, đăng nhập, phân quyền (user / admin)
- Xem sản phẩm, lọc, tìm kiếm, phân trang
- Giỏ hàng và đặt hàng
- Thanh toán online (cổng giả lập) hoặc COD
- Giao hàng và hoàn tiền
- Ghi lại lịch sử đổi trạng thái của đơn hàng

## Luồng đơn hàng

**Thanh toán online:**
```
Đặt hàng (PENDING) → Thanh toán xong (PAID) → Đang xử lý → Đang giao → Đã giao
```

**Thanh toán khi nhận hàng (COD):**
```
Đặt hàng (PENDING) → Xác nhận → Đang xử lý → Đang giao → Đã giao → Đã thu tiền (PAID)
```

- Đơn còn `PENDING` thì hủy được.
- Đơn online đã thanh toán (`PAID`) thì xin hoàn tiền được.
- Một đơn có thể thanh toán nhiều lần (thử lại nếu lần trước thất bại), nhưng chỉ một lần thành công.
- Trạng thái đơn hàng và trạng thái thanh toán được quản lý **riêng**.

## Điểm nổi bật
- **Chống bán quá số lượng:** trừ kho bằng một câu `UPDATE` có điều kiện.
- **Chống xem đơn của người khác:** mọi truy vấn đơn hàng đều kèm id người dùng.
- **Báo lỗi thống nhất:** một nơi xử lý lỗi, trả JSON rõ ràng (400, 401, 403, 404).
- **Chuyển trạng thái an toàn:** mọi thay đổi đều qua kiểm tra hợp lệ và được ghi log.

## Chạy dự án

Cần: JDK 17, Maven, SQL Server.

1. Tạo database `shop` và chạy file `schema.sql`.
2. Đặt 3 biến môi trường:
   - `DB_USERNAME`: tài khoản SQL Server
   - `DB_PASSWORD`: mật khẩu SQL Server
   - `JWT_SECRET`: chuỗi bí mật, dài ít nhất 32 ký tự
3. Chạy lệnh:
   ```
   mvn spring-boot:run
   ```
   Ứng dụng chạy ở `http://localhost:8080`.

Hai quyền `ROLE_USER` và `ROLE_ADMIN` được tạo tự động khi khởi động.

## Chạy test
```
mvn test
```
Phần lớn test không cần database.

## API chính

| Chức năng | Endpoint |
|---|---|
| Đăng ký / đăng nhập | `POST /api/auth/register`, `POST /api/auth/login` |
| Xem sản phẩm | `GET /api/products` |
| Giỏ hàng | `GET /api/cart`, `POST /api/cart/items` |
| Đặt hàng | `POST /api/orders` |
| Xem / hủy đơn | `GET /api/orders/{id}`, `PATCH /api/orders/{id}/cancel` |
| Thanh toán | `POST /api/payments/{orderId}?method=...`, `POST /api/payments/confirm` |
| Xin hoàn tiền | `POST /api/refunds/orders/{orderId}` |
| Admin: đổi trạng thái đơn | `PATCH /api/admin/orders/{id}/status` |
| Admin: cập nhật giao hàng | `PATCH /api/admin/orders/{id}/shipping` |
| Admin: duyệt hoàn tiền | `PATCH /api/admin/refunds/{id}/status` |

## Ghi chú
Cổng thanh toán là **giả lập**: gửi `"success": true` hoặc `false` ở bước xác nhận để mô phỏng kết quả.