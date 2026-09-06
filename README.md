# WEP-BAITAP02 — Ứng dụng quản lý Category bằng Jakarta Servlet/JSP

## 1. Giới thiệu

Đây là bài tập Java Web triển khai mô hình MVC 3 tầng với Jakarta Servlet/JSP, Hibernate ORM và SQL Server. Ứng dụng gồm hai nhóm chức năng chính:

- Quản lý tài khoản: đăng ký, kích hoạt bằng OTP gửi qua email, đăng nhập, ghi nhớ đăng nhập và đặt lại mật khẩu.
- Quản lý Category theo từng tài khoản: CRUD, tìm kiếm, phân trang và upload ảnh riêng cho owner.

Luồng xử lý được tổ chức theo mô hình:

```text
JSP View → Servlet Controller/Filter → Service → DAO → JPA EntityManager → SQL Server
```

## 2. Các chức năng đã hoàn thành

### 2.1. Đăng ký và kích hoạt tài khoản bằng OTP

Khi người dùng đăng ký, hệ thống thực hiện các bước sau:

1. Kiểm tra dữ liệu bắt buộc và định dạng email, số điện thoại, mật khẩu.
2. Kiểm tra email, username và số điện thoại đã tồn tại hay chưa.
3. Tạo tài khoản người dùng với vai trò User và trạng thái **chưa kích hoạt**.
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
- Sau đăng nhập, người dùng được chuyển đến trang phù hợp với role: Admin, Manager hoặc User.

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

### 2.4. Quản lý Category và phân quyền

- Mỗi Category thuộc đúng một tài khoản thông qua khóa ngoại `categories.user_id`.
- Mọi tài khoản đã kích hoạt đều có thể mở trang Category và chỉ thấy dữ liệu do mình tạo.
- Admin, Manager và User vẫn được giữ trong `role_id` để tương thích dữ liệu, nhưng role không giới hạn CRUD Category cá nhân.
- Category cũ được migration gán cho tài khoản `admin`.
- Một tài khoản không được sửa hoặc xóa Category thuộc tài khoản khác, kể cả khi thay đổi ID trong URL.
- Hỗ trợ thêm, sửa, xóa, tìm kiếm và phân trang Category.
- Ảnh Category có thể lấy từ URL hoặc upload, giới hạn 5 MB và kiểm tra định dạng.
- Filter xử lý UTF-8, xác thực Session và phân quyền URL.

## 3. Cấu trúc mã nguồn liên quan đến tài khoản

- `entity/User.java`: thông tin tài khoản, trạng thái, OTP và thời hạn OTP.
- `entity/Category.java`: Category và owner là User sở hữu Category.
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

Nếu dùng port cố định, có thể đặt:

```powershell
$env:DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=jakartaJPA;encrypt=true;trustServerCertificate=true"
```

### 5.2. Biến môi trường

Đặt các biến môi trường cho đúng tiến trình chạy Tomcat:

```powershell
$env:DB_USER = "sa"
$env:DB_PASSWORD = "YOUR_PASSWORD"
$env:UPLOAD_DIR = "D:\BAITAP02\uploads"
```

Không ghi mật khẩu thật vào source code, `persistence.xml` hoặc Git.

### 5.3. Cấu hình gửi OTP qua Gmail

Ứng dụng gửi OTP bằng SMTP Gmail:

```powershell
$env:SMTP_HOST = "smtp.gmail.com"
$env:SMTP_PORT = "587"
$env:SMTP_USER = "your-sender@gmail.com"
$env:SMTP_PASSWORD = "YOUR_GOOGLE_APP_PASSWORD"
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

Migration thêm `categories.user_id`, gán Category cũ cho `admin`, tạo foreign key và unique index theo `(user_id, category_name)`. Migration không xóa Category hoặc Video.

Sau khi database mới hoặc migration đã hoàn tất, nạp dữ liệu mẫu:

```powershell
sqlcmd -S ".\SQLEXPRESS" -U sa -P "YOUR_PASSWORD" -i sql\02-seed-data.sql
```

Truy cập: `http://localhost:8080/WEP-BAITAP02/`

| Role | Username | Password | Trang đích | Quyền Category |
|---|---|---|---|---|
| Admin | `admin` | `123456` | `/home` | CRUD Category của admin |
| Manager | `manager` | `123456` | `/home` | CRUD Category của manager |
| User | `member` | `123456` | `/home` | CRUD Category của member |

## 6. Kiểm thử

Unit test không yêu cầu SQL Server. Lệnh kiểm thử hiện tại:

```powershell
mvn test
```

Kết quả kiểm tra gần nhất: **40 test đạt, 0 test lỗi, 1 smoke test SQL Server được bỏ qua** vì smoke test chỉ chạy khi bật biến môi trường:

```powershell
$env:RUN_JPA_SMOKE = "true"
mvn test
```

Smoke test tạo Category và Video, kiểm tra quan hệ JPA rồi rollback transaction để không để lại dữ liệu thử nghiệm.

Các nhóm kiểm thử tài khoản bao gồm:

- Đăng nhập đúng/sai và chặn tài khoản chưa kích hoạt.
- Tạo tài khoản với role User và kiểm tra dữ liệu trùng.
- Xác thực OTP đúng, sai và hết hạn.
- Gửi lại OTP kích hoạt.
- Gửi OTP quên mật khẩu và cập nhật mật khẩu.
- Không cho phép reset password bằng email/định danh thay đổi từ request.
- Xóa OTP sau khi đổi mật khẩu thành công.
- Hai tài khoản được dùng cùng tên Category nhưng không nhìn thấy dữ liệu của nhau.
- Tài khoản không được sửa/xóa Category thuộc owner khác.
- Cookie của tài khoản chưa kích hoạt không được khôi phục Session.

## 7. Lưu ý phạm vi bài tập

Mô hình dữ liệu chính:

```text
User 1 ─── N Category 1 ─── N Video
```

Owner luôn được lấy từ Session của tài khoản đăng nhập. Không truyền `userId` qua URL, form hoặc hidden input.

Theo yêu cầu đối chiếu bài giảng, mật khẩu hiện được lưu dạng thường và Cookie ghi nhớ chứa username. Đây là cách triển khai phục vụ bài tập, không nên dùng nguyên trạng cho production. Hệ thống thực tế nên băm mật khẩu, dùng remember-token ngẫu nhiên, giới hạn số lần gửi OTP và bổ sung cơ chế chống dò tài khoản.

Không commit mật khẩu SQL Server/Gmail, file `.env`, thư mục `uploads` hoặc thư mục `target`.
