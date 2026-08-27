# WEP-BAITAP02 - Servlet MVC, JPA và SQL Server

Bài tập Java Web triển khai MVC 3 tầng với Jakarta Servlet/JSP, Hibernate ORM và SQL Server. Project có đăng ký, đăng nhập bằng Session, ghi nhớ bằng Cookie, điều hướng theo role và CRUD Category bằng JPA API.

## Công nghệ

- Java 21, Maven 3.9+
- Apache Tomcat 11 (Servlet 6.1, JSP 4.0)
- Hibernate ORM 7.4.6.Final / Jakarta Persistence 3.2
- Hibernate Validator 9.1.3.Final
- Microsoft JDBC Driver 13.4.0.jre11
- Jakarta Tags (JSTL) 3.0
- SQL Server 2022 Express, database `jakartaJPA`

## Chức năng

- Đăng ký tài khoản, kiểm tra trùng email/username/phone.
- Đăng nhập, Session, Cookie remember-me 30 phút, đăng xuất.
- Điều hướng role: Admin = 1, Manager = 2, User = 3.
- Filter UTF-8, xác thực và phân quyền URL.
- CRUD, tìm kiếm, phân trang Category bằng `EntityManager`.
- Entity Category - Video quan hệ one-to-many/many-to-one.
- Ảnh Category từ URL hoặc upload; giới hạn 5 MB và kiểm tra định dạng.

> **Lưu ý học tập:** theo yêu cầu đối chiếu bài giảng, mật khẩu ứng dụng được lưu dạng thường và cookie username có thể khôi phục Session. Không dùng cơ chế này cho hệ thống production; production phải hash mật khẩu và dùng remember-token không thể giả mạo.

## 1. Chuẩn bị SQL Server

Yêu cầu SQL Server Express instance `SQLEXPRESS`, SQL Server Authentication đã bật và SQL Browser đang chạy.

Chạy script tạo database bằng SSMS hoặc `sqlcmd`:

```powershell
sqlcmd -S ".\SQLEXPRESS" -U sa -P "YOUR_PASSWORD" -i sql\01-create-database.sql
```

Nếu máy dùng port cố định, có thể đặt biến `DB_URL` thay cho URL mặc định:

```powershell
$env:DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=jakartaJPA;encrypt=true;trustServerCertificate=true"
```

## 2. Cấu hình môi trường

Không ghi mật khẩu thật vào `persistence.xml` hoặc Git. Trước khi build/chạy Tomcat:

```powershell
$env:DB_USER = "sa"
$env:DB_PASSWORD = "YOUR_PASSWORD"
$env:UPLOAD_DIR = "D:\BAITAP02\uploads"
```

Tomcat chạy bằng Windows Service phải nhận các biến trên trong môi trường của service. Khi chạy Tomcat từ terminal/IDE, đặt biến trong cùng terminal/Run Configuration.

## 3. Tạo bảng, seed và chạy

Build WAR:

```powershell
mvn clean test
mvn clean package
```

Copy `target\WEP-BAITAP02.war` vào thư mục `webapps` của Tomcat 11 rồi khởi động Tomcat. Lần chạy đầu, Hibernate `hbm2ddl.auto=update` tạo các bảng.

Sau khi bảng đã được tạo, chạy dữ liệu mẫu:

```powershell
sqlcmd -S ".\SQLEXPRESS" -U sa -P "YOUR_PASSWORD" -i sql\02-seed-data.sql
```

Mở: <http://localhost:8080/WEP-BAITAP02/>

| Role | Username | Password | Trang đích |
|---|---|---|---|
| Admin | `admin` | `123456` | `/admin/home` |
| Manager | `manager` | `123456` | `/manager/home` |
| User | `member` | `123456` | `/home` |

## 4. Kiểm thử JPA với SQL Server thật

Unit test không cần database. Smoke test bị tắt mặc định để build chạy được trên máy chưa cấu hình SQL Server. Bật smoke test sau khi đặt credentials:

```powershell
$env:RUN_JPA_SMOKE = "true"
mvn test
```

Smoke test tạo Category + Video, đọc lại quan hệ rồi rollback nên không để lại dữ liệu rác.

## Cấu trúc MVC

```text
Controller / Filter -> Service interface -> DAO interface -> JPA EntityManager -> SQL Server
                                      |
JSP View <----------------------------+
```

- `entity`: `User`, `Category`, `Video`.
- `dao`: truy vấn và transaction JPA.
- `service`: validation và business rules.
- `controller`, `filter`: HTTP flow, Session/Cookie và authorization.
- `WEB-INF/views`: JSP không thể truy cập trực tiếp.

## Kịch bản quay video nộp bài

1. Mở cấu trúc project, `pom.xml`, `persistence.xml` và ba entity.
2. Chạy `mvn clean test` để chứng minh unit test pass.
3. Đăng ký tài khoản mới, đăng nhập và giải thích Session.
4. Logout; đăng nhập Admin có chọn ghi nhớ, đóng/mở lại trình duyệt để chứng minh Cookie.
5. Trình diễn phân quyền Admin/Manager/User.
6. Trình diễn thêm Category bằng URL, thêm bằng upload, sửa không đổi ảnh, tìm kiếm, phân trang và xóa.
7. Mở SSMS chứng minh dữ liệu nằm trong `jakartaJPA` và câu SQL do Hibernate sinh trong log Tomcat.
8. Chạy JPA smoke test Category - Video và cho thấy transaction rollback.

## GitHub

Remote nộp bài: <https://github.com/lwd7071/WEP-BAITAP02>

Không commit `.env`, mật khẩu SQL Server, thư mục `uploads` hoặc output `target`.
