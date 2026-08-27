package vn.iotstar.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "videos")
@NamedQuery(name = "Video.findAll", query = "SELECT v FROM Video v ORDER BY v.videoId")
public class Video implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "video_id", length = 50)
    private String videoId;

    @Column(nullable = false)
    private boolean active;

    @Column(columnDefinition = "nvarchar(max)")
    private String description;

    @Column(length = 500, columnDefinition = "nvarchar(500)")
    private String poster;

    @Column(length = 500, columnDefinition = "nvarchar(500)")
    private String title;

    @Column(nullable = false)
    private int views;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public Video() {
    }

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}
