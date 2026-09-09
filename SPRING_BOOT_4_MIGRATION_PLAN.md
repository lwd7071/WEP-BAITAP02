# KẾ HOẠCH CHUYỂN TOÀN BỘ WEP-BAITAP02 SANG SPRING BOOT 4

> Tài liệu giao việc cho Luna. Phải thực hiện đúng thứ tự, không tự mở rộng phạm vi và không để lại hai kiến trúc Servlet/Spring chạy song song.

## 0. Nguồn và trạng thái đã kiểm tra

- Repository: `lwd7071/WEP-BAITAP02`
- Nhánh duy nhất hiện tại: `main`
- Commit được dùng để lập kế hoạch: `9884949f9dd23cd69b30b494be930565804f6229`
- Stack hiện tại: Java 21, Jakarta Servlet/JSP, Hibernate/JPA với `EntityManager` thủ công, SQL Server, SiteMesh, WAR chạy trên Tomcat 11.
- Bảng đã có: `users`, `categories`, `products`, `videos`.
- `Category` hiện có `user_id` và toàn bộ truy vấn Category/Product đang lọc theo ID người đang đăng nhập.
- Quyền hiện tại: `role_id = 1` admin, `2` manager, `3` member; nhưng các URL `/categories`, `/products`, `/product/*` không nằm dưới `/admin/**`, nên mọi user đăng nhập hiện đều CRUD được dữ liệu của chính mình.
- Đăng ký local + OTP, gửi lại OTP, đăng nhập local, remember cookie, quên mật khẩu + OTP đang tồn tại.
- **Google OAuth chưa tồn tại trên nhánh `main`**: chưa có dependency OAuth2 Client, chưa có security handler/service và `login.jsp` chưa có nút Google. Phải triển khai mới bằng Spring Security OAuth2 Client.
- Chưa có CRUD User dành cho admin và chưa có `/api/users`.

## 1. Mục tiêu bắt buộc

Chuyển hoàn toàn sang Spring Boot 4, vẫn dùng JSP/JSTL làm view. Không giữ Servlet controller, Servlet filter, DAO `EntityManager`, `persistence.xml`, `JpaConfig` hoặc session authentication cũ.

Mô hình quyền sau cùng:

| Tác vụ | ADMIN | USER | Chưa đăng nhập |
|---|---:|---:|---:|
| Đăng ký local + OTP | Không cần | Có | Có |
| Đăng nhập local/Google | Có | Có | Có |
| Xem trang chủ, Category hoạt động, Product hoạt động | Có | Có | Không |
| Cập nhật profile | Có | Có | Không |
| CRUD Category | Có | Không | Không |
| CRUD Product | Có | Không | Không |
| CRUD/tìm kiếm/khóa User | Có | Không | Không |
| Gọi API GET Category/Product | Có | Có | Không |
| Gọi API ghi Category/Product/User | Có | Không | Không |

Các bất biến bắt buộc:

1. Hệ thống có đúng một tài khoản `ADMIN`.
2. Mọi tài khoản tạo từ đăng ký local, Google OAuth hoặc màn hình admin tạo user đều có role `USER`.
3. Không có form/API công khai nào nhận `role` từ client.
4. Không được xóa, khóa hoặc hạ quyền admin duy nhất.
5. User chỉ đọc Category/Product hoạt động do admin quản lý; user không sở hữu dữ liệu bán hàng.
6. OTP chỉ kích hoạt tài khoản hoặc đặt lại mật khẩu, tuyệt đối không thay đổi role.

## 2. Quyết định kỹ thuật cố định

- Java 21.
- Spring Boot `4.1.1` (vẫn thuộc Spring Boot 4). Nếu Maven Central tại máy không tải được đúng bản này thì dừng và báo, không tự hạ xuống Spring Boot 3.
- Maven, packaging `war` vì dự án bắt buộc JSP.
- Spring MVC `@Controller` cho trang JSP.
- Spring `@RestController` cho API JSON.
- Spring Data JPA; repository phải `extends JpaRepository`, không giữ DAO thủ công.
- Spring Security cho form login, remember-me, logout, role authorization và Google OAuth2.
- Spring Mail `JavaMailSender` cho OTP.
- Jakarta Bean Validation cho form/request DTO.
- SQL Server giữ nguyên.
- JSP giữ trong `src/main/webapp/WEB-INF/views`; static asset có thể giữ `src/main/webapp/assets` trong WAR hoặc chuyển đồng bộ sang `src/main/resources/static`. Không để cùng một asset tồn tại ở cả hai nơi.
- Không dùng SiteMesh. Thay decorator bằng JSP fragment/include để luồng render hoàn toàn do Spring MVC quản lý.
- Dùng constructor injection; không gọi `new XxxServiceImpl()` hoặc `new XxxRepository()` trong controller/service.
- Dùng `PasswordEncoder` BCrypt. Không tiếp tục so sánh password chuỗi thường.
- API giữ wrapper `ApiResponse<T>`, nhưng trả bằng `ResponseEntity`; xóa `JsonUtil.writeJson/fromJson` khỏi controller.

## 3. Cấu trúc package đích

Giữ root package `vn.iotstar` để giảm thay đổi không cần thiết:

```text
src/main/java/vn/iotstar
├── WepBaitap02Application.java
├── config
│   ├── SecurityConfig.java
│   ├── WebMvcConfig.java
│   └── AdminInitializer.java
├── controller
│   ├── AuthController.java
│   ├── HomeController.java
│   ├── ProfileController.java
│   ├── PublicProductController.java
│   └── admin
│       ├── AdminCategoryController.java
│       ├── AdminProductController.java
│       └── AdminUserController.java
├── controller/api
│   ├── ApiCategoryController.java
│   ├── ApiProductController.java
│   └── ApiUserController.java
├── dto
│   ├── ApiResponse.java
│   ├── CategoryDto.java
│   ├── CategoryForm.java
│   ├── ProductDto.java
│   ├── ProductForm.java
│   ├── RegisterForm.java
│   ├── ResetPasswordForm.java
│   ├── UserAdminCreateForm.java
│   ├── UserAdminUpdateForm.java
│   └── UserDto.java
├── entity
│   ├── User.java
│   ├── Category.java
│   ├── Product.java
│   ├── Role.java
│   ├── UserStatus.java
│   ├── AuthProvider.java
│   └── OtpPurpose.java
├── repository
│   ├── UserRepository.java
│   ├── CategoryRepository.java
│   └── ProductRepository.java
├── security
│   ├── CustomUserDetailsService.java
│   ├── CustomOAuth2UserService.java
│   └── AuthenticationSuccessHandler.java
├── service
│   ├── AuthService.java
│   ├── UserService.java
│   ├── CategoryService.java
│   ├── ProductService.java
│   ├── OtpService.java
│   ├── EmailService.java
│   └── UploadService.java
└── exception
    ├── GlobalExceptionHandler.java
    ├── ResourceNotFoundException.java
    └── BusinessException.java
```

Không bắt buộc giữ cặp interface/impl nếu mỗi service chỉ có một implementation. Nếu giữ interface thì implementation phải là Spring bean `@Service` và vẫn dùng constructor injection.

## 4. `pom.xml` và bootstrap Spring Boot

### 4.1 Thay `pom.xml`

Thay POM hiện tại bằng Spring Boot parent `4.1.1`, giữ Java 21 và `war`. Dùng dependency do Boot quản lý version; không ghim riêng version Hibernate, Jackson, JUnit, Mockito, Servlet API, Validator hoặc SQL Server driver trừ khi thực sự bắt buộc.

Dependency cần có:

- Spring MVC starter của Spring Boot 4.
- `spring-boot-starter-data-jpa`.
- `spring-boot-starter-security`.
- `spring-boot-starter-oauth2-client`.
- `spring-boot-starter-validation`.
- `spring-boot-starter-mail`.
- SQL Server JDBC driver.
- `tomcat-embed-jasper`.
- Jakarta JSTL API và implementation.
- `spring-boot-starter-test`.
- `spring-security-test`.
- H2 chỉ ở scope test nếu repository test dùng H2.

Xóa dependency trực tiếp hiện tại: `jakarta.servlet-api`, `jakarta.servlet.jsp-api`, `jakarta.el-api`, SiteMesh, Hibernate core ghim tay, Hibernate Validator ghim tay, Jackson 2 ghim tay, JUnit/Mockito ghim tay, Angus Mail ghim tay.

Thêm `spring-boot-maven-plugin`. Giữ `maven-war-plugin` nếu cần, nhưng không cấu hình chồng chéo với Boot plugin.

### 4.2 Tạo application class

Tạo `WepBaitap02Application extends SpringBootServletInitializer`:

- Có `main()` gọi `SpringApplication.run`.
- Override `configure(SpringApplicationBuilder)` để WAR deploy ngoài Tomcat vẫn chạy.
- Không thêm `@ServletComponentScan`; toàn bộ `@WebServlet/@WebFilter/@WebListener` phải bị loại bỏ.

### 4.3 Tạo `application.properties`

Cấu hình tối thiểu:

```properties
spring.application.name=WEP-BAITAP02
server.servlet.context-path=/WEP-BAITAP02
server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
server.servlet.session.tracking-modes=cookie

spring.mvc.view.prefix=/WEB-INF/views/
spring.mvc.view.suffix=.jsp

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.format_sql=true

spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=6MB

spring.mail.host=${SMTP_HOST:smtp.gmail.com}
spring.mail.port=${SMTP_PORT:587}
spring.mail.username=${SMTP_USER}
spring.mail.password=${SMTP_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=openid,profile,email

app.upload-dir=${UPLOAD_DIR}
app.admin.username=${ADMIN_USERNAME:admin}
app.admin.email=${ADMIN_EMAIL:admin@iotstar.vn}
app.admin.password=${ADMIN_PASSWORD}
```

