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
    public void insert(Category category, int ownerId) {
        validateOwner(ownerId);
        validate(category);
        if (categoryDao.findByCategoryName(category.getCategoryName(), ownerId) != null) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại trong tài khoản của bạn");
        }
        categoryDao.insert(category, ownerId);
    }

    @Override
    public void update(Category category, int ownerId) {
        validateOwner(ownerId);
        validate(category);
        Category current = categoryDao.findById(category.getCategoryId(), ownerId);
        if (current == null) {
            throw new IllegalArgumentException("Không tìm thấy danh mục hoặc bạn không có quyền sửa");
        }
        Category duplicate = categoryDao.findByCategoryName(category.getCategoryName(), ownerId);
        if (duplicate != null && duplicate.getCategoryId() != category.getCategoryId()) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại trong tài khoản của bạn");
        }
        categoryDao.update(category, ownerId);
    }

    @Override
    public void delete(int categoryId, int ownerId) {
        validateOwner(ownerId);
        categoryDao.delete(categoryId, ownerId);
    }

    @Override
    public Category findById(int categoryId, int ownerId) {
        validateOwner(ownerId);
        return categoryDao.findById(categoryId, ownerId);
    }

    @Override
    public Category findByCategoryName(String name, int ownerId) {
        validateOwner(ownerId);
        return name == null || name.isBlank() ? null : categoryDao.findByCategoryName(name.trim(), ownerId);
    }

    @Override
    public List<Category> findAll(int ownerId) {
        validateOwner(ownerId);
        return categoryDao.findAll(ownerId);
    }

    @Override
    public List<Category> searchByName(String keyword, int ownerId) {
        validateOwner(ownerId);
        return keyword == null || keyword.isBlank() ? findAll(ownerId) : categoryDao.searchByName(keyword, ownerId);
    }

    @Override
    public List<Category> findAll(int ownerId, int page, int pageSize) {
        validateOwner(ownerId);
        return categoryDao.findAll(ownerId, page, pageSize);
    }

    @Override
    public int count(int ownerId) {
        validateOwner(ownerId);
        return categoryDao.count(ownerId);
    }

    private void validateOwner(int ownerId) {
        if (ownerId <= 0) {
            throw new IllegalArgumentException("Tài khoản sở hữu danh mục không hợp lệ");
        }
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
