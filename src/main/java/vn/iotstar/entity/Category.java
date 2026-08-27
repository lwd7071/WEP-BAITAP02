package vn.iotstar.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@NamedQuery(name = "Category.findAll", query = "SELECT c FROM Category c ORDER BY c.categoryId")
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private int categoryId;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Column(name = "category_name", nullable = false, length = 255, columnDefinition = "nvarchar(255)")
    private String categoryName;

    @Column(length = 500, columnDefinition = "nvarchar(500)")
    private String images;

    @Column(nullable = false)
    private int status;

    @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST)
    private List<Video> videos = new ArrayList<>();

    public Category() {
    }

    public Category(String categoryName, String images, int status) {
        this.categoryName = categoryName;
        this.images = images;
        this.status = status;
    }

    public Video addVideo(Video video) {
        videos.add(video);
        video.setCategory(this);
        return video;
    }

    public Video removeVideo(Video video) {
        videos.remove(video);
        video.setCategory(null);
        return video;
    }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public List<Video> getVideos() { return videos; }
    public void setVideos(List<Video> videos) { this.videos = videos; }
}
