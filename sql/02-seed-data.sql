USE jakartaJPA;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
GO

IF OBJECT_ID(N'dbo.users', N'U') IS NULL OR OBJECT_ID(N'dbo.categories', N'U') IS NULL OR OBJECT_ID(N'dbo.products', N'U') IS NULL
    THROW 50001, N'Hãy chạy ứng dụng một lần để Hibernate tạo bảng trước khi seed.', 1;
GO

-- 1. Đảm bảo các tài khoản cơ bản tồn tại và đang hoạt động (status = 1)
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = N'admin')
    INSERT INTO dbo.users (email, username, full_name, password, avatar, role_id, phone, created_date, status)
    VALUES (N'admin@iotstar.vn', N'admin', N'Quản trị viên Admin', N'123456', NULL, 1, N'0900000001', SYSDATETIME(), 1);
ELSE
    UPDATE dbo.users SET status = 1, password = N'123456' WHERE username = N'admin';

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = N'manager')
    INSERT INTO dbo.users (email, username, full_name, password, avatar, role_id, phone, created_date, status)
    VALUES (N'manager@iotstar.vn', N'manager', N'Quản lý Manager', N'123456', NULL, 2, N'0900000002', SYSDATETIME(), 1);
ELSE
    UPDATE dbo.users SET status = 1, password = N'123456' WHERE username = N'manager';

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = N'member')
    INSERT INTO dbo.users (email, username, full_name, password, avatar, role_id, phone, created_date, status)
    VALUES (N'member@iotstar.vn', N'member', N'Khách hàng Member', N'123456', NULL, 3, N'0900000003', SYSDATETIME(), 1);
ELSE
    UPDATE dbo.users SET status = 1, password = N'123456' WHERE username = N'member';
GO

-- 2. Tạo danh mục mẫu chuẩn E-commerce cho tài khoản Admin
DECLARE @adminId INT = (SELECT TOP 1 id FROM dbo.users WHERE username = N'admin');

IF @adminId IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM dbo.categories WHERE category_name = N'Điện thoại & Tablet' AND user_id = @adminId)
        INSERT INTO dbo.categories (category_name, images, status, user_id)
        VALUES (N'Điện thoại & Tablet', N'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800', 1, @adminId);

    IF NOT EXISTS (SELECT 1 FROM dbo.categories WHERE category_name = N'Laptop & Máy tính' AND user_id = @adminId)
        INSERT INTO dbo.categories (category_name, images, status, user_id)
        VALUES (N'Laptop & Máy tính', N'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800', 1, @adminId);

    IF NOT EXISTS (SELECT 1 FROM dbo.categories WHERE category_name = N'Tai nghe & Âm thanh' AND user_id = @adminId)
        INSERT INTO dbo.categories (category_name, images, status, user_id)
        VALUES (N'Tai nghe & Âm thanh', N'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800', 1, @adminId);

    IF NOT EXISTS (SELECT 1 FROM dbo.categories WHERE category_name = N'Đồng hồ & Phụ kiện' AND user_id = @adminId)
        INSERT INTO dbo.categories (category_name, images, status, user_id)
        VALUES (N'Đồng hồ & Phụ kiện', N'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800', 1, @adminId);
END
GO

-- 3. Seed 16 sản phẩm đa dạng cho Admin test layout, card, filter, stepper
DECLARE @adminId INT = (SELECT TOP 1 id FROM dbo.users WHERE username = N'admin');
DECLARE @catPhone INT = (SELECT TOP 1 category_id FROM dbo.categories WHERE category_name = N'Điện thoại & Tablet' AND user_id = @adminId);
DECLARE @catLaptop INT = (SELECT TOP 1 category_id FROM dbo.categories WHERE category_name = N'Laptop & Máy tính' AND user_id = @adminId);
DECLARE @catAudio INT = (SELECT TOP 1 category_id FROM dbo.categories WHERE category_name = N'Tai nghe & Âm thanh' AND user_id = @adminId);
DECLARE @catWatch INT = (SELECT TOP 1 category_id FROM dbo.categories WHERE category_name = N'Đồng hồ & Phụ kiện' AND user_id = @adminId);

