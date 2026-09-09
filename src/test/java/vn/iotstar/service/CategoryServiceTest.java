package vn.iotstar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.CategoryDto;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Role;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin tạo danh mục mới sẽ tự động gán owner là ADMIN duy nhất của hệ thống")
    void createCategory_ShouldAssignToAdminOwner() {
        CategoryDto dto = new CategoryDto();
        dto.setCategoryName("Danh mục Điện máy");
        dto.setImages("dienmay.png");
        dto.setStatus(1);

        Category created = categoryService.create(dto);

        assertThat(created.getCategoryId()).isPositive();
        assertThat(created.getOwner()).isNotNull();
        assertThat(created.getOwner().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Xóa Category đang có Product phải ném ngoại lệ từ chối xóa")
    void deleteCategory_WhenHasProducts_ShouldThrowException() {
        CategoryDto dto = new CategoryDto();
        dto.setCategoryName("Laptop");
        dto.setStatus(1);
        Category cat = categoryService.create(dto);

        Product p = new Product();
        p.setProductName("Laptop ThinkPad");
        p.setCategory(cat);
        p.setUnitPrice(new BigDecimal("20000000"));
        p.setQuantity(5);
        p.setStatus(1);
        productRepository.save(p);

        assertThatThrownBy(() -> categoryService.delete(cat.getCategoryId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sản phẩm");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Tìm kiếm Category theo từ khóa lọc trực tiếp tại database")
    void searchCategory_WithKeyword_ShouldFilterAtDatabase() {
        CategoryDto c1 = new CategoryDto();
        c1.setCategoryName("Điện thoại Samsung");
        c1.setStatus(1);
        categoryService.create(c1);

        CategoryDto c2 = new CategoryDto();
        c2.setCategoryName("Máy tính bảng Apple");
        c2.setStatus(1);
        categoryService.create(c2);

        Page<Category> result = categoryService.searchAdmin("samsung", PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategoryName()).isEqualTo("Điện thoại Samsung");
    }
}