package vn.iotstar.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.CategoryDto;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.CategoryService;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               ProductRepository productRepository,
                               UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Category create(CategoryDto dto) {
        if (dto.getCategoryName() == null || dto.getCategoryName().isBlank()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }
        String trimmedName = dto.getCategoryName().trim();

        if (categoryRepository.existsByCategoryNameIgnoreCaseAndOwnerRole(trimmedName, Role.ADMIN)) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại trong hệ thống: " + trimmedName);
        }

        User adminOwner = userRepository.findByRole(Role.ADMIN)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản quản trị viên duy nhất của hệ thống"));

        Category category = new Category();
        category.setCategoryName(trimmedName);
        category.setImages(dto.getImages());
        category.setStatus(dto.getStatus());
        category.setOwner(adminOwner);

        return categoryRepository.save(category);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Category update(Integer id, CategoryDto dto) {
        if (dto.getCategoryName() == null || dto.getCategoryName().isBlank()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }
        String trimmedName = dto.getCategoryName().trim();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + id));

        if (categoryRepository.existsByCategoryNameIgnoreCaseAndOwnerRoleAndCategoryIdNot(trimmedName, Role.ADMIN, id)) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại trong hệ thống: " + trimmedName);
        }

        category.setCategoryName(trimmedName);
        category.setImages(dto.getImages());
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
}