# WEP-BAITAP02 — Ứng dụng quản lý Category bằng Jakarta Servlet/JSP

## 1. Giới thiệu

Đây là bài tập Java Web triển khai mô hình MVC 3 tầng với Jakarta Servlet/JSP, Hibernate ORM và SQL Server. Ứng dụng gồm hai nhóm chức năng chính:

- Quản lý tài khoản: đăng ký, kích hoạt bằng OTP gửi qua email, đăng nhập, ghi nhớ đăng nhập và đặt lại mật khẩu.
- Quản lý Category theo từng tài khoản: CRUD, tìm kiếm, phân trang và upload ảnh riêng cho owner.
- Quản lý Product theo Category: CRUD, ảnh, phân trang 6 sản phẩm/trang, trang chi tiết và 10 sản phẩm mới nhất.

Luồng xử lý được tổ chức theo mô hình:

```text
JSP View → Servlet Controller/Filter → Service → DAO → JPA EntityManager → SQL Server
```

## 2. Các chức năng đã hoàn thành

### 2.1. Đăng ký và kích hoạt tài khoản bằng OTP

Khi người dùng đăng ký, hệ thống thực hiện các bước sau:

1. Kiểm tra dữ liệu bắt buộc và định dạng email, số điện thoại, mật khẩu.
2. Kiểm tra email, username và số điện thoại đã tồn tại hay chưa.
3. Tạo tài khoản người dùng với trạng thái **chưa kích hoạt**.
4. Sinh mã OTP 6 chữ số, lưu thời hạn 5 phút và gửi mã đến email đăng ký.
5. Chuyển người dùng đến màn hình xác thực OTP.
6. Nếu OTP đúng và còn hạn, tài khoản chuyển sang trạng thái hoạt động; OTP và thời hạn được xóa.

Người dùng có thể yêu cầu gửi lại OTP. Mã mới sẽ thay thế mã cũ. Nếu SMTP chưa cấu hình hoặc Gmail từ chối gửi, hệ thống báo lỗi rõ ràng và không hiển thị thông báo gửi thành công giả.

### 2.2. Đăng nhập

- Đăng nhập bằng username và mật khẩu.
- Tài khoản chưa kích hoạt không được đăng nhập và có liên kết đến trang xác thực OTP.
- Sau khi đăng nhập thành công, tài khoản được lưu trong Session.
- Tùy chọn “Ghi nhớ 30 phút” lưu username bằng HttpOnly Cookie.
- Khi khôi phục đăng nhập từ Cookie, hệ thống vẫn kiểm tra trạng thái tài khoản trước khi tạo Session.
- Sau khi đăng nhập thành công, người dùng được chuyển đến trang chủ `/home` để quản lý Category và Product của riêng mình.

### 2.3. Quên mật khẩu bằng OTP gửi đến email đã đăng ký

Luồng quên mật khẩu đã được điều chỉnh theo username, không yêu cầu người dùng nhập lại email:

1. Người dùng nhập username trên màn hình đăng nhập.
2. Nhấn “Quên mật khẩu?”.
3. Hệ thống tìm tài khoản theo username và lấy email đã lưu trong database.
4. Hệ thống tạo OTP đặt lại mật khẩu và gửi đến email đó.
5. Người dùng nhập OTP, mật khẩu mới và xác nhận mật khẩu.
6. Nếu OTP đúng và còn hạn, mật khẩu được cập nhật; OTP bị xóa ngay sau khi sử dụng.

Định danh tài khoản đặt lại mật khẩu được lưu trong Session. Màn hình đặt lại không nhận email từ URL hoặc input ẩn, vì vậy người dùng không thể thay đổi email để đặt lại mật khẩu cho tài khoản khác. Email hiển thị trên màn hình được che một phần, ví dụ `n***@gmail.com`.

Nếu username trống, không tồn tại hoặc tài khoản chưa kích hoạt, hệ thống hiển thị hướng dẫn tương ứng ngay trên trang đăng nhập.

### 2.4. Quản lý Category & Product theo từng người dùng (Data Isolation)

Hệ thống triển khai theo mô hình **mỗi người dùng đều là người quản trị (admin) toàn quyền đối với dữ liệu của chính mình**:

- Mỗi Category và Product đều gắn liền với người tạo qua khóa ngoại `user_id`.
- Mọi tài khoản sau khi đăng nhập đều có toàn quyền CRUD (thêm, sửa, xóa, tìm kiếm, phân trang) trên danh mục và sản phẩm của riêng mình.
- Dữ liệu được cô lập tuyệt đối: Người dùng chỉ thấy và chỉ thao tác được trên dữ liệu do mình tạo ra; không thể can thiệp dữ liệu của người khác kể cả khi thay đổi ID trên URL hay gọi API.
- Hỗ trợ upload ảnh riêng cho từng Category và Product (giới hạn 5 MB, kiểm tra định dạng an toàn).
- Bộ lọc Filter tự động kiểm tra phiên đăng nhập và quyền sở hữu tài nguyên ở mọi tác vụ.