Không được để fallback mật khẩu DB như `lvvd7071` trong source. Không commit secret.

Tạo `application-test.properties` dùng DB test riêng/H2, mail sender mock và OAuth credentials giả.

## 5. Database và entity migration

### 5.1 `User.java`

Entity hiện tại đã là bảng `users`; sửa entity, không tạo bảng user thứ hai.

Mô hình đích:

| Field | DB column | Quy tắc |
|---|---|---|
| `id` | `id` | Identity |
| `email` | `email` | unique, not null |
| `username` | `username` | unique, not null |
| `fullName` | `full_name` | not null |
| `password` | `password` | nullable cho Google-only, BCrypt cho local |
| `avatar` | `avatar` | nullable |
| `role` | `role` | enum string `ADMIN/USER`, not null |
| `phone` | `phone` | unique, nullable |
| `status` | `status` | enum string `PENDING/ACTIVE/BLOCKED`, not null |
| `provider` | `provider` | enum `LOCAL/GOOGLE`, not null |
| `providerId` | `provider_id` | Google `sub`, nullable, unique với Google user |
| `otpCode` | `otp_code` | nullable |
| `otpExpiry` | `otp_expiry` | nullable |
| `otpPurpose` | `otp_purpose` | `ACTIVATION/PASSWORD_RESET`, nullable |
| `createdDate` | `created_date` | not null |
| `updatedDate` | `updated_date` | not null |

Xóa logic mặc định `roleId = 3`; không cho entity tự quyết định quyền. Role phải được service gán rõ.

Không trả entity User trực tiếp qua API. `UserDto` tuyệt đối không chứa password/OTP/providerId.

### 5.2 `Category.java`

- Giữ quan hệ `owner` và cột `user_id` trong giai đoạn này để không phá dữ liệu, nhưng owner luôn phải là admin duy nhất.
- Đổi unique constraint từ `(user_id, category_name)` thành vẫn giữ cấu trúc đó để migration an toàn; vì chỉ admin tạo Category nên nó hoạt động như unique toàn hệ thống.
- Bỏ `@NamedQuery`; Spring Data repository xử lý truy vấn.
- Không cascade delete Product/Video khi xóa Category.

### 5.3 `Product.java`

- Giữ quan hệ `Product -> Category`.
- Product không cần `user_id`; owner được suy ra qua `product.category.owner`.
- User thường chỉ đọc `status = 1` và Category `status = 1`.
- Admin đọc cả active/inactive.

### 5.4 SQL migration mới

Tạo `sql/05-spring-boot-4-role-auth-migration.sql`, chạy được nhiều lần an toàn:

1. Thêm cột mới nếu chưa tồn tại: `role`, `provider`, `provider_id`, `otp_code`, `otp_purpose`, `updated_date`.
2. Chuyển dữ liệu:
   - Username `admin` -> `role = 'ADMIN'`.
   - Mọi user khác, kể cả manager -> `role = 'USER'`.
   - `role_id` cũ không còn được code mới sử dụng.
   - `status = 0` cũ -> `PENDING`; `status = 1` cũ -> `ACTIVE`.
   - User hiện có -> `provider = 'LOCAL'`.
   - Chuyển `code` cũ sang `otp_code` nếu cần.
3. Gán tất cả Category cũ về `admin.id`. Trước khi gán phải xử lý trùng `category_name` giữa owner; không được xóa âm thầm. Nếu trùng, migration dừng và báo danh sách trùng để xử lý.
4. Tạo filtered unique index SQL Server bảo đảm tối đa một admin:

```sql
CREATE UNIQUE INDEX uk_users_single_admin
ON dbo.users(role)
WHERE role = 'ADMIN';
```

5. Không drop cột cũ ở migration đầu. Sau khi ứng dụng mới chạy và test pass mới tạo `06-drop-legacy-columns.sql` để bỏ `role_id`, `code` nếu muốn.

### 5.5 Password hiện đang lưu plaintext

Phải xử lý trước khi bật Spring Security:

- Tạo runner migration một lần: với user `provider=LOCAL`, nếu password không bắt đầu `$2a$`, `$2b$` hoặc `$2y$`, encode BCrypt rồi lưu.
- Runner phải idempotent, không hash lại chuỗi BCrypt.
- Admin initializer đọc `ADMIN_PASSWORD`; nếu admin chưa có thì tạo BCrypt, nếu đã có password plaintext thì migrate.
- Sau khi migration password pass, không còn code nào được so sánh `password.equals(...)`.

## 6. Repository mới và truy vấn chính xác

### 6.1 `UserRepository`

Cần các method:

```java
Optional<User> findByUsernameIgnoreCase(String username);
Optional<User> findByEmailIgnoreCase(String email);
Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
boolean existsByUsernameIgnoreCase(String username);
boolean existsByEmailIgnoreCase(String email);
boolean existsByPhone(String phone);
boolean existsByUsernameIgnoreCaseAndIdNot(String username, Integer id);
boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);
boolean existsByPhoneAndIdNot(String phone, Integer id);
long countByRole(Role role);
```

Tìm kiếm admin dùng `JpaSpecificationExecutor<User>` hoặc một `@Query` có phân trang. `keyword` phải match không phân biệt hoa thường trên `username`, `fullName`, `email`, `phone`; filter tùy chọn `status` và `provider`. Kết quả `Page<User>` sort mặc định `createdDate DESC, id DESC`.

### 6.2 `CategoryRepository`

```java
Optional<Category> findByCategoryId(Integer id);
Optional<Category> findByCategoryNameIgnoreCaseAndOwnerRole(String name, Role role);
boolean existsByCategoryNameIgnoreCaseAndOwnerRole(String name, Role role);
boolean existsByCategoryNameIgnoreCaseAndOwnerRoleAndCategoryIdNot(String name, Role role, Integer id);
Page<Category> findByOwnerRoleAndCategoryNameContainingIgnoreCase(Role role, String keyword, Pageable pageable);
Page<Category> findByOwnerRole(Role role, Pageable pageable);
List<Category> findByOwnerRoleAndStatusOrderByCategoryNameAsc(Role role, int status);
```

Nếu derived query quá dài, dùng `@Query`. Không truyền ID user đang đăng nhập vào Category service nữa; admin owner được resolve bởi role hoặc `AdminProvider`.

### 6.3 `ProductRepository`

Cần tách query admin và query public:

- Admin: toàn bộ Product thuộc Category của admin, có phân trang.
- User: chỉ Product `status = 1`, Category `status = 1`, Category owner role `ADMIN`.
- Detail public phải dùng cùng điều kiện active, không dùng `findById` rồi mới kiểm tra.
- Latest public: sort `createdDate DESC, productId DESC`, limit 10/24.

Không còn query `ownerId = currentUser.id` cho trang người dùng.

## 7. Service: nghiệp vụ phải nằm ở đâu

### 7.1 `AuthService`

- `register(RegisterForm)`: validate unique; encode password; luôn gán `role=USER`, `provider=LOCAL`, `status=PENDING`; tạo OTP activation 6 số/5 phút; lưu rồi gửi mail.
- `activate(email, otp)`: chỉ chấp nhận `otpPurpose=ACTIVATION`, đúng mã, chưa hết hạn; chuyển `PENDING -> ACTIVE`; xóa OTP; không đổi role.
- `resendActivationOtp(email)`: chỉ cho user `PENDING`; tạo mã mới; không đổi role.
- `requestPasswordReset(username)`: chỉ cho tài khoản `ACTIVE`; tạo OTP purpose `PASSWORD_RESET`; không đổi role.
- `resetPassword(userIdFromSession, otp, password)`: kiểm tra purpose, mã và hạn; BCrypt password mới; xóa OTP; không đổi role.

Không truyền email tùy ý vào reset endpoint để chọn user. Tiếp tục lưu `passwordResetUserId` trong HTTP session như code hiện tại.

### 7.2 `UserService`

- `search(keyword, status, provider, pageable)` chỉ admin gọi.
- `createByAdmin(form)`: luôn role `USER`; nếu tạo local user thì encode password; quy định rõ `ACTIVE` mặc định vì admin tạo, không gửi OTP activation.
- `updateByAdmin(id, form)`: chỉ sửa fullName, email, phone, avatar; không nhận role/password trừ endpoint reset riêng.
- `toggleStatus(id)`: `ACTIVE <-> BLOCKED`; cấm admin.
- `delete(id)`: chỉ xóa role USER; cấm admin; nếu user có legacy Category thì migration phải đã chuyển Category về admin trước.
- `updateProfile(currentUsername, form)`: user tự sửa profile, không role/status/provider.

### 7.3 `CategoryService`

- Tất cả create/update/delete phải được bảo vệ `@PreAuthorize("hasRole('ADMIN')")`.
- `create`: lấy admin từ principal/repository, không lấy ownerId từ request/form.
- `searchAdmin`: tìm Category owner là ADMIN, phân trang ngay tại DB. Không lấy toàn bộ list rồi `subList` như controller hiện tại.
- `findPublicActive`: chỉ active Category của admin.
- `delete`: nếu có Product hoặc Video liên kết thì trả business error; không cascade.
- Duplicate name trả `409 Conflict` ở API và lỗi validation ở JSP.

### 7.4 `ProductService`

- CRUD admin giữ lại để hệ thống vẫn quản lý mặt hàng admin.
- `findPublicProducts` và `findPublicProductDetail` không nhận current user ID; luôn query active Product của admin.
- User không có method ghi nào được controller public gọi.

### 7.5 `EmailService` và `OtpService`

- Chuyển `EmailUtil` static sang Spring bean dùng `JavaMailSender`.
- `OtpService` dùng `SecureRandom`, mã 6 chữ số, hạn 5 phút.
- Test không gửi mail thật; mock `JavaMailSender`/`EmailService`.

