package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dao.ICategoryDao;
import vn.iotstar.dao.impl.CategoryDao;
import vn.iotstar.dto.CategoryDto;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.ICategoryService;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService, ICategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ICategoryDao categoryDao;

    public CategoryServiceImpl() {
        this(new CategoryDao());
    }

    public CategoryServiceImpl(ICategoryDao categoryDao) {
        this.categoryDao = categoryDao;
        this.categoryRepository = null;
        this.productRepository = null;
        this.userRepository = null;
    }

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               ProductRepository productRepository,
                               UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.categoryDao = null;
    }

    // --- Phương thức Spring Boot CategoryService mới ---

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Category create(CategoryDto dto) {
        if (dto.getCategoryName() == null || dto.getCategoryName().isBlank()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }
        User admin = userRepository.findByRole(Role.ADMIN)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản quản trị viên trong hệ thống"));

        if (categoryRepository.existsByCategoryNameIgnoreCaseAndOwnerRole(dto.getCategoryName().trim(), Role.ADMIN)) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }

        Category category = new Category();
        category.setCategoryName(dto.getCategoryName().trim());
        category.setImages(dto.getImages());
        category.setStatus(dto.getStatus() != 0 ? dto.getStatus() : 1);
        category.setOwner(admin);

        return categoryRepository.save(category);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Category update(Integer id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + id));

        if (dto.getCategoryName() == null || dto.getCategoryName().isBlank()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }

        if (categoryRepository.existsByCategoryNameIgnoreCaseAndOwnerRoleAndCategoryIdNot(
                dto.getCategoryName().trim(), Role.ADMIN, id)) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }

        category.setCategoryName(dto.getCategoryName().trim());
        if (dto.getImages() != null && !dto.getImages().isBlank()) {
            category.setImages(dto.getImages());
        }
        category.setStatus(dto.getStatus());

        return categoryRepository.save(category);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + id));

        long productCount = productRepository.countByCategory_CategoryId(id);
        if (productCount > 0) {
            throw new IllegalStateException("Không thể xóa danh mục đang chứa " + productCount + " sản phẩm liên kết");
        }

        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findById(Integer id) {
        return categoryRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Category> searchAdmin(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return categoryRepository.findByOwnerRoleAndCategoryNameContainingIgnoreCase(
                    Role.ADMIN, keyword.trim(), pageable);
        }
        return categoryRepository.findByOwnerRole(Role.ADMIN, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAllActiveAdmin() {
        return categoryRepository.findByOwnerRoleAndStatusOrderByCategoryNameAsc(Role.ADMIN, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAllAdmin() {
        return categoryRepository.findByOwnerRoleOrderByCategoryNameAsc(Role.ADMIN);
    }

    // --- Phương thức ICategoryService legacy ---

    @Override
    public void insert(Category category, int ownerId) {
        if (categoryDao != null) {
            categoryDao.insert(category, ownerId);
        }
    }

    @Override
    public void update(Category category, int ownerId) {
        if (categoryDao != null) {
            categoryDao.update(category, ownerId);
        }
    }

    @Override
    public void delete(int categoryId, int ownerId) {
        if (categoryDao != null) {
            categoryDao.delete(categoryId, ownerId);
        }
    }

    @Override
    public Category findById(int categoryId, int ownerId) {
        return categoryDao != null ? categoryDao.findById(categoryId, ownerId) : null;
    }

    @Override
    public Category findByCategoryName(String name, int ownerId) {
        return categoryDao != null ? categoryDao.findByCategoryName(name, ownerId) : null;
    }

    @Override
    public List<Category> findAll(int ownerId) {
        return categoryDao != null ? categoryDao.findAll(ownerId) : List.of();
    }

    @Override
    public List<Category> searchByName(String keyword, int ownerId) {
        return categoryDao != null ? categoryDao.searchByName(keyword, ownerId) : List.of();
    }

    @Override
    public List<Category> findAll(int ownerId, int page, int pageSize) {
        return categoryDao != null ? categoryDao.findAll(ownerId, page, pageSize) : List.of();
    }

    @Override
    public int count(int ownerId) {
        return categoryDao != null ? categoryDao.count(ownerId) : 0;
    }
}