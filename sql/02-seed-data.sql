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

IF OBJECT_ID(N'dbo.users', N'U') IS NULL OR OBJECT_ID(N'dbo.categories', N'U') IS NULL
    THROW 50001, N'Hãy chạy ứng dụng một lần để Hibernate tạo bảng trước khi seed.', 1;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = N'user1')
    INSERT INTO dbo.users (email, username, full_name, password, avatar, role_id, phone, created_date)
    VALUES (N'user1@iotstar.vn', N'user1', N'Người dùng 1', N'123456', NULL, 3, N'0900000011', SYSDATETIME());

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = N'user2')
    INSERT INTO dbo.users (email, username, full_name, password, avatar, role_id, phone, created_date)
    VALUES (N'user2@iotstar.vn', N'user2', N'Người dùng 2', N'123456', NULL, 3, N'0900000012', SYSDATETIME());
GO

DECLARE @user1Id INT = (SELECT TOP 1 id FROM dbo.users WHERE username = N'user1');
DECLARE @user2Id INT = (SELECT TOP 1 id FROM dbo.users WHERE username = N'user2');

-- Danh mục riêng của user1
IF @user1Id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.categories WHERE category_name = N'Điện thoại' AND user_id = @user1Id)
    INSERT INTO dbo.categories (category_name, images, status, user_id)
    VALUES (N'Điện thoại', N'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800', 1, @user1Id);

IF @user1Id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.categories WHERE category_name = N'Máy tính' AND user_id = @user1Id)
    INSERT INTO dbo.categories (category_name, images, status, user_id)
    VALUES (N'Máy tính', N'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800', 1, @user1Id);

-- Danh mục riêng của user2
IF @user2Id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.categories WHERE category_name = N'Thời trang & Phụ kiện' AND user_id = @user2Id)
    INSERT INTO dbo.categories (category_name, images, status, user_id)
    VALUES (N'Thời trang & Phụ kiện', N'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800', 1, @user2Id);
GO

PRINT N'Đã seed tài khoản user1, user2 và danh mục mẫu riêng cho từng người dùng.';
GO
