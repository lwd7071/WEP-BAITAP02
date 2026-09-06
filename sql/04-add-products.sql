USE jakartaJPA;
GO

IF OBJECT_ID(N'dbo.categories', N'U') IS NULL
    THROW 50010, N'Bảng categories chưa tồn tại.', 1;
GO

IF OBJECT_ID(N'dbo.products', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.products (
        product_id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_products PRIMARY KEY,
        product_name NVARCHAR(255) NOT NULL,
        unit_price DECIMAL(18,2) NOT NULL,
        quantity INT NOT NULL,
        description NVARCHAR(MAX) NULL,
        images NVARCHAR(500) NULL,
        status INT NOT NULL,
        created_date DATETIME2 NOT NULL CONSTRAINT df_products_created_date DEFAULT SYSDATETIME(),
        category_id INT NOT NULL,
        CONSTRAINT ck_products_price CHECK (unit_price >= 0),
        CONSTRAINT ck_products_quantity CHECK (quantity >= 0),
        CONSTRAINT ck_products_status CHECK (status IN (0, 1)),
        CONSTRAINT fk_products_categories FOREIGN KEY (category_id)
            REFERENCES dbo.categories(category_id)
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_products_category' AND object_id = OBJECT_ID(N'dbo.products'))
    CREATE INDEX ix_products_category ON dbo.products(category_id);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_products_latest' AND object_id = OBJECT_ID(N'dbo.products'))
    CREATE INDEX ix_products_latest ON dbo.products(status, created_date DESC, product_id DESC) INCLUDE (category_id);
GO

DECLARE @phoneCategory INT = (SELECT TOP 1 category_id FROM dbo.categories WHERE category_name = N'Điện thoại' ORDER BY category_id);
DECLARE @computerCategory INT = (SELECT TOP 1 category_id FROM dbo.categories WHERE category_name = N'Máy tính' ORDER BY category_id);
DECLARE @accessoryCategory INT = (SELECT TOP 1 category_id FROM dbo.categories WHERE category_name = N'Phụ kiện' ORDER BY category_id);

IF @phoneCategory IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'Điện thoại Nova X')
    INSERT dbo.products(product_name, unit_price, quantity, description, images, status, category_id)
    VALUES (N'Điện thoại Nova X', 12990000, 18, N'Màn hình OLED, hiệu năng mạnh và camera sắc nét.', N'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=900', 1, @phoneCategory);

IF @computerCategory IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'Laptop WorkBook 14')
    INSERT dbo.products(product_name, unit_price, quantity, description, images, status, category_id)
    VALUES (N'Laptop WorkBook 14', 21990000, 9, N'Laptop gọn nhẹ dành cho học tập và làm việc.', N'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=900', 1, @computerCategory);

IF @accessoryCategory IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.products WHERE product_name = N'Tai nghe không dây AirBeat')
    INSERT dbo.products(product_name, unit_price, quantity, description, images, status, category_id)
    VALUES (N'Tai nghe không dây AirBeat', 1490000, 25, N'Tai nghe Bluetooth với hộp sạc nhỏ gọn.', NULL, 1, @accessoryCategory);
GO

PRINT N'Đã tạo bảng products, index và dữ liệu mẫu.';
GO