## 8. Spring Security và các luồng xác thực

### 8.1 `SecurityConfig`

Cấu hình `SecurityFilterChain`:

```text
permitAll:
  /login
  /register
  /verify-otp/**
  /forgot-password
  /reset-password
  /oauth2/**
  /login/oauth2/**
  /assets/**
  /image
  /error

authenticated USER hoặc ADMIN:
  /
  /home
  /products/**
  /profile/**
  GET /api/categories/**
  GET /api/products/**

hasRole ADMIN:
  /admin/**
  POST/PUT/PATCH/DELETE /api/categories/**
  POST/PUT/PATCH/DELETE /api/products/**
  mọi method /api/users/**
```

Yêu cầu thêm:

- Form login: login page `/login`, processing URL `/login`, parameter `username/password` giữ nguyên JSP.
- `CustomUserDetailsService` load theo username, map `Role.ADMIN -> ROLE_ADMIN`, `Role.USER -> ROLE_USER`; chỉ `ACTIVE` mới enabled.
- Remember-me 30 phút dùng Spring Security token/cookie, không giữ cookie raw username hiện tại.
- Logout đổi thành `POST /logout`, invalidate session và xóa remember-me cookie.
- CSRF bật. Mọi form POST trong JSP phải có hidden `${_csrf.parameterName}`/`${_csrf.token}`.
- HTML chưa đăng nhập redirect `/login`; API chưa đăng nhập trả JSON 401; API thiếu quyền trả JSON 403 bằng custom entry point/access denied handler.
- Bật `@EnableMethodSecurity` và thêm `@PreAuthorize` cho các service ghi admin.

### 8.2 Login local

- `GET /login` do `AuthController` render JSP.
- `POST /login` do Spring Security xử lý, không viết controller POST.
- Login success handler:
  - ADMIN -> `/admin/categories` hoặc `/admin/dashboard` nếu tạo dashboard.
  - USER -> `/home`.
- Xóa `WaitingController`; không còn `/waiting`.
- Xóa session attribute `account`; JSP đọc principal qua Spring Security taglib hoặc model advice.

### 8.3 Google OAuth mới

- Nút trên `login.jsp` trỏ `/oauth2/authorization/google`.
- Callback do Spring Security dùng `/login/oauth2/code/google`.
- `CustomOAuth2UserService` lấy `sub`, `email`, `name`, `picture`.
- Ưu tiên tìm `(provider=GOOGLE, providerId=sub)`; nếu chưa có thì tìm email.
- Nếu chưa có user: tạo `username` duy nhất từ phần trước `@` + suffix khi trùng; `role=USER`, `status=ACTIVE`, `provider=GOOGLE`, password null.
- Nếu email đã thuộc admin LOCAL: **không tự liên kết và không đăng nhập admin bằng Google**; báo lỗi rõ ràng. Admin chỉ đăng nhập local trừ khi có migration liên kết quản trị riêng được phê duyệt.
- Nếu email thuộc USER LOCAL: không tự ghi đè password/provider; yêu cầu đăng nhập local hoặc xây luồng link account riêng ngoài scope.
- Nếu user Google bị `BLOCKED`, từ chối đăng nhập.
- Google login không tạo OTP activation và không bao giờ gán ADMIN.

### 8.4 OTP và quên mật khẩu

Giữ URL người dùng quen thuộc nhưng controller chuyển sang Spring MVC:

| Method | Endpoint | Xử lý mới |
|---|---|---|
| GET | `/register` | Render `register.jsp` |
| POST | `/register` | `AuthService.register`, luôn USER/PENDING |
| GET | `/verify-otp?email=...` | Render form |
| POST | `/verify-otp` | Xác thực activation OTP |
| POST | `/verify-otp/resend` | Gửi lại activation OTP; thay `action=resend` |
| POST | `/forgot-password` | Nhận username, lưu reset user ID trong session, gửi OTP |
| GET | `/reset-password` | Chỉ render nếu session có reset user ID |
| POST | `/reset-password` | Xác thực OTP reset và BCrypt password mới |

## 9. Endpoint MVC phải đổi

### 9.1 Bảng ánh xạ endpoint cũ -> mới

| Endpoint hiện tại | Endpoint Spring đích | Hành động |
|---|---|---|
| `GET ""` | `GET /` | `HomeController`; authenticated, chuyển theo role hoặc render home |
| `GET/POST /login` | giữ URL | GET controller, POST Spring Security |
| `GET /logout` | `POST /logout` | Spring Security; sửa mọi link thành form POST có CSRF |
| `GET /waiting` | bỏ | success handler redirect trực tiếp |
| `/manager/home` | bỏ | không còn role MANAGER |
| `/admin/home` | bỏ hoặc redirect `/admin/categories` | không giữ logic cũ |
| `GET /home` | giữ | luôn đọc Category/Product của admin, không current user owner |
| `GET/POST /profile` | giữ | Spring MVC controller + principal |
| `GET /image?fname=` | giữ URL | chuyển thành Spring `@GetMapping`, dùng `UploadService.safeResolve` |
| `GET /categories` | `GET /admin/categories` | chỉ ADMIN, search/paging DB |
| `GET /category/add` | `GET /admin/categories/new` | chỉ ADMIN |
| `POST /category/insert` | `POST /admin/categories` | chỉ ADMIN |
| `GET /category/edit?id=` | `GET /admin/categories/{id}/edit` | chỉ ADMIN |
| `POST /category/update` | `POST /admin/categories/{id}/update` | chỉ ADMIN |
| `GET /category/delete?id=` | `POST /admin/categories/{id}/delete` | sửa hành vi nguy hiểm GET-delete |
| `GET /products` (admin list cũ) | `GET /admin/products` | chỉ ADMIN |
| `GET /product/add` | `GET /admin/products/new` | chỉ ADMIN |
| `POST /product/insert` | `POST /admin/products` | chỉ ADMIN |
| `GET /product/edit?id=` | `GET /admin/products/{id}/edit` | chỉ ADMIN |
| `POST /product/update` | `POST /admin/products/{id}/update` | chỉ ADMIN |
| `GET /product/delete?id=` | `POST /admin/products/{id}/delete` | chỉ ADMIN |
| `GET /product` | `GET /products` | USER/ADMIN xem catalog admin |
| `GET /product/detail?id=` | `GET /products/{id}` | chỉ trả product active của admin |

### 9.2 MVC Category admin

| Method | Endpoint | Query/form | View/redirect |
|---|---|---|---|
| GET | `/admin/categories` | `q`, `page=0`, `size=10` | `admin/category-list` |
| GET | `/admin/categories/new` | — | `admin/category-add` |
| POST | `/admin/categories` | multipart `CategoryForm` | success redirect list |
| GET | `/admin/categories/{id}/edit` | path id | `admin/category-edit` |
| POST | `/admin/categories/{id}/update` | multipart form | success redirect list |
| POST | `/admin/categories/{id}/delete` | CSRF + path id | success/error redirect list |

`q` trống -> toàn bộ Category admin. `q` có giá trị -> `contains ignore case`. Luôn giữ `q`, `page`, `size` trong link phân trang.

### 9.3 MVC User admin mới

| Method | Endpoint | Query/form | View/redirect |
|---|---|---|---|
| GET | `/admin/users` | `q`, `status`, `provider`, `page=0`, `size=10` | `admin/user-list` |
| GET | `/admin/users/new` | — | `admin/user-add` |
| POST | `/admin/users` | `UserAdminCreateForm` | tạo role USER, redirect list |
| GET | `/admin/users/{id}/edit` | path id | `admin/user-edit` |
| POST | `/admin/users/{id}/update` | update form | redirect list |
| POST | `/admin/users/{id}/toggle-status` | path id + CSRF | ACTIVE/BLOCKED; cấm admin |
| POST | `/admin/users/{id}/delete` | path id + CSRF | xóa USER; cấm admin |

Không cho admin nhập/chọn role trên form. Có thể hiển thị role read-only.

## 10. REST API phải sửa/thêm

Giữ format:

```json
{
  "success": true,
  "code": 200,
  "message": "...",
  "data": {},
  "timestamp": 0
}
```

### 10.1 Category API hiện có: giữ path, đổi authorization và owner semantics

| Method | Endpoint | Quyền | Sửa cụ thể |
|---|---|---|---|
| GET | `/api/categories?q=&page=0&size=10` | USER, ADMIN | Không dùng current user ID; trả Category của admin. USER chỉ thấy active; ADMIN thấy cả hai trạng thái. Trả dữ liệu phân trang, không trả list thiếu total metadata. |
| GET | `/api/categories/{id}` | USER, ADMIN | USER chỉ đọc active Category của admin; ADMIN đọc mọi Category admin. |
| POST | `/api/categories` | ADMIN | Owner lấy từ server/admin principal; bỏ `ownerId/ownerUsername` khỏi request. |
| PUT | `/api/categories/{id}` | ADMIN | Update Category admin, validation + duplicate. |
| DELETE | `/api/categories/{id}` | ADMIN | Trả `204` hoặc wrapper `200`; chọn một chuẩn và test cố định. Kế hoạch này dùng `200` để tương thích `ApiResponse`. |

`CategoryDto` response có thể giữ `ownerUsername`, nhưng request phải dùng `CategoryForm/CategoryRequest` riêng để client không mass-assign owner.

### 10.2 Product API hiện có: giữ path, đổi semantics

| Method | Endpoint | Quyền | Sửa cụ thể |
|---|---|---|---|
| GET | `/api/products?page=0&size=10` | USER, ADMIN | Không dùng current user ID. USER chỉ thấy active product + active category của admin; ADMIN thấy toàn bộ admin products. |
| GET | `/api/products?latest=true` | USER, ADMIN | Trả tối đa 10 product active của admin cho USER. |
| GET | `/api/products/{id}` | USER, ADMIN | USER không được xem product inactive. |
| POST | `/api/products` | ADMIN | Category ID phải thuộc admin. |
| PUT | `/api/products/{id}` | ADMIN | Product và Category đều thuộc admin. |
| DELETE | `/api/products/{id}` | ADMIN | Chỉ admin. |

