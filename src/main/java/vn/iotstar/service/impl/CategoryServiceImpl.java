package vn.iotstar.service.impl;

import vn.iotstar.dao.ICategoryDao;
import vn.iotstar.dao.impl.CategoryDao;
import vn.iotstar.entity.Category;
import vn.iotstar.service.ICategoryService;

import java.util.List;

public class CategoryServiceImpl implements ICategoryService {
    private final ICategoryDao categoryDao;

    public CategoryServiceImpl() {
        this(new CategoryDao());
    }

    public CategoryServiceImpl(ICategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Override
    public void insert(Category category) {
        validate(category);
        if (categoryDao.findByCategoryName(category.getCategoryName()) != null) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }
        categoryDao.insert(category);
    }

    @Override
    public void update(Category category) {
        validate(category);
        Category current = categoryDao.findById(category.getCategoryId());
        if (current == null) {
            throw new IllegalArgumentException("Không tìm thấy danh mục");
        }
        Category duplicate = categoryDao.findByCategoryName(category.getCategoryName());
        if (duplicate != null && duplicate.getCategoryId() != category.getCategoryId()) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }
        categoryDao.update(category);
    }

    @Override
    public void delete(int categoryId) {
        categoryDao.delete(categoryId);
    }

    @Override
    public Category findById(int categoryId) {
        return categoryDao.findById(categoryId);
    }

    @Override
    public Category findByCategoryName(String name) {
        return categoryDao.findByCategoryName(name);
    }

    @Override
    public List<Category> findAll() {
        return categoryDao.findAll();
    }

    @Override
    public List<Category> searchByName(String keyword) {
        return keyword == null || keyword.isBlank() ? findAll() : categoryDao.searchByName(keyword);
    }

    @Override
    public List<Category> findAll(int page, int pageSize) {
        return categoryDao.findAll(page, pageSize);
    }

    @Override
    public int count() {
        return categoryDao.count();
    }

    private void validate(Category category) {
        if (category == null || category.getCategoryName() == null || category.getCategoryName().isBlank()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }
        category.setCategoryName(category.getCategoryName().trim());
        if (category.getStatus() != 0 && category.getStatus() != 1) {
            throw new IllegalArgumentException("Trạng thái danh mục không hợp lệ");
        }
    }
}
