package vn.iotstar.dto;

import vn.iotstar.entity.Product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object cho sản phẩm (Product)
 */
public class ProductDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private int productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private String description;
    private String images;
    private int status;
    private LocalDateTime createdDate;
    private int categoryId;
    private String categoryName;

    public ProductDto() {
    }

    public ProductDto(int productId, String productName, BigDecimal unitPrice, int quantity,
                      String description, String images, int status, LocalDateTime createdDate,
                      int categoryId, String categoryName) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.description = description;
        this.images = images;
        this.status = status;
        this.createdDate = createdDate;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    // Chuyển đổi từ Entity sang DTO
    public static ProductDto fromEntity(Product product) {
        if (product == null) {
            return null;
        }
        int categoryId = 0;
        String categoryName = null;
        if (product.getCategory() != null) {
            categoryId = product.getCategory().getCategoryId();
            categoryName = product.getCategory().getCategoryName();
        }
        return new ProductDto(
                product.getProductId(),
                product.getProductName(),
                product.getUnitPrice(),
                product.getQuantity(),
                product.getDescription(),
                product.getImages(),
                product.getStatus(),
                product.getCreatedDate(),
                categoryId,
                categoryName
        );
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