### 2.5. Hệ thống RESTful API chuẩn hóa (Format ApiResponse)

Tất cả các endpoint API trả về JSON đều tuân thủ duy nhất 1 format chuẩn:

```json
{
  "success": true,           // true/false: trạng thái thành công
  "code": 200,              // Mã HTTP status code (200, 201, 400, 401, 403, 404, 500)
  "message": "Thông báo",    // Thông điệp kết quả hoặc nguyên nhân lỗi
  "data": { ... },          // Dữ liệu đối tượng, danh sách, hoặc null
  "timestamp": 1725700000000 // Epoch time (ms)
}
```

Danh sách API:
- `GET /api/categories`: Lấy danh sách danh mục của tài khoản (hỗ trợ `?q=...`, `?page=...&size=...`).
- `GET /api/categories/{id}`: Xem chi tiết danh mục thuộc sở hữu.
- `POST /api/categories`: Tạo mới danh mục (JSON body).
- `PUT /api/categories/{id}`: Cập nhật danh mục thuộc sở hữu (JSON body).
- `DELETE /api/categories/{id}`: Xóa danh mục thuộc sở hữu.
- `GET /api/products`: Lấy danh sách sản phẩm của tài khoản (hỗ trợ `?latest=true`, `?page=...&size=...`).
- `GET /api/products/{id}`: Xem chi tiết sản phẩm thuộc sở hữu.
- `POST /api/products`: Tạo mới sản phẩm (JSON body).
- `PUT /api/products/{id}`: Cập nhật sản phẩm thuộc sở hữu (JSON body).
- `DELETE /api/products/{id}`: Xóa sản phẩm thuộc sở hữu.
- Bảo mật API: Tích hợp trong `AuthorizationFilter`, tự động kiểm tra đăng nhập và trả về JSON lỗi 401 Unauthorized nếu chưa đăng nhập.

## 3. Cấu trúc mã nguồn liên quan

- `dto/ApiResponse.java`: format chuẩn phản hồi REST API.
- `dto/CategoryDto.java` & `dto/ProductDto.java`: DTO trung gian an toàn cho API.
- `controller/api/ApiCategoryController.java`: REST API quản lý danh mục.
- `controller/api/ApiProductController.java`: REST API quản lý sản phẩm.
- `util/JsonUtil.java`: tiện ích cấu hình Jackson, đọc ghi JSON.
- `entity/User.java`: thông tin tài khoản, trạng thái, OTP và thời hạn OTP.
- `entity/Category.java`: Category và owner là User sở hữu Category.
- `entity/Product.java`: Product thuộc Category.
- `controller/RegisterController.java`: tiếp nhận đăng ký.
- `controller/LoginController.java`: đăng nhập và ghi nhớ tài khoản.
- `controller/VerifyOtpController.java`: xác thực và gửi lại OTP kích hoạt.
- `controller/ForgotPasswordController.java`: gửi OTP quên mật khẩu và đổi mật khẩu.
- `service/impl/UserServiceImpl.java`: kiểm tra dữ liệu và xử lý nghiệp vụ tài khoản.
- `dao/impl/UserDao.java`: truy vấn User và cập nhật OTP/mật khẩu trong transaction.
- `dao/impl/CategoryDao.java`: mọi truy vấn Category đều lọc theo `ownerId`.
- `util/EmailUtil.java`: gửi email HTML qua SMTP Gmail.
- `WEB-INF/views/login.jsp`: đăng nhập và nút quên mật khẩu.
- `WEB-INF/views/verify-otp.jsp`: nhập OTP kích hoạt.
- `WEB-INF/views/reset-password.jsp`: nhập OTP và mật khẩu mới.

## 4. Công nghệ sử dụng

- Java 21, Maven 3.9+
- Apache Tomcat 11, Jakarta Servlet 6.1, JSP 4.0
- Hibernate ORM 7.4.6, Jakarta Persistence 3.2
- Jackson Databind 2.18+ (Xử lý JSON cho REST API)
- SQL Server 2022 Express
- Jakarta Tags/JSTL 3.0
- Jakarta Mail API và Eclipse Angus Mail
- JUnit 5 và Mockito

## 5. Cấu hình và chạy chương trình

### 5.1. Cấu hình SQL Server

Yêu cầu SQL Server Express instance `SQLEXPRESS`, bật SQL Server Authentication và SQL Browser. Tạo database bằng:

```powershell
sqlcmd -S ".\SQLEXPRESS" -U sa -P "YOUR_PASSWORD" -i sql\01-create-database.sql
```

Nếu dùng port cố định, đặt trong file `.env` ở thư mục gốc project:

```powershell
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=jakartaJPA;encrypt=false;trustServerCertificate=true
```

### 5.2. File `.env`

