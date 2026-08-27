USE master;
GO

IF DB_ID(N'jakartaJPA') IS NULL
BEGIN
    CREATE DATABASE jakartaJPA;
    PRINT N'Đã tạo database jakartaJPA.';
END
ELSE
    PRINT N'Database jakartaJPA đã tồn tại.';
GO
