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
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserStatus;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        User admin = new User();
        admin.setUsername("admin_prod_test");
        admin.setEmail("admin_prod@iotstar.vn");
        admin.setFullName("Admin Product Test");
        admin.setPassword("pass");
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        entityManager.persist(admin);

        User normalUser = new User();
        normalUser.setUsername("normal_user");
        normalUser.setEmail("normal_user@iotstar.vn");
        normalUser.setFullName("Normal User");
        normalUser.setPassword("pass");
        normalUser.setRole(Role.USER);
        normalUser.setStatus(UserStatus.ACTIVE);
        entityManager.persist(normalUser);

        // Cat 1: Admin, Active (status = 1)
        Category catActiveAdmin = new Category("Admin Active Cat", null, 1);
        catActiveAdmin.setOwner(admin);
        entityManager.persist(catActiveAdmin);

        // Cat 2: Admin, Inactive (status = 0)
        Category catInactiveAdmin = new Category("Admin Inactive Cat", null, 0);
        catInactiveAdmin.setOwner(admin);
        entityManager.persist(catInactiveAdmin);

        // Cat 3: Normal user (legacy test), Active (status = 1)
        Category catNormalUser = new Category("User Cat", null, 1);
        catNormalUser.setOwner(normalUser);
        entityManager.persist(catNormalUser);

        // p1: Cat 1 (Admin, Active), Product status = 1 => HỢP LỆ PUBLIC
        Product p1 = new Product();
        p1.setProductName("iPhone 15 Pro");
        p1.setUnitPrice(new BigDecimal("25000000"));
        p1.setQuantity(10);
        p1.setStatus(1);
        p1.setCategory(catActiveAdmin);
        entityManager.persist(p1);

        // p2: Cat 1 (Admin, Active), Product status = 0 => KHÔNG HỢP LỆ (product inactive)
        Product p2 = new Product();
        p2.setProductName("iPhone 11 Cu");
        p2.setUnitPrice(new BigDecimal("5000000"));
        p2.setQuantity(5);
        p2.setStatus(0);
        p2.setCategory(catActiveAdmin);
        entityManager.persist(p2);

        // p3: Cat 2 (Admin, Inactive), Product status = 1 => KHÔNG HỢP LỆ (category inactive)
        Product p3 = new Product();
        p3.setProductName("Macbook Pro Cu");
        p3.setUnitPrice(new BigDecimal("15000000"));
        p3.setQuantity(2);
        p3.setStatus(1);
        p3.setCategory(catInactiveAdmin);
        entityManager.persist(p3);

        // p4: Cat 3 (User, Active), Product status = 1 => KHÔNG HỢP LỆ (category owner != ADMIN)
        Product p4 = new Product();
        p4.setProductName("Ao thun user");
        p4.setUnitPrice(new BigDecimal("100000"));
        p4.setQuantity(50);
        p4.setStatus(1);
        p4.setCategory(catNormalUser);
        entityManager.persist(p4);

        entityManager.flush();
    }

    @Test
    @DisplayName("Public search chỉ trả về Product có status=1, Category status=1 và Category owner Role ADMIN")
    void publicSearch_ShouldOnlyReturnActiveAdminProducts() {
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate", "productId"));

        Page<Product> publicProducts = productRepository.findPublicProducts(null, null, pageable);

        assertThat(publicProducts.getContent()).hasSize(1);
        assertThat(publicProducts.getContent().get(0).getProductName()).isEqualTo("iPhone 15 Pro");
    }
}