IF @catPhone IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'iPhone 16 Pro Max 256GB')
        INSERT INTO dbo.products (product_name, unit_price, quantity, description, images, status, created_date, category_id)
        VALUES (N'iPhone 16 Pro Max 256GB', 34990000, 25, 
                N'Thiết kế khung viền Titanium sang trọng, chip A18 Pro mạnh mẽ, camera điều khiển chuyên nghiệp thế hệ mới.', 
                N'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=800', 1, SYSDATETIME(), @catPhone);

    IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'Samsung Galaxy S24 Ultra 512GB')
        INSERT INTO dbo.products (product_name, unit_price, quantity, description, images, status, created_date, category_id)
        VALUES (N'Samsung Galaxy S24 Ultra 512GB', 29990000, 18, 
                N'Màn hình Dynamic AMOLED 2X sắc nét, tích hợp quyền năng Galaxy AI vượt trội và bút S-Pen đỉnh cao.', 
                N'https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=800', 1, DATEADD(minute, -10, SYSDATETIME()), @catPhone);

    IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'iPad Pro M4 11 inch Wi-Fi 256GB')
        INSERT INTO dbo.products (product_name, unit_price, quantity, description, images, status, created_date, category_id)
        VALUES (N'iPad Pro M4 11 inch Wi-Fi 256GB', 27490000, 15, 
                N'Đỉnh cao máy tính bảng với màn hình Ultra Retina XDR Tandem OLED, chip Apple M4 siêu mỏng nhẹ.', 
                N'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800', 1, DATEADD(minute, -20, SYSDATETIME()), @catPhone);

    IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'Xiaomi 14 Ultra Leica Camera')
        INSERT INTO dbo.products (product_name, unit_price, quantity, description, images, status, created_date, category_id)
        VALUES (N'Xiaomi 14 Ultra Leica Camera', 23990000, 20, 
                N'Ống kính quang học Leica Summilux đỉnh cao nhiếp ảnh di động, Snapdragon 8 Gen 3 và sạc siêu nhanh 90W.', 
                N'https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=800', 1, DATEADD(minute, -30, SYSDATETIME()), @catPhone);
END

IF @catLaptop IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'MacBook Pro 14 M3 Pro 18GB 512GB')
        INSERT INTO dbo.products (product_name, unit_price, quantity, description, images, status, created_date, category_id)
        VALUES (N'MacBook Pro 14 M3 Pro 18GB 512GB', 48990000, 12, 
                N'Hiệu năng đồ họa vượt bậc, thời lượng pin lên đến 22 giờ, màn hình Liquid Retina XDR màu Space Black ấn tượng.', 
                N'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800', 1, DATEADD(minute, -40, SYSDATETIME()), @catLaptop);

    IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'Dell XPS 13 Plus OLED Core Ultra')
        INSERT INTO dbo.products (product_name, unit_price, quantity, description, images, status, created_date, category_id)
        VALUES (N'Dell XPS 13 Plus OLED Core Ultra', 39500000, 10, 
                N'Tuyệt tác ultrabook mỏng nhẹ với bàn phím liền mạch chạm cảm ứng, màn hình OLED InfinityEdge 3.5K cực rực rỡ.', 
                N'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=800', 1, DATEADD(minute, -50, SYSDATETIME()), @catLaptop);

    IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'ASUS ROG Zephyrus G14 RTX 4070')
        INSERT INTO dbo.products (product_name, unit_price, quantity, description, images, status, created_date, category_id)
        VALUES (N'ASUS ROG Zephyrus G14 RTX 4070', 42000000, 8, 
                N'Laptop gaming cao cấp vỏ nhôm CNC nguyên khối, card đồ họa RTX 4070 và màn hình ROG Nebula OLED 120Hz.', 
                N'https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=800', 1, DATEADD(minute, -60, SYSDATETIME()), @catLaptop);

    IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'Lenovo ThinkPad X1 Carbon Gen 12')
        INSERT INTO dbo.products (product_name, unit_price, quantity, description, images, status, created_date, category_id)
        VALUES (N'Lenovo ThinkPad X1 Carbon Gen 12', 36800000, 14, 
                N'Chuẩn mực laptop doanh nhân bền bỉ chuẩn quân đội, bàn phím gõ êm ái bậc nhất và bảo mật vân tay sinh trắc học.', 
                N'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=800', 1, DATEADD(minute, -70, SYSDATETIME()), @catLaptop);
END

