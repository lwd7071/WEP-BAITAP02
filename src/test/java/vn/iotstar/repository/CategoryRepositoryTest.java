package vn.iotstar.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CategoryRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    private User admin;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setUsername("admin_cat_test");
        admin.setEmail("admin_cat@iotstar.vn");
        admin.setFullName("Admin Category Test");
        admin.setPassword("hash");
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        entityManager.persist(admin);

        Category c1 = new Category("Điện thoại iPhone", "iphone.jpg", 1);
        c1.setOwner(admin);
        entityManager.persist(c1);

        Category c2 = new Category("Laptop Dell Inspiron", "dell.jpg", 1);
        c2.setOwner(admin);
        entityManager.persist(c2);

        Category c3 = new Category("Phụ kiện tai nghe", "audio.jpg", 0);
        c3.setOwner(admin);
        entityManager.persist(c3);

        entityManager.flush();
    }

    @Test
    @DisplayName("Tìm kiếm Category không phân biệt hoa thường và hỗ trợ phân trang")
    void searchCategory_ShouldIgnoreCase() {
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "categoryId"));
        Page<Category> result = categoryRepository.findByOwnerRoleAndCategoryNameContainingIgnoreCase(
                Role.ADMIN, "iphone", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategoryName()).isEqualTo("Điện thoại iPhone");

        Page<Category> resultUpper = categoryRepository.findByOwnerRoleAndCategoryNameContainingIgnoreCase(
                Role.ADMIN, "DELL", pageable);
        assertThat(resultUpper.getContent()).hasSize(1);
        assertThat(resultUpper.getContent().get(0).getCategoryName()).isEqualTo("Laptop Dell Inspiron");
    }
}