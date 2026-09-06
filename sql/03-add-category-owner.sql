USE jakartaJPA;
GO

IF OBJECT_ID(N'dbo.users', N'U') IS NULL OR OBJECT_ID(N'dbo.categories', N'U') IS NULL
    THROW 50002, N'Hãy chạy ứng dụng và tạo bảng users/categories trước khi migration.', 1;
GO

DECLARE @adminId INT = (SELECT TOP 1 id FROM dbo.users WHERE username = N'admin');
IF @adminId IS NULL
    THROW 50003, N'Không tìm thấy tài khoản admin để gán Category cũ.', 1;

IF COL_LENGTH(N'dbo.categories', N'user_id') IS NULL
    ALTER TABLE dbo.categories ADD user_id INT NULL;

UPDATE dbo.categories
SET user_id = @adminId
WHERE user_id IS NULL;

ALTER TABLE dbo.categories ALTER COLUMN user_id INT NOT NULL;

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
