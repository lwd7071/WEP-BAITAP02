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
    THROW 50002, N'Hãy chạy ứng dụng và tạo bảng users/categories trước khi migration.', 1;
GO

IF COL_LENGTH(N'dbo.categories', N'user_id') IS NULL
BEGIN
    EXEC(N'ALTER TABLE dbo.categories ADD user_id INT NULL;');
END;
GO

DECLARE @defaultUserId INT = (SELECT TOP 1 id FROM dbo.users WHERE username IN (N'user1', N'admin') ORDER BY id);
IF @defaultUserId IS NULL
    THROW 50003, N'Không tìm thấy tài khoản (user1/admin) để gán Category cũ.', 1;

UPDATE dbo.categories
SET user_id = @defaultUserId
WHERE user_id IS NULL;
GO

ALTER TABLE dbo.categories ALTER COLUMN user_id INT NOT NULL;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = N'fk_categories_user'
      AND parent_object_id = OBJECT_ID(N'dbo.categories')
)
BEGIN
    ALTER TABLE dbo.categories
        ADD CONSTRAINT fk_categories_user
        FOREIGN KEY (user_id) REFERENCES dbo.users(id);
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'uk_categories_owner_name'
      AND object_id = OBJECT_ID(N'dbo.categories')
)
BEGIN
    CREATE UNIQUE INDEX uk_categories_owner_name
        ON dbo.categories(user_id, category_name);
END;
GO

PRINT N'Đã bổ sung owner cho Category và gán dữ liệu cũ cho admin.';
GO