IF @catAudio IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'Sony WH-1000XM5 Chống Ồn Cao Cấp')
        INSERT INTO dbo.products (product_name, unit_price, quantity, description, images, status, created_date, category_id)
        VALUES (N'Sony WH-1000XM5 Chống Ồn Cao Cấp', 7490000, 35, 
                N'Khử tiếng ồn hàng đầu thế giới với bộ xử lý V1 + QN1, chất âm Hi-Res Audio không dây và micro đàm thoại rõ nét.', 
                N'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=800', 1, DATEADD(minute, -80, SYSDATETIME()), @catAudio);

    IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'Apple AirPods Pro 2 USB-C MagSafe')
        INSERT INTO dbo.products (product_name, unit_price, quantity, description, images, status, created_date, category_id)
        VALUES (N'Apple AirPods Pro 2 USB-C MagSafe', 5390000, 50, 
                N'Chống ồn chủ động gấp 2 lần, âm thanh thích ứng theo môi trường xung quanh và cổng sạc Type-C tiện lợi.', 
                N'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=800', 1, DATEADD(minute, -90, SYSDATETIME()), @catAudio);

    IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'Loa Bluetooth Marshall Stanmore III')
        INSERT INTO dbo.products (product_name, unit_price, quantity, description, images, status, created_date, category_id)
        VALUES (N'Loa Bluetooth Marshall Stanmore III', 9200000, 16, 
                N'Âm trường rộng mở sống động, thiết kế biểu tượng phong cách vintage cổ điển đậm chất rock n roll.', 
                N'https://images.unsplash.com/photo-1545454675-3531b543be5d?w=800', 1, DATEADD(minute, -100, SYSDATETIME()), @catAudio);

    IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'Bose QuietComfort Ultra Headphones')
        INSERT INTO dbo.products (product_name, unit_price, quantity, description, images, status, created_date, category_id)
        VALUES (N'Bose QuietComfort Ultra Headphones', 8990000, 15, 
                N'Công nghệ âm thanh không gian Bose Immersive Audio sống động, đệm tai êm ái sang trọng cho chuyến bay dài.', 
                N'https://images.unsplash.com/photo-1583394838336-acd977736f90?w=800', 1, DATEADD(minute, -110, SYSDATETIME()), @catAudio);
END

IF @catWatch IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'Apple Watch Ultra 2 Titanium 49mm')
        INSERT INTO dbo.products (product_name, unit_price, quantity, description, images, status, created_date, category_id)
        VALUES (N'Apple Watch Ultra 2 Titanium 49mm', 19890000, 20, 
                N'Vỏ titan siêu nhẹ chịu lực, GPS tần số kép chuẩn xác, màn hình sáng 3000 nits dành riêng cho dân thể thao chuyên nghiệp.', 
                N'https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=800', 1, DATEADD(minute, -120, SYSDATETIME()), @catWatch);

    IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'Garmin Fenix 7 Pro Sapphire Solar')
        INSERT INTO dbo.products (product_name, unit_price, quantity, description, images, status, created_date, category_id)
        VALUES (N'Garmin Fenix 7 Pro Sapphire Solar', 18490000, 12, 
                N'Đồng hồ thể thao thông minh sạc bằng năng lượng mặt trời, tích hợp bản đồ địa hình TOPO và đèn pin LED trợ lực.', 
                N'https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=800', 1, DATEADD(minute, -130, SYSDATETIME()), @catWatch);

    IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'Củ Sạc Nhanh Anker Prime 100W GaN')
        INSERT INTO dbo.products (product_name, unit_price, quantity, description, images, status, created_date, category_id)
        VALUES (N'Củ Sạc Nhanh Anker Prime 100W GaN', 1450000, 80, 
                N'Công nghệ GaNPrime thế hệ mới siêu nhỏ gọn, trang bị 3 cổng sạc đồng thời công suất cao cho Laptop và iPhone.', 
                N'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=800', 1, DATEADD(minute, -140, SYSDATETIME()), @catWatch);

    IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'Bàn Phím Cơ Keychron Q1 Pro RGB')
        INSERT INTO dbo.products (product_name, unit_price, quantity, description, images, status, created_date, category_id)
        VALUES (N'Bàn Phím Cơ Keychron Q1 Pro RGB', 4250000, 25, 
                N'Vỏ nhôm CNC nguyên khối thiết kế Double Gasket, kết nối Bluetooth 5.1 đa thiết bị, switch cơ học gõ cực đầm tay.', 
                N'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800', 1, DATEADD(minute, -150, SYSDATETIME()), @catWatch);
END
GO

PRINT N'✅ Đã seed thành công 4 danh mục và 16 sản phẩm đa dạng cho tài khoản admin!';
GO
