package vn.iotstar.dto;

import vn.iotstar.entity.Category;

import java.io.Serializable;

/**
 * Data Transfer Object cho danh mục (Category)
 */
public class CategoryDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private int categoryId;
    private String categoryName;
    private String images;
    private int status;
    private int ownerId;
    private String ownerUsername;

    public CategoryDto() {
    }

    public CategoryDto(int categoryId, String categoryName, String images, int status, int ownerId, String ownerUsername) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.images = images;
        this.status = status;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
    }

    // Chuyển đổi từ JPA Entity sang DTO để tránh vòng lặp và dữ liệu thừa
    public static CategoryDto fromEntity(Category category) {
        if (category == null) {
            return null;
        }
        int ownerId = 0;
        String ownerUsername = null;
        if (category.getOwner() != null) {
            ownerId = category.getOwner().getId();
            ownerUsername = category.getOwner().getUsername();
        }
        return new CategoryDto(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getImages(),
                category.getStatus(),
                ownerId,
                ownerUsername
        );
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

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }
}
