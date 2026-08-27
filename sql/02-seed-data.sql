USE jakartaJPA;
GO

IF OBJECT_ID(N'dbo.users', N'U') IS NULL OR OBJECT_ID(N'dbo.categories', N'U') IS NULL
    THROW 50001, N'Hãy chạy ứng dụng một lần để Hibernate tạo bảng trước khi seed.', 1;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = N'admin')
    INSERT INTO dbo.users (email, username, full_name, password, avatar, role_id, phone, created_date)
    VALUES (N'admin@iotstar.vn', N'admin', N'Quản trị viên', N'123456', NULL, 1, N'0900000001', SYSDATETIME());

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = N'manager')
    INSERT INTO dbo.users (email, username, full_name, password, avatar, role_id, phone, created_date)
    VALUES (N'manager@iotstar.vn', N'manager', N'Quản lý', N'123456', NULL, 2, N'0900000002', SYSDATETIME());

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = N'member')
    INSERT INTO dbo.users (email, username, full_name, password, avatar, role_id, phone, created_date)
    VALUES (N'member@iotstar.vn', N'member', N'Người dùng', N'123456', NULL, 3, N'0900000003', SYSDATETIME());
GO

IF NOT EXISTS (SELECT 1 FROM dbo.categories WHERE category_name = N'Điện thoại')
    INSERT INTO dbo.categories (category_name, images, status)
    VALUES (N'Điện thoại', N'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800', 1);

IF NOT EXISTS (SELECT 1 FROM dbo.categories WHERE category_name = N'Máy tính')
    INSERT INTO dbo.categories (category_name, images, status)
    VALUES (N'Máy tính', N'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800', 1);

IF NOT EXISTS (SELECT 1 FROM dbo.categories WHERE category_name = N'Phụ kiện')
    INSERT INTO dbo.categories (category_name, images, status)
    VALUES (N'Phụ kiện', NULL, 0);
GO

PRINT N'Đã seed tài khoản và danh mục mẫu.';
GO