Copy `.env.example` thành `.env` rồi điền các giá trị local. Spring Boot tự đọc file `.env` khi khởi động. File `.env` đã nằm trong `.gitignore` và không được commit.

```powershell
Copy-Item .env.example .env
```

Sau đó chạy:

```powershell
mvn spring-boot:run
```

Không cần set `$env:...` thủ công mỗi lần. Không ghi mật khẩu thật vào source code hoặc Git.

### 5.3. Cấu hình gửi OTP qua Gmail

Ứng dụng gửi OTP bằng SMTP Gmail:

Sửa các key sau trong `.env`:

```properties
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-sender@gmail.com
SMTP_PASSWORD=YOUR_GOOGLE_APP_PASSWORD
```

`SMTP_PASSWORD` phải là Google App Password, không phải mật khẩu Gmail thông thường. Tài khoản gửi cần bật xác minh hai bước và tạo App Password. Nếu thiếu cấu hình hoặc gửi thất bại, hệ thống báo lỗi để người dùng thử lại; mã OTP không được giả lập thành công trong log.

### 5.4. Build và khởi động

```powershell
mvn clean test
mvn clean package
```

Copy `target\WEP-BAITAP02.war` vào thư mục `webapps` của Tomcat 11 rồi khởi động Tomcat. Khi chạy lần đầu, Hibernate sử dụng `hbm2ddl.auto=update` để tạo/cập nhật bảng.

Với database cũ, chạy migration trước khi deploy phiên bản mới:

```powershell
sqlcmd -S ".\SQLEXPRESS" -U sa -P "YOUR_PASSWORD" -i sql\03-add-category-owner.sql
```

Migration `03` thêm owner cho Category. Migration `04` tạo bảng Product, khóa ngoại tới Category, index phân trang và dữ liệu sản phẩm mẫu. Các script đều có thể chạy lại an toàn.

Sau khi database mới hoặc migration đã hoàn tất, nạp dữ liệu mẫu:

```powershell
sqlcmd -S ".\SQLEXPRESS" -U sa -P "YOUR_PASSWORD" -i sql\02-seed-data.sql
sqlcmd -S ".\SQLEXPRESS" -U sa -P "YOUR_PASSWORD" -i sql\04-add-products.sql
```

Truy cập: `http://localhost:8080/WEP-BAITAP02/`

| Tài khoản mẫu | Mật khẩu | Phạm vi dữ liệu quản lý (Data Isolation) |
|---|---|---|
| `user1` | `123456` | Toàn quyền quản trị danh mục & sản phẩm của riêng `user1` |
| `user2` | `123456` | Toàn quyền quản trị danh mục & sản phẩm của riêng `user2` |

*(Hai tài khoản trên dùng để đối chiếu tính năng: mỗi người dùng chỉ thấy và quản trị dữ liệu do chính mình tạo ra, hoàn toàn độc lập và không thể can thiệp dữ liệu của người khác).*

## 6. Kiểm thử tự động (Automated Testing)

Dự án được xây dựng kèm bộ kiểm thử tự động bằng JUnit 5 và Mockito, bao phủ các tầng Service, Controller và Filter.

Lệnh thực hiện kiểm thử:

```powershell
mvn test
```

Ngoài ra, dự án hỗ trợ smoke test kiểm tra quan hệ JPA với cơ sở dữ liệu thật (tự động rollback transaction sau khi kiểm tra):

```powershell
$env:RUN_JPA_SMOKE = "true"
mvn test
```

Các phạm vi chức năng được kiểm thử tự động:

- **Tài khoản & Xác thực:** Đăng nhập đúng/sai, chặn tài khoản chưa kích hoạt, luồng OTP đăng ký/quên mật khẩu và cơ chế khôi phục qua Cookie.
- **Phân quyền & Dữ liệu cá nhân:** Đảm bảo mỗi owner chỉ thao tác trên Category và Product của mình, ngăn chặn can thiệp trái phép qua ID.
- **REST API & Chuẩn Response:** Kiểm thử định dạng cấu trúc `ApiResponse<T>`, mã HTTP status code và tính hợp lệ của dữ liệu JSON trả về.

## 7. Lưu ý phạm vi bài tập

Mô hình dữ liệu chính:

```text
User 1 ─── N Category 1 ─── N Video
```

Owner luôn được lấy từ Session của tài khoản đăng nhập. Không truyền `userId` qua URL, form hoặc hidden input.

Theo yêu cầu đối chiếu bài giảng, mật khẩu hiện được lưu dạng thường và Cookie ghi nhớ chứa username. Đây là cách triển khai phục vụ bài tập, không nên dùng nguyên trạng cho production. Hệ thống thực tế nên băm mật khẩu, dùng remember-token ngẫu nhiên, giới hạn số lần gửi OTP và bổ sung cơ chế chống dò tài khoản.

Không commit mật khẩu SQL Server/Gmail, file `.env`, thư mục `uploads` hoặc thư mục `target`.
