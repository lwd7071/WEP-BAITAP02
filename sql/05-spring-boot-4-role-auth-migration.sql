USE jakartaJPA;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF OBJECT_ID(N'dbo.users', N'U') IS NULL
    THROW 50020, N'Bảng users chưa tồn tại.', 1;
GO

-- 1. Thêm cột mới nếu chưa tồn tại
IF COL_LENGTH(N'dbo.users', N'role') IS NULL
    ALTER TABLE dbo.users ADD role NVARCHAR(20) NULL;

IF COL_LENGTH(N'dbo.users', N'provider') IS NULL
    ALTER TABLE dbo.users ADD provider NVARCHAR(20) NULL;

IF COL_LENGTH(N'dbo.users', N'provider_id') IS NULL
    ALTER TABLE dbo.users ADD provider_id NVARCHAR(255) NULL;

IF COL_LENGTH(N'dbo.users', N'otp_code') IS NULL
    ALTER TABLE dbo.users ADD otp_code NVARCHAR(10) NULL;

IF COL_LENGTH(N'dbo.users', N'otp_expiry') IS NULL
    ALTER TABLE dbo.users ADD otp_expiry DATETIME2 NULL;

IF COL_LENGTH(N'dbo.users', N'otp_purpose') IS NULL
    ALTER TABLE dbo.users ADD otp_purpose NVARCHAR(50) NULL;

IF COL_LENGTH(N'dbo.users', N'updated_date') IS NULL
    ALTER TABLE dbo.users ADD updated_date DATETIME2 NULL;

IF COL_LENGTH(N'dbo.users', N'created_date') IS NULL
    ALTER TABLE dbo.users ADD created_date DATETIME2 NULL;
GO

-- 2. Chuyển đổi cột status từ số sang chuỗi NVARCHAR(20)
IF EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'dbo.users') 
      AND name = N'status' 
      AND system_type_id IN (TYPE_ID('int'), TYPE_ID('tinyint'), TYPE_ID('smallint'), TYPE_ID('bit'))
)
BEGIN
    IF EXISTS (SELECT 1 FROM dbo.users WHERE status NOT IN (0, 1) OR status IS NULL)
        THROW 50021, N'Cột users.status chứa giá trị không hợp lệ hoặc NULL; migration đã dừng để xử lý thủ công.', 1;

    IF COL_LENGTH(N'dbo.users', N'status_new') IS NULL
        ALTER TABLE dbo.users ADD status_new NVARCHAR(20) NULL;

    EXEC(N'
        UPDATE dbo.users
        SET status_new = CASE 
            WHEN status = 1 THEN N''ACTIVE''
            WHEN status = 0 THEN N''PENDING''
        END;
    ');

    DECLARE @defConstraint NVARCHAR(255);
    SELECT @defConstraint = d.name
    FROM sys.default_constraints d
    JOIN sys.columns c ON d.parent_object_id = c.object_id AND d.parent_column_id = c.column_id
    WHERE d.parent_object_id = OBJECT_ID(N'dbo.users') AND c.name = N'status';

    IF @defConstraint IS NOT NULL
        EXEC(N'ALTER TABLE dbo.users DROP CONSTRAINT ' + @defConstraint);

    ALTER TABLE dbo.users DROP COLUMN status;
    EXEC sp_rename N'dbo.users.status_new', N'status', N'COLUMN';
    ALTER TABLE dbo.users ALTER COLUMN status NVARCHAR(20) NOT NULL;
END
GO

-- 3. Chuyển dữ liệu role, provider, updated_date
UPDATE dbo.users
SET role = CASE 
        WHEN username = N'admin' OR role_id = 1 THEN N'ADMIN'
        ELSE N'USER'
    END
WHERE role IS NULL;

UPDATE dbo.users
SET provider = N'LOCAL'
WHERE provider IS NULL;

UPDATE dbo.users
SET updated_date = ISNULL(created_date, SYSDATETIME())
WHERE updated_date IS NULL;

IF COL_LENGTH(N'dbo.users', N'code') IS NOT NULL
BEGIN
    EXEC(N'UPDATE dbo.users SET otp_code = code WHERE otp_code IS NULL AND code IS NOT NULL;');
END

ALTER TABLE dbo.users ALTER COLUMN role NVARCHAR(20) NOT NULL;
ALTER TABLE dbo.users ALTER COLUMN provider NVARCHAR(20) NOT NULL;
ALTER TABLE dbo.users ALTER COLUMN updated_date DATETIME2 NOT NULL;
GO

-- 4. Gán tất cả Category cũ về ADMIN duy nhất
DECLARE @adminId INT = (SELECT TOP 1 id FROM dbo.users WHERE role = N'ADMIN' ORDER BY id);
IF @adminId IS NOT NULL
BEGIN
    IF EXISTS (
        SELECT category_name
        FROM dbo.categories
        GROUP BY category_name
        HAVING COUNT(*) > 1
    )
        THROW 50022, N'Có category trùng tên giữa các owner; migration đã dừng để xử lý thủ công.', 1;

    UPDATE dbo.categories
    SET user_id = @adminId
    WHERE user_id <> @adminId;
END
GO

-- 5. Tạo filtered unique index SQL Server bảo đảm tối đa một admin và phone duy nhất khi không null
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes 
    WHERE name = N'uk_users_single_admin' AND object_id = OBJECT_ID(N'dbo.users')
)
BEGIN
    CREATE UNIQUE INDEX uk_users_single_admin
    ON dbo.users(role)
    WHERE role = 'ADMIN';
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'uk_users_google_provider_id' AND object_id = OBJECT_ID(N'dbo.users')
)
BEGIN
    CREATE UNIQUE INDEX uk_users_google_provider_id
    ON dbo.users(provider_id)
    WHERE provider = 'GOOGLE' AND provider_id IS NOT NULL;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes 
    WHERE name = N'uk_users_phone' AND object_id = OBJECT_ID(N'dbo.users')
)
BEGIN
    CREATE UNIQUE INDEX uk_users_phone
    ON dbo.users(phone)
    WHERE phone IS NOT NULL;
END
GO

PRINT N'Migration 05: Đã chuyển đổi dữ liệu sang Spring Boot, gán quyền và tạo filtered unique index 1 ADMIN thành công.';
GO