### 10.3 User API mới

Toàn bộ `/api/users/**` chỉ ADMIN:

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/api/users?q=&status=&provider=&page=0&size=10` | Tìm kiếm/lọc/phân trang |
| GET | `/api/users/{id}` | Chi tiết an toàn, không password/OTP |
| POST | `/api/users` | Tạo local USER; encode password; status ACTIVE |
| PUT | `/api/users/{id}` | Sửa profile user; không nhận role |
| PATCH | `/api/users/{id}/status` | Body `{ "status": "ACTIVE" }` hoặc `BLOCKED`; cấm admin |
| DELETE | `/api/users/{id}` | Xóa USER; cấm admin |

Status code:

- `200` đọc/update/delete thành công.
- `201` tạo thành công.
- `400` validation/request sai.
- `401` chưa đăng nhập.
- `403` không phải admin.
- `404` ID không tồn tại hoặc product/category public không khả dụng.
- `409` username/email/phone/category name trùng hoặc xóa Category còn Product.

### 10.4 Error handling API

Tạo `@RestControllerAdvice`:

- `MethodArgumentNotValidException` -> 400 + map lỗi field.
- `ConstraintViolationException` -> 400.
- `ResourceNotFoundException` -> 404.
- `DataIntegrityViolationException`/duplicate business -> 409.
- `AccessDeniedException` -> 403.
- Exception còn lại -> 500, không trả raw stack trace/message DB.

## 11. JSP/JSTL cần sửa và tạo

### 11.1 Giữ và chuyển đổi

- `login.jsp`: thêm nút Google, hidden CSRF cho form local, hiển thị `?error`, `?logout`, OAuth error; form POST vẫn `/login`.
- `register.jsp`: giữ fields; bỏ nội dung Manager; ghi rõ account mới là USER; thêm CSRF và field error từ `BindingResult`.
- `verify-otp.jsp`, `reset-password.jsp`: đổi action sang endpoint mục 8.4, thêm CSRF.
- `home.jsp`, `product-list.jsp`, `product-detail.jsp`: dữ liệu luôn là sản phẩm admin, không dùng `sessionScope.account` để xác định owner.
- `profile.jsp`: dùng model `currentUser`, Spring multipart và CSRF.
- Category JSP: sửa toàn bộ action/link theo `/admin/categories/**`; xóa câu “của tài khoản hiện tại”; delete thành form POST.
- Product admin JSP: sửa action/link theo `/admin/products/**`; delete thành form POST.

### 11.2 Tạo mới User JSP

- `WEB-INF/views/admin/user-list.jsp`: search keyword + status + provider; bảng id/avatar/username/fullName/email/phone/provider/status/createdDate/actions; badge ADMIN read-only; không hiện nút khóa/xóa admin.
- `admin/user-add.jsp`: username, fullName, email, phone, password, avatar; không role selector.
- `admin/user-edit.jsp`: fullName, email, phone, avatar; username có thể read-only; không password/role.

### 11.3 Bỏ SiteMesh

- Xóa `SiteMeshFilter.java`, hai decorator JSP và dependency SiteMesh.
- Tạo fragment `partials/admin-sidebar.jspf`, `partials/topbar.jspf`, `partials/head.jspf`, `partials/footer.jspf`, `partials/messages.jspf`.
- Mỗi view include fragment trực tiếp. Không tạo filter/decorator thay thế.
- Sửa admin navigation có mục `Danh mục`, `Sản phẩm`, `Người dùng`.

## 12. File cũ phải xóa/thay thế

### 12.1 Xóa sau khi Spring equivalent đã tồn tại và test pass

- `src/main/java/vn/iotstar/config/JpaConfig.java`
- `src/main/java/vn/iotstar/config/JpaLifecycleListener.java`
- `src/main/resources/META-INF/persistence.xml`
- Toàn bộ `src/main/java/vn/iotstar/dao/**`
- `src/main/java/vn/iotstar/filter/AuthorizationFilter.java`
- `src/main/java/vn/iotstar/filter/EncodingFilter.java`
- `src/main/java/vn/iotstar/filter/SiteMeshFilter.java`
- `src/main/java/vn/iotstar/controller/WaitingController.java`
- `src/main/java/vn/iotstar/util/AuthUtil.java`
- `src/main/java/vn/iotstar/util/JsonUtil.java`
- `src/main/webapp/WEB-INF/decorators/**`
- Các Servlet controller cũ sau khi đã thay bằng Spring controller cùng nghiệp vụ.
- Unit test cũ mock `HttpServletRequest/HttpServletResponse/RequestDispatcher`.

### 12.2 Thay vì xóa nghiệp vụ

- `EmailUtil` -> `EmailService` Spring bean.
- `UploadUtil` -> `UploadService` Spring bean/config properties.
- `AppConstants` chỉ giữ hằng số thật sự dùng chung; bỏ session account/cookie username.
- `ICategoryDao/IProductDao/IUserDao` -> Spring Data repository.
- `CategoryController` -> `AdminCategoryController`.
- `ProductController` -> `AdminProductController` + `PublicProductController`.
- Auth Servlet controllers -> `AuthController` + Spring Security.
- API Servlet controllers -> `@RestController`.

### 12.3 `web.xml`

Có thể giữ file tối thiểu chỉ cho JSP encoding nếu WAR container cần, nhưng không đăng ký servlet/filter/listener. Ưu tiên cấu hình bằng Spring Boot properties/config. Không để annotation Servlet component được scan.

## 13. Trình tự triển khai bắt buộc cho Luna

### Phase 1 — Khóa baseline

1. Tạo branch `feature/spring-boot-4-full-migration` từ commit đã nêu.
2. Chạy và ghi kết quả `mvn clean test` trên code cũ.
3. Không sửa UI trước khi Boot context và datasource chạy.

Gate: baseline test hiện tại pass hoặc ghi rõ test nào đã fail trước migration.

### Phase 2 — Dựng Spring Boot shell

1. Thay POM.
2. Tạo application class và properties.
3. Tạo controller tạm `GET /actuator` là không cần; chỉ cần context load test.
4. Cấu hình JSP resolver và WAR.

Gate: `mvn test` có `contextLoads`; `mvn package` tạo WAR; chạy app render được một JSP test.

### Phase 3 — Schema/entity/repository

1. Tạo enums và sửa entity.
2. Viết/chạy migration 05 trên bản sao DB.
3. Tạo repositories và `@DataJpaTest`.
4. Chuyển password plaintext một lần.

Gate: JPA `ddl-auto=validate` khởi động thành công; repository search/paging pass; DB có đúng một ADMIN.

### Phase 4 — Authentication local + OTP

1. Chuyển mail/OTP/auth service.
2. Cấu hình form login/remember/logout.
3. Chuyển register/verify/resend/forgot/reset controller và JSP.
4. Xóa Login/Register/Verify/Forgot/Logout Servlet cũ.

Gate: test đầy đủ các luồng local; không còn password plaintext mới; OTP không đổi role.

### Phase 5 — Google OAuth

1. Thêm OAuth2 config/service/success handler.
2. Thêm nút Google.
3. Viết test mapping claims và blocked user.

Gate: Google user mới luôn USER/ACTIVE; không takeover admin/local email; callback success/failure đúng.

### Phase 6 — Category admin CRUD + search

1. Repository/service/controller MVC/API.
2. Chuyển JSP và image upload.
3. Đổi delete GET thành POST.
4. Xóa owner=current-user semantics.

Gate: ADMIN CRUD/search được; USER 403 mọi write và `/admin/**`; duplicate/delete-linked xử lý đúng.

### Phase 7 — User admin CRUD + search

1. Tạo DTO/service/controller MVC/API/JSP.
2. Bảo vệ admin duy nhất.
3. Search DB-level + paging.

Gate: admin quản lý USER; mọi cách tạo tài khoản đều không thể tạo admin thứ hai.

### Phase 8 — Product và public catalog

1. Chuyển Product DAO/service/controller/API sang Spring.
2. Tách admin management và public read.
3. Home/catalog đọc sản phẩm admin thay vì sản phẩm current user.

Gate: hai USER khác nhau nhìn cùng catalog admin; USER không CRUD được; inactive không lộ.

### Phase 9 — Profile/upload/layout và dọn legacy

1. Chuyển profile/image.
2. Bỏ SiteMesh, dùng fragments.
3. Xóa toàn bộ file legacy mục 12.
4. Dùng `rg` xác nhận không còn `@WebServlet`, `@WebFilter`, `@WebListener`, `extends HttpServlet`, `JpaConfig`, `Persistence.createEntityManagerFactory`, `new UserServiceImpl`, `sessionScope.account`, `roleId` trong main code.

Gate: chỉ còn Spring Boot runtime; không còn đường chạy Servlet cũ.

### Phase 10 — Regression, docs, commit

1. Chạy toàn bộ automated test.
2. Manual test bằng SQL Server + SMTP test + Google OAuth local.
3. Cập nhật `.env.example` và README.
4. Không ghi secret/log OTP/password.

Gate: checklist mục 15 pass.

## 14. Kế hoạch test chi tiết

### 14.1 Build/static checks

```bash
mvn clean test
mvn clean package
rg "@WebServlet|@WebFilter|@WebListener|extends HttpServlet|JpaConfig|Persistence.createEntityManagerFactory|sessionScope.account|roleId" src/main
```

Kết quả `rg` phải rỗng, trừ tài liệu migration SQL có nhắc `role_id` cũ.

### 14.2 Entity/repository tests

`UserRepositoryTest`:

- Tìm username/email không phân biệt hoa thường.
- Unique username/email/phone.
- Search keyword match username, fullName, email, phone.
- Filter status/provider kết hợp keyword.
- Paging/sort ổn định.
- `countByRole(ADMIN) == 1` trong dataset chuẩn.

`CategoryRepositoryTest`:

- Search contains ignore case chỉ Category của ADMIN.
- USER legacy Category không xuất hiện.
- Public list chỉ `status=1`.
- Duplicate name admin bị chặn.
- Paging thực hiện tại DB.

`ProductRepositoryTest`:

- User thấy product active + category active + owner ADMIN.
- Product inactive, category inactive và product legacy owner USER không xuất hiện.
- Detail public áp dụng đúng điều kiện, trả empty nếu không hợp lệ.
- Latest order đúng `createdDate DESC, id DESC`.

### 14.3 Service unit tests

`AuthServiceTest`:

- Register luôn set USER/LOCAL/PENDING dù request cố chèn role.
- Password được BCrypt.
- OTP 6 số, hạn 5 phút, purpose ACTIVATION.
- OTP đúng -> ACTIVE, giữ USER.
- OTP sai/hết hạn/sai purpose -> thất bại.
- Resend thay code cũ, giữ USER.
- Reset password dùng purpose PASSWORD_RESET, BCrypt password mới, xóa OTP, giữ role.
- Email send fail trả thông báo hợp lý và tài khoản vẫn ở trạng thái có thể resend.

`UserServiceTest`:

- Admin create luôn USER/ACTIVE.
- Duplicate username/email/phone bị chặn.
- Update không đổi role/provider/password.
- Không khóa/xóa admin.
- Khóa/mở USER đúng.
- Search truyền Pageable xuống repository.

`CategoryServiceTest`:

- Create gắn admin owner, không current USER.
- Duplicate bị chặn.
- Update/delete chỉ admin.
- Không xóa Category còn Product/Video.
- Upload fail không để file rác; DB fail sau upload phải cleanup file mới.

`ProductServiceTest`:

- CRUD chỉ admin.
- Category của product phải là Category admin.
- Public list/detail không phụ thuộc người đang đăng nhập.

`CustomOAuth2UserServiceTest`:

- Google user mới -> USER/ACTIVE/GOOGLE.
- Username collision sinh username khác.
- Existing Google user được reuse.
- BLOCKED bị từ chối.
- Email admin local không bị takeover.
- Email user local không bị tự đổi provider.

### 14.4 MVC/Security tests bằng MockMvc

Các test bắt buộc:

| Case | Expected |
|---|---|
| Anonymous `GET /login`, `/register` | 200 |
| Anonymous `GET /home` | 302 tới login |
| USER `GET /admin/categories` | 403 |
| USER `POST /admin/categories` có CSRF | 403 |
| ADMIN `GET /admin/categories?q=phone` | 200 + gọi search đúng |
| ADMIN tạo Category hợp lệ | 302 redirect list |
| ADMIN tạo Category validation sai | 200 form + field error |
| Delete Category bằng GET | 405/404, không xóa |
| POST delete không CSRF | 403 |
| USER `GET /admin/users` | 403 |
| ADMIN CRUD User | đúng 200/302 |
| Admin cố khóa/xóa chính mình | báo business error, dữ liệu không đổi |
| Local login USER | redirect `/home` |
| Local login ADMIN | redirect admin page |
| PENDING/BLOCKED login | authentication failure |
| POST logout có CSRF | session invalid + redirect login |

### 14.5 REST API tests bằng MockMvc

Cho từng API Category/Product/User phải test:

- Anonymous -> 401 JSON đúng `ApiResponse`, không redirect HTML.
- USER gọi GET được theo phạm vi public.
- USER gọi POST/PUT/PATCH/DELETE -> 403 JSON.
- ADMIN gọi CRUD thành công.
- Invalid body -> 400 + field errors.
- Not found -> 404.
- Duplicate/conflict -> 409.
- Response không serialize password, OTP, providerId.
- `page/size` âm hoặc vượt giới hạn -> normalize hoặc 400 theo một chuẩn; kế hoạch chọn 400, `size` tối đa 100.

### 14.6 OTP/mail tests

- Mock `EmailService`, không gửi Gmail thật trong unit/integration test.
- Kiểm tra subject activation/reset khác nhau.
- OTP hết hạn chính xác.
- OTP dùng một lần; lần hai thất bại.
- OTP activation không dùng được cho reset và ngược lại.
- Không log OTP ở production profile.

### 14.7 Manual acceptance với SQL Server

Chuẩn bị 3 account:

1. `admin`: ADMIN/ACTIVE.
2. `localuser`: USER sau đăng ký + OTP.
3. `googleuser`: USER tạo từ Google.

Thử tay:

1. Admin đăng nhập -> thấy menu Category/Product/User, CRUD + search hoạt động.
2. Local user đăng nhập -> không thấy menu admin; nhập trực tiếp URL admin nhận 403.
3. Google user đăng nhập -> cùng catalog với local user.
4. Admin tạo Category/Product -> cả hai user thấy khi status active.
5. Admin khóa Product/Category -> user không còn thấy; admin vẫn thấy trong quản lý.
6. Admin khóa localuser -> phiên mới không đăng nhập được; nếu cần hiệu lực ngay thì implement session invalidation ngoài scope, còn mặc định áp dụng từ request authentication/đăng nhập kế tiếp.
7. Admin tìm User theo username/email/phone và Category theo tên, giữ đúng phân trang.
8. Quên mật khẩu local -> nhận OTP, đổi mật khẩu, role vẫn USER.
9. Kiểm tra DB: đúng một row `role='ADMIN'`; password local đều BCrypt; không OTP tồn tại sau khi dùng.

## 15. Definition of Done

Chỉ báo hoàn thành khi tất cả điều sau đúng:

- [ ] Project khởi động bằng Spring Boot 4 và build WAR thành công.
- [ ] Không còn Servlet controller/filter/listener hoặc JPA bootstrap thủ công.
- [ ] Không còn SiteMesh; JSP/JSTL vẫn render đúng.
- [ ] SQL Server schema validate thành công.
- [ ] Đúng một ADMIN; không endpoint nào tạo ADMIN thứ hai.
- [ ] Local register + activation OTP còn hoạt động và tạo USER.
- [ ] Forgot/reset OTP còn hoạt động, password BCrypt, role không đổi.
- [ ] Google OAuth hoạt động và user mới luôn USER.
- [ ] Admin CRUD + search Category.
- [ ] Admin CRUD + search/filter User.
- [ ] Admin CRUD Product hiện có vẫn hoạt động.
- [ ] USER xem Category/Product active của admin.
- [ ] USER không CRUD qua cả MVC lẫn API.
- [ ] Delete dùng POST/DELETE, không còn GET làm thay đổi dữ liệu.
- [ ] CSRF bật và mọi form POST có token.
- [ ] API 401/403 trả JSON, không redirect HTML.
- [ ] Không response/log nào lộ password, OTP, SMTP/Google/DB secret.
- [ ] Toàn bộ automated test pass.
- [ ] README và `.env.example` cập nhật đủ DB/SMTP/Google/Admin variables.

## 16. Quy tắc làm việc dành cho Luna

1. Không đổi giao diện tổng thể nếu không cần cho endpoint/role mới; ưu tiên giữ CSS và cấu trúc nhìn hiện tại.
2. Không viết lại rút gọn làm mất Product, profile, upload, OTP hoặc API response hiện có.
3. Không giữ compatibility shim gọi DAO/Servlet cũ.
4. Mỗi phase phải build/test trước khi sang phase kế tiếp.
5. Khi một test cũ kiểm tra owner=current-user, phải xóa/viết lại theo owner=ADMIN; không cố làm cả hai semantics cùng lúc.
6. Không sửa trực tiếp `main`; làm trên branch feature và commit theo phase.
7. Nếu migration gặp Category trùng tên giữa nhiều owner, dừng và báo dữ liệu xung đột; không xóa record tự động.
8. Nếu Google OAuth chưa có credentials, vẫn phải hoàn thành code + mocked tests; manual OAuth được đánh dấu blocked bởi credentials, không giả vờ pass.
9. Không dùng `ddl-auto=update` làm migration production; dùng script SQL và `ddl-auto=validate`.
10. Sau cùng báo rõ: file tạo/sửa/xóa, endpoint thay đổi, migration đã chạy ở môi trường nào, kết quả từng nhóm test và việc còn blocked.

## 17. Commit đề xuất

```text
chore: bootstrap Spring Boot 4 WAR application
feat: migrate entities and repositories to Spring Data JPA
feat: migrate local authentication OTP and password reset
feat: add Spring Security roles remember-me and Google OAuth
feat: migrate admin category CRUD and search
feat: add admin user CRUD search and status management
feat: migrate product management and public admin catalog
refactor: replace SiteMesh and servlet utilities with Spring components
test: add repository MVC API security OAuth and OTP coverage
docs: document Spring Boot setup migrations and environment variables
```

---

## 18. Kế hoạch triển khai TDD chi tiết (RED – GREEN – REFACTOR)

> Mục này quy định quy trình Test-Driven Development chuẩn cho toàn bộ quá trình chuyển đổi sang Spring Boot, tuân thủ nguyên tắc **Vertical Slice (Lát cắt dọc - Tracer Bullet)**.
> Tuyệt đối không làm theo kiểu Horizontal Slice (không viết dồn toàn bộ test rồi mới viết code).
> Mỗi tính năng đều đi qua chu trình: **RED** (viết 1 test hành vi cụ thể, test fail) ➔ **GREEN** (viết lượng code tối thiểu để test pass) ➔ **REFACTOR** (tối ưu mã nguồn, test vẫn pass).

### 18.1 Bảng tổng hợp Endpoint chi tiết cho toàn hệ thống

| Chức năng | Method | Endpoint | Quyền | Params / Body | Kết quả |
|---|---|---|---|---|---|
| Đăng nhập | GET | `/login` | Public | — | Hiển thị JSP `login.jsp` |
| Đăng nhập local | POST | `/login` | Public | `username`, `password`, `remember-me` | Spring Security xử lý, redirect `/home` hoặc `/admin/categories` |
| Đăng ký | GET | `/register` | Public | — | Hiển thị JSP `register.jsp` |
| Đăng ký | POST | `/register` | Public | Form `RegisterForm` | Tạo `USER/PENDING`, gửi OTP, redirect `/verify-otp?email=...` |
| Form xác thực OTP | GET | `/verify-otp` | Public | `email` | Hiển thị `verify-otp.jsp` |
| Xác nhận kích hoạt OTP | POST | `/activate` | Public | `email`, `otp` | Kích hoạt tài khoản `ACTIVE`, redirect `/login?verified=true` |
| Gửi lại OTP | POST | `/otp/resend` | Public | `email` | Sinh OTP mới, gửi email |
| Quên mật khẩu | POST | `/forgot-password` | Public | `username` | Gửi reset OTP, lưu userId vào session, redirect `/reset-password` |
| Form đặt lại mật khẩu | GET | `/reset-password` | Public (cần session) | — | Hiển thị `reset-password.jsp` |
| Đặt lại mật khẩu | POST | `/reset-password` | Public | `otp`, `password`, `confirmPassword` | Cập nhật mật khẩu BCrypt, xóa OTP, giữ role USER |
| Google OAuth Login | GET | `/oauth2/authorization/google` | Public | — | Chuyển hướng sang Google Consent Screen |
| Google OAuth Callback | GET | `/login/oauth2/code/google` | Public | Google authorization code | Spring Security xử lý, chuyển hướng theo role |
| Đăng xuất | POST | `/logout` | Authenticated | CSRF token | Invalidate session, xóa cookie remember-me, redirect `/login?logout` |
| Trang chủ | GET | `/home` (hoặc `/`) | Authenticated | — | Hiển thị catalog sản phẩm active của Admin |
| Danh sách sản phẩm public | GET | `/products` | Authenticated | `keyword`, `categoryId`, `page=0`, `size=12` | Hiển thị catalog phân trang của Admin |
| Chi tiết sản phẩm public | GET | `/products/{id}` | Authenticated | path `{id}` | Hiển thị chi tiết (chỉ active product của Admin) |
| Trang cá nhân | GET | `/profile` | Authenticated | — | Hiển thị `profile.jsp` với thông tin user đăng nhập |
| Cập nhật cá nhân | POST | `/profile` | Authenticated | multipart form (`fullName`, `phone`, `avatar`) | Cập nhật thông tin (không đổi role/status/provider) |
| Xem ảnh tải lên | GET | `/image` | Public | `fname` | Trả về stream byte ảnh an toàn qua `UploadService` |
| Danh sách Category admin | GET | `/admin/categories` | ADMIN | `keyword`, `status`, `page=0`, `size=10` | Hiển thị `admin/category-list.jsp` |
| Form tạo Category | GET | `/admin/categories/create` | ADMIN | — | Hiển thị `admin/category-add.jsp` |
| Lưu Category mới | POST | `/admin/categories/create` | ADMIN | multipart `CategoryForm` | Owner luôn là Admin; redirect `/admin/categories` |
| Form sửa Category | GET | `/admin/categories/{id}/edit` | ADMIN | path `{id}` | Hiển thị `admin/category-edit.jsp` |
| Cập nhật Category | POST | `/admin/categories/{id}/edit` | ADMIN | multipart form | Cập nhật; redirect `/admin/categories` |
| Xóa Category | POST | `/admin/categories/{id}/delete` | ADMIN | path `{id}` + CSRF | Xóa an toàn (chặn nếu có Product); redirect list |
| Danh sách User admin | GET | `/admin/users` | ADMIN | `keyword`, `status`, `provider`, `page=0`, `size=10` | Hiển thị `admin/user-list.jsp` |
| Form tạo User | GET | `/admin/users/create` | ADMIN | — | Hiển thị `admin/user-add.jsp` |
| Lưu User mới | POST | `/admin/users/create` | ADMIN | form `UserAdminCreateForm` | Luôn tạo role `USER`, `ACTIVE`, encode BCrypt |
| Form sửa User | GET | `/admin/users/{id}/edit` | ADMIN | path `{id}` | Hiển thị `admin/user-edit.jsp` |
| Cập nhật User | POST | `/admin/users/{id}/edit` | ADMIN | form `UserAdminUpdateForm` | Không cho phép đổi thành role ADMIN |
| Khóa / Mở User | POST | `/admin/users/{id}/status` | ADMIN | path `{id}` + CSRF | Đổi `ACTIVE <-> BLOCKED`; chặn thao tác trên Admin |
| Xóa User | POST | `/admin/users/{id}/delete` | ADMIN | path `{id}` + CSRF | Xóa USER; chặn tuyệt đối không cho xóa Admin |
| API Category | GET/POST/PUT/DELETE | `/api/categories/**` | USER (GET) / ADMIN (All) | JSON / Query params | Giữ nguyên nếu có client ngoài gọi, trả `ApiResponse<T>` |
| API Product | GET/POST/PUT/DELETE | `/api/products/**` | USER (GET) / ADMIN (All) | JSON / Query params | Giữ nguyên nếu có client ngoài gọi, trả `ApiResponse<T>` |
| API User | GET/POST/PUT/PATCH/DELETE | `/api/users/**` | ADMIN | JSON / Query params | Quản trị user an toàn, trả `ApiResponse<T>` |

*Ghi chú*: Nếu hệ thống chỉ thuần JSP/MVC thì ưu tiên hoàn thiện toàn bộ controller MVC trước; các API controller chỉ phục vụ khi có yêu cầu gọi REST.

---

### 18.2 Cấu trúc mã nguồn chuẩn cho Spring Boot + JSP/JSTL

Để tránh tự đoán cấu trúc, toàn bộ dự án phải tuân theo sơ đồ:

```text
src/main/java/vn/iotstar/
├── WepBaitap02Application.java          # Kế thừa SpringBootServletInitializer
├── config/
│   ├── SecurityConfig.java              # Spring Security filter chain, CSRF, URL authorization
│   ├── WebMvcConfig.java                # View controller, static resources
│   └── AdminInitializer.java            # Khởi tạo ADMIN duy nhất từ biến môi trường
├── controller/
│   ├── AuthController.java              # /login, /register, /verify-otp, /forgot-password, /reset-password
│   ├── HomeController.java              # / và /home
│   ├── ProfileController.java           # /profile, /image
│   ├── PublicProductController.java     # /products, /products/{id}
│   └── admin/
│       ├── AdminCategoryController.java # /admin/categories/**
│       ├── AdminUserController.java     # /admin/users/**
│       └── AdminProductController.java  # /admin/products/**
├── repository/
│   ├── UserRepository.java              # Spring Data JPA UserRepository
│   ├── CategoryRepository.java          # Spring Data JPA CategoryRepository
│   └── ProductRepository.java           # Spring Data JPA ProductRepository
├── service/
│   ├── AuthService.java, UserService.java, CategoryService.java, ProductService.java
│   ├── OtpService.java, EmailService.java, UploadService.java
└── runner/
    └── PasswordMigrationRunner.java     # Hash BCrypt mật khẩu plaintext cũ có cờ bật tắt

src/main/resources/
├── application.properties               # Cấu hình Spring Boot, JPA, Mail, OAuth2, Admin env
└── application-test.properties          # Cấu hình cho môi trường test (H2/DB test, Mock mail)

src/main/webapp/
└── WEB-INF/views/
    ├── login.jsp, register.jsp, verify-otp.jsp, reset-password.jsp, home.jsp, profile.jsp
    ├── product-list.jsp, product-detail.jsp
    ├── admin/
    │   ├── category-list.jsp, category-add.jsp, category-edit.jsp
    │   ├── user-list.jsp, user-add.jsp, user-edit.jsp
    │   └── product-list.jsp, product-add.jsp, product-edit.jsp
    └── partials/                        # Thay thế SiteMesh
        ├── head.jspf, topbar.jspf, admin-sidebar.jspf, footer.jspf, messages.jspf
```

Cấu hình JSP View Resolver bắt buộc trong `application.properties`:
```properties
spring.mvc.view.prefix=/WEB-INF/views/
spring.mvc.view.suffix=.jsp
```

---

### 18.3 Trình tự 12 vòng TDD chuẩn (Vertical Slices)

#### Vòng 1: Bootstrap Spring Boot Shell & JSP/JSTL View Resolver (Tracer Bullet)
*Mục tiêu*: Khởi tạo ứng dụng Spring Boot packaging `war`, kiểm tra Spring context load và JSP resolver.

- **RED**:
  - Tạo `src/test/java/vn/iotstar/WepBaitap02ApplicationTests.java`.
  - Viết test `contextLoads()`.
  - *Kết quả*: FAIL vì chưa có class `WepBaitap02Application` và cấu hình Spring Boot.
- **GREEN**:
  - Cập nhật `pom.xml`: Khai báo parent Spring Boot (hoặc 3.4.x tương thích Java 21), packaging `war`, các dependency (`spring-boot-starter-web`, `tomcat-embed-jasper`, `jakarta.servlet.jsp.jstl`).
  - Tạo `WepBaitap02Application.java` (`extends SpringBootServletInitializer`).
  - Tạo `application.properties` cấu hình view prefix `/WEB-INF/views/`, suffix `.jsp`.
  - *Kết quả*: `mvn test` ➔ `contextLoads()` PASS.
- **REFACTOR**:
  - Dọn dẹp các dependency compile-time Servlet/JSP cũ khỏi POM.

---

#### Vòng 2: Migration dữ liệu Legacy (Owner, Role & Bất biến 1 Admin)
*Mục tiêu*: Chuyển toàn bộ dữ liệu Product/Category cũ về một Admin duy nhất, chuyển mọi user khác thành `USER`, áp dụng ràng buộc DB.

- **Các bước migration**:
  1. Chọn hoặc tạo tài khoản ADMIN duy nhất từ cấu hình (`AdminInitializer`).
  2. Chuyển `owner_id` của toàn bộ Product cũ sang ADMIN.
  3. Chuyển `owner_id` của Category sang ADMIN.
  4. Chuyển toàn bộ tài khoản còn lại thành role `USER`.
  5. Không thay đổi OTP, provider, email, trạng thái kích hoạt.
  6. Kiểm tra không còn Product/Category thuộc USER.
  7. Áp dụng unique filtered index SQL Server: `CREATE UNIQUE INDEX uk_users_single_admin ON dbo.users(role) WHERE role = 'ADMIN'`.
- **Cấu hình biến môi trường Admin**:
  ```properties
  app.admin.username=${APP_ADMIN_USERNAME:admin}
  app.admin.email=${APP_ADMIN_EMAIL:admin@iotstar.vn}
  app.admin.password=${APP_ADMIN_PASSWORD}
  ```
- **RED**:
  - Tạo `LegacyDataMigrationTest.java`:
    + `allLegacyProducts_ShouldBelongToSingleAdmin()`
    + `allNonAdminAccounts_ShouldHaveRoleUser()`
    + `migration_ShouldNotChangeOtpOrProvider()`
    + `migration_ShouldBeIdempotent()`
    + `secondAdminInsert_ShouldBeRejected()`
    + `applicationStartup_WhenAdminMissing_ShouldCreateConfiguredAdmin()`
  - *Kết quả*: FAIL vì chưa có migration script và `AdminInitializer`.
- **GREEN**:
  - Viết SQL script `sql/05-spring-boot-4-role-auth-migration.sql`.
  - Tạo `AdminInitializer.java`: Khi khởi động, nếu DB chưa có admin thì đọc từ biến môi trường và tạo; nếu đã có 1 admin thì bỏ qua; nếu có nhiều hơn 1 admin cũ thì ném lỗi dừng ứng dụng để xử lý dữ liệu.
  - *Kết quả*: Test PASS.
- **REFACTOR**:
  - Đảm bảo script migration có thể chạy nhiều lần mà không sinh lỗi (idempotent).

---

#### Vòng 3: Entity & Repository (Spring Data JPA với Search & Paging)
*Mục tiêu*: Xây dựng Entity chuẩn và Repository truy vấn phân trang tại DB.

- **Đặc tả tìm kiếm & phân trang**:
  - Category: `keyword`, `status`, `page`, `size` (tìm chứa từ khóa không phân biệt hoa thường theo tên).
  - User: `keyword`, `status`, `provider`, `page`, `size` (tìm theo username, họ tên, email hoặc số điện thoại).
  - Product: `keyword`, `categoryId`, `page`, `size` (tìm theo tên/mô tả nhưng luôn giới hạn `category.owner.role = 'ADMIN'` và `status = 1`).
- **RED**:
  - Tạo `UserRepositoryTest.java`, `CategoryRepositoryTest.java`, `ProductRepositoryTest.java` (dùng `@DataJpaTest`):
    + `searchCategory_ShouldIgnoreCase()`
    + `searchUser_ShouldMatchUsernameNameEmailOrPhone()`
    + `searchUser_ShouldNotReturnAdminAccount()`
    + `publicSearch_ShouldOnlyReturnActiveAdminProducts()`
  - *Kết quả*: FAIL vì chưa có interface Spring Data JPA.
- **GREEN**:
  - Cập nhật các Entity: `User.java`, `Category.java`, `Product.java`.
  - Tạo interface `UserRepository`, `CategoryRepository`, `ProductRepository` kế thừa `JpaRepository` và `JpaSpecificationExecutor`.
  - *Kết quả*: Test PASS.
- **REFACTOR**:
  - Chuẩn hóa sort mặc định trong `Pageable` (`createdDate DESC, id DESC`).

---

#### Vòng 4: Spring Security & Phân quyền URL
*Mục tiêu*: Thiết lập Security Filter Chain, bật CSRF, chặn truy cập trái phép, phân biệt phản hồi HTML vs JSON.

- **RED**:
  - Tạo `SecurityFilterChainTest.java` (dùng MockMvc):
    + `anonymousAccessProtectedWeb_ShouldRedirectToLogin()`: Truy cập `/home` khi chưa login ➔ 302 về `/login`.
    + `anonymousAccessProtectedApi_ShouldReturn401Json()`: Gọi `/api/categories` khi chưa login ➔ 401 JSON kèm `ApiResponse`.
    + `userAccessAdminUrl_ShouldReturn403()`: User thường vào `/admin/**` ➔ 403.
    + `adminAccessAdminUrl_ShouldReturn200()`: Admin vào `/admin/**` ➔ 200.
    + `postLogoutWithoutCsrf_ShouldReturn403()`: Logout POST không CSRF ➔ 403.
  - *Kết quả*: FAIL vì chưa cấu hình `SecurityConfig`.
- **GREEN**:
  - Viết `CustomUserDetailsService.java` tải user theo username, map role `ROLE_ADMIN` / `ROLE_USER`.
  - Viết `SecurityConfig.java`: Cấu hình permitAll, hasRole('ADMIN'), CSRF cho JSP form, Remember-me, Form Login.
  - Tạo custom `AuthenticationEntryPoint` và `AccessDeniedHandler` trả JSON cho `/api/**` và redirect cho web.
  - *Kết quả*: Test PASS.
- **REFACTOR**:
  - Bật `@EnableMethodSecurity` cho các Service method nhạy cảm.

---

#### Vòng 5: Xác thực Local, Đăng ký & Chu trình OTP
*Mục tiêu*: Quản lý đăng ký tài khoản local, xác thực kích hoạt qua OTP, quên mật khẩu và đặt lại mật khẩu an toàn.

- **RED**:
  - Tạo `AuthServiceTest.java` và `AuthControllerTest.java` (MockMvc):
    + `register_AlwaysAssignRoleUserAndStatusPending()`
    + `activate_ValidOtp_ShouldActivateAccountAndPreserveRoleUser()`
    + `activate_InvalidOrExpiredOtp_ShouldFail()`
    + `requestPasswordReset_ShouldGenerateResetOtp()`
    + `resetPassword_ValidOtp_ShouldUpdateBcryptPasswordAndPreserveRole()`
  - *Kết quả*: FAIL vì chưa có `AuthService`, `OtpService`, `EmailService`.
- **GREEN**:
  - Viết `OtpService` (sinh mã 6 số ngẫu nhiên an toàn, hết hạn 5 phút).
  - Viết `EmailService` (dùng `JavaMailSender`, gửi mail kích hoạt/reset).
  - Viết `AuthService` triển khai đầy đủ các bước nghiệp vụ.
  - Viết `AuthController` ánh xạ các endpoint GET/POST theo bảng mục 18.1.
  - *Kết quả*: Test PASS.
- **REFACTOR**:
  - Đảm bảo trong mọi trường hợp (kể cả client cố gửi field `role`), role của user luôn cố định là `USER`.

---

#### Vòng 6: Google OAuth2 & Chống cướp quyền
*Mục tiêu*: Tích hợp đăng nhập Google an toàn, ngăn chặn việc chiếm quyền tài khoản Admin hoặc ghi đè user local chưa xác minh.

- **Quy tắc xử lý tài khoản Google**:
  1. Trùng email admin local ➔ Từ chối Google login với thông báo lỗi rõ ràng.
  2. Trùng USER đã đăng ký local ➔ Không tự động chiếm/link tài khoản nếu chưa xác minh.
  3. USER đã liên kết Google ➔ Cho phép đăng nhập bình thường.
  4. User có trạng thái `BLOCKED` trong DB ➔ Bị từ chối đăng nhập.
  5. Google user mới ➔ Tự động tạo tài khoản với `role = USER`, `status = ACTIVE`, `provider = GOOGLE`.
  6. OAuth tuyệt đối không đọc `role` từ request hoặc Google claims.
- **RED**:
  - Tạo `CustomOAuth2UserServiceTest.java`:
    + `googleLogin_NewUser_ShouldCreateUserRoleAndActive()`
    + `googleLogin_MatchingLocalAdminEmail_ShouldReject()`
    + `googleLogin_MatchingLocalUserEmail_ShouldNotAutoOverride()`
    + `googleLogin_BlockedUser_ShouldReject()`
  - *Kết quả*: FAIL do chưa có OAuth2 config và service.
- **GREEN**:
  - Cấu hình OAuth2 Client trong `application.properties`.
  - Viết `CustomOAuth2UserService.java` kế thừa `DefaultOAuth2UserService` cài đặt các quy tắc trên.
  - Bổ sung nút đăng nhập Google trong `login.jsp`.
  - *Kết quả*: Test PASS.
- **REFACTOR**:
  - Xử lý va chạm tên người dùng (username collision) khi sinh username tự động từ email Google.

---

#### Vòng 7: Quản trị Danh mục (Category CRUD & Search)
*Mục tiêu*: Hoàn thiện toàn bộ chức năng quản trị Category dành riêng cho Admin, hỗ trợ tìm kiếm phân trang.

- **RED**:
  - Tạo `AdminCategoryControllerTest.java` (MockMvc):
    + `listCategory_Admin_ShouldReturnPagedResult()`
    + `searchCategory_ShouldFilterByKeyword()`
    + `createCategory_ValidForm_ShouldPersistWithAdminOwner()`
    + `createCategory_DuplicateName_ShouldReturnValidationError()`
    + `editCategory_ValidForm_ShouldUpdate()`
    + `deleteCategory_WithoutProducts_ShouldDelete()`
    + `deleteCategory_WithProducts_ShouldReject()`
    + `userAccessAdminCategory_ShouldReturn403()`
    + `getDeleteCategory_ShouldReturn405Or404()` (chặn xóa bằng method GET cũ)
  - *Kết quả*: FAIL.
- **GREEN**:
  - Viết `CategoryService` (CRUD, validate trùng tên, gán owner là Admin).
  - Viết `AdminCategoryController` phục vụ giao diện JSP (`/admin/categories/**`).
  - Cập nhật view JSP Category: Đổi link xóa thành form `POST` kèm CSRF.
  - *Kết quả*: Test PASS.
- **REFACTOR**:
  - Dọn dẹp `CategoryController` Servlet cũ và `ICategoryDao`.

---

#### Vòng 8: Quản trị Người dùng (User Management - Admin)
*Mục tiêu*: Cho phép Admin tìm kiếm, xem danh sách, thêm, sửa, khóa/mở và xóa user; bảo vệ Admin duy nhất.

- **RED**:
  - Tạo `AdminUserControllerTest.java` (MockMvc):
    + `listUsers_ShouldExcludeOrProtectAdmin()`
    + `searchUsers_ShouldReturnPagedResult()`
    + `createUser_ShouldAlwaysAssignRoleUser()`
    + `editUser_ShouldNotAllowRoleEscalation()`
    + `blockUser_ShouldInvalidateFutureLogin()`
    + `deleteUser_ShouldRejectAdmin()` (Admin không thể tự xóa chính mình)
    + `userAccessAdminUsers_ShouldReturn403()`
  - *Kết quả*: FAIL.
- **GREEN**:
  - Viết `UserService` (tìm kiếm đa tiêu chí, tạo user role USER, chặn khóa/xóa Admin).
  - Viết `AdminUserController` phục vụ giao diện JSP (`/admin/users/**`).
  - Tạo các view `user-list.jsp`, `user-add.jsp`, `user-edit.jsp`.
  - *Kết quả*: Test PASS.
- **REFACTOR**:
  - Đảm bảo form sửa không hiển thị input chọn role để tránh tấn công mass-assignment.

---

#### Vòng 9: Quản lý Sản phẩm (Product Management & Public Catalog)
*Mục tiêu*: Tách biệt hoàn toàn luồng quản trị sản phẩm của Admin và luồng xem danh mục của User/Khách.

- **RED**:
  - Tạo `ProductServiceTest.java` và `PublicProductControllerTest.java` (MockMvc):
    + `publicProductCatalog_ShouldOnlyShowActiveAdminProducts()`
    + `publicProductCatalog_DifferentUsers_SeeIdenticalCatalog()`
    + `productDetail_WhenInactive_ShouldReturn404()`
    + `adminProductCrud_FullWorkflow_Success()`
    + `userAttemptToMutateProduct_ShouldReturn403()`
  - *Kết quả*: FAIL.
- **GREEN**:
  - Viết `ProductService` tách biệt query admin và query public active.
  - Viết `AdminProductController` cho Admin quản lý sản phẩm.
  - Viết `PublicProductController` cho người dùng xem danh mục/chi tiết.
  - Sửa `home.jsp`, `product-list.jsp`, `product-detail.jsp` hiển thị catalog của Admin.
  - *Kết quả*: Test PASS.
- **REFACTOR**:
  - Loại bỏ hoàn toàn điều kiện `owner_id = current_user_id` trong toàn bộ nghiệp vụ Product.

---

#### Vòng 10: Password Migration Runner (Plaintext sang BCrypt)
*Mục tiêu*: Mã hóa mật khẩu plaintext cũ sang BCrypt với cờ bật/tắt an toàn.

- **Cấu hình cờ migration**:
  ```properties
  app.migration.password.enabled=${PASSWORD_MIGRATION_ENABLED:false}
  ```
- **RED**:
  - Tạo `PasswordMigrationRunnerTest.java`:
    + `plaintextPassword_ShouldBeConvertedToBcrypt()`
    + `existingBcryptPassword_ShouldRemainUnchanged()`
    + `whenMigrationDisabled_ShouldNotRun()`
  - *Kết quả*: FAIL.
- **GREEN**:
  - Tạo `PasswordMigrationRunner.java` (`CommandLineRunner` hoặc `ApplicationRunner`).
  - Kiểm tra `app.migration.password.enabled == true`. Lọc user local có password không khớp prefix BCrypt (`$2a$`, `$2b$`, `$2y$`) để hash và lưu.
  - Chạy thử nghiệm thành công ➔ Chuyển cờ về `false`.
  - *Kết quả*: Test PASS.
- **REFACTOR**:
  - Đảm bảo tính idempotent tuyệt đối (chạy nhiều lần không làm hỏng mật khẩu đã hash).

---

#### Vòng 11: Dọn dẹp Legacy Code & Static Verification
*Mục tiêu*: Xóa bỏ toàn bộ tàn dư Servlet/Filter/DAO/SiteMesh cũ và xác nhận không còn sót mã lỗi thời.

- **RED / STATIC CHECK**:
  - Chạy lệnh kiểm tra tĩnh:
    ```bash
    rg "@WebServlet|@WebFilter|@WebListener|extends HttpServlet|JpaConfig|sessionScope.account" src/main
    ```
  - *Kết quả mong đợi*: Không tìm thấy bất kỳ kết quả nào trong `src/main/java`.
- **GREEN**:
  - Xóa danh sách file legacy:
    + `vn/iotstar/config/JpaConfig.java`, `JpaLifecycleListener.java`
    + `META-INF/persistence.xml`
    + Toàn bộ package `vn/iotstar/dao/**`
    + `vn/iotstar/filter/AuthorizationFilter.java`, `EncodingFilter.java`, `SiteMeshFilter.java`
    + `vn/iotstar/controller/WaitingController.java`
    + `vn/iotstar/util/AuthUtil.java`, `JsonUtil.java`
    + `WEB-INF/decorators/**`
  - Thay thế SiteMesh bằng các fragment JSP trong `WEB-INF/views/partials/`.
  - *Kết quả*: `mvn clean package` build WAR thành công.
- **REFACTOR**:
  - Dọn dẹp các import không dùng và tối ưu comment trong toàn bộ codebase.

---

#### Vòng 12: Acceptance Testing End-to-End & Regression Test
*Mục tiêu*: Kiểm thử nghiệm thu tổng thể hệ thống với SQL Server thật và bộ 3 tài khoản chuẩn.

- **Bộ tài khoản nghiệm thu**:
  1. `admin`: Role ADMIN, trạng thái ACTIVE.
  2. `localuser`: Role USER, tạo qua form đăng ký + xác thực OTP.
  3. `googleuser`: Role USER, tạo tự động qua Google OAuth.
- **Kịch bản kiểm thử nghiệm thu (Manual + Automated)**:
  1. Admin đăng nhập ➔ Thấy menu Category, Product, User; CRUD và tìm kiếm phân trang hoạt động chuẩn xác.
  2. Local user đăng nhập ➔ Không thấy menu Admin; cố nhập URL `/admin/**` nhận 403 Forbidden.
  3. Google user đăng nhập ➔ Nhìn thấy catalog sản phẩm giống hệt Local user.
  4. Admin tạo Category/Product ➔ Cả 2 user đều nhìn thấy khi `status = 1`.
  5. Admin đổi status Category/Product sang inactive ➔ User không còn thấy.
  6. Quên mật khẩu localuser ➔ Nhận mã OTP, đổi mật khẩu thành công, role vẫn là USER.
  7. Kiểm tra DB: Chỉ có đúng 1 record `role = 'ADMIN'`; 100% mật khẩu local là chuỗi mã hóa BCrypt.
- **Kết quả nghiệm thu**: Toàn bộ automated test suites pass 100% (`mvn clean test`).

---

### 18.4 Bảng theo dõi tiến độ TDD (Checklist thực thi 12 vòng)

| Vòng | Lát cắt nghiệp vụ | RED (Test viết trước) | GREEN (Code tối thiểu) | REFACTOR (Tối ưu) | Trạng thái |
|:---:|:---|:---:|:---:|:---:|:---:|
| **1** | Bootstrap Spring Boot Shell & JSP Resolver | [ ] | [ ] | [ ] | Chưa bắt đầu |
| **2** | Migration Dữ liệu Legacy & Bất biến 1 Admin | [ ] | [ ] | [ ] | Chưa bắt đầu |
| **3** | Entity & Repository (Search + Paging) | [ ] | [ ] | [ ] | Chưa bắt đầu |
| **4** | Spring Security & Phân quyền URL | [ ] | [ ] | [ ] | Chưa bắt đầu |
| **5** | Xác thực Local & Chu trình OTP | [ ] | [ ] | [ ] | Chưa bắt đầu |
| **6** | Google OAuth2 & Chống cướp quyền | [ ] | [ ] | [ ] | Chưa bắt đầu |
| **7** | Quản trị Danh mục (Category CRUD & Search) | [ ] | [ ] | [ ] | Chưa bắt đầu |
| **8** | Quản trị Người dùng (Admin User Management) | [ ] | [ ] | [ ] | Chưa bắt đầu |
| **9** | Quản lý Sản phẩm (Product Admin / Public) | [ ] | [ ] | [ ] | Chưa bắt đầu |
| **10**| Password Migration Runner (BCrypt flag) | [ ] | [ ] | [ ] | Chưa bắt đầu |
| **11**| Dọn dẹp Legacy Code & Static Check | [ ] | [ ] | [ ] | Chưa bắt đầu |
| **12**| Acceptance Testing End-to-End | [ ] | [ ] | [ ] | Chưa bắt đầu |

