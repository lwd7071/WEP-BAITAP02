-- Run in SSMS after stopping the application.
-- Keeps the existing username 'admin' only; does not change its password.
-- Deletes other accounts permanently. Back up jakartaJPA first if needed.
USE [jakartaJPA];
GO
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @adminId INT, @adminCount INT, @deleted INT;
    SELECT @adminCount = COUNT(*), @adminId = MIN(id)
    FROM dbo.users WITH (TABLOCKX, HOLDLOCK)
    WHERE username = N'admin' AND role = N'ADMIN';

    IF @adminCount <> 1
        THROW 50060, N'Expected exactly one username admin with role ADMIN. No changes applied.', 1;

    -- Avoid silently cascading deletes into unexpected tables.
    IF EXISTS (
        SELECT 1
        FROM sys.foreign_keys fk
        WHERE fk.referenced_object_id = OBJECT_ID(N'dbo.users')
          AND NOT (
              fk.parent_object_id = OBJECT_ID(N'dbo.categories')
              AND (SELECT COUNT(*) FROM sys.foreign_key_columns c
                   WHERE c.constraint_object_id = fk.object_id) = 1
              AND EXISTS (
                  SELECT 1 FROM sys.foreign_key_columns c
                  WHERE c.constraint_object_id = fk.object_id
                    AND COL_NAME(c.parent_object_id, c.parent_column_id) = N'user_id'
                    AND COL_NAME(c.referenced_object_id, c.referenced_column_id) = N'id'
              )
          )
    )
        THROW 50061, N'Unexpected foreign key references users. Script stopped to preserve related data.', 1;

    IF EXISTS (SELECT 1 FROM sys.triggers
               WHERE parent_id IN (OBJECT_ID(N'dbo.users'), OBJECT_ID(N'dbo.categories'))
                 AND is_disabled = 0)
        THROW 50062, N'Active triggers found on users/categories. Review before resetting accounts.', 1;

    -- The current entity still reads role_id but no longer writes it.
    -- Keep the column and existing values; allow inserts using the new role column.
    IF NOT EXISTS (SELECT 1 FROM sys.columns
                   WHERE object_id = OBJECT_ID(N'dbo.users')
                     AND name = N'role_id' AND system_type_id = TYPE_ID(N'int'))
        THROW 50063, N'Expected users.role_id to be INT. No changes applied.', 1;

    ALTER TABLE dbo.users ALTER COLUMN role_id INT NULL;

    -- Preserve categories and all products belonging to those categories.
    IF OBJECT_ID(N'dbo.categories', N'U') IS NOT NULL
        UPDATE dbo.categories
        SET user_id = @adminId
        WHERE user_id <> @adminId OR user_id IS NULL;

    DELETE FROM dbo.users WHERE id <> @adminId;
    SET @deleted = @@ROWCOUNT;

    IF (SELECT COUNT(*) FROM dbo.users) <> 1
        THROW 50064, N'Account reset postcondition failed.', 1;

    COMMIT TRANSACTION;

    SELECT @deleted AS deleted_accounts;
    SELECT id, username, role, status FROM dbo.users;
    PRINT N'Done. Legacy role_id now accepts NULL. Only admin remains; password unchanged.';
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
