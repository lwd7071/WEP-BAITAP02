package vn.iotstar.dao;

import vn.iotstar.entity.Category;

import java.util.List;

public interface ICategoryDao {
    void insert(Category category, int ownerId);
    void update(Category category, int ownerId);
    void delete(int categoryId, int ownerId);
    Category findById(int categoryId, int ownerId);
    Category findByCategoryName(String name, int ownerId);
    List<Category> findAll(int ownerId);
    List<Category> searchByName(String keyword, int ownerId);
    List<Category> findAll(int ownerId, int page, int pageSize);
    int count(int ownerId);
}
