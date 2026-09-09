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
import vn.iotstar.dto.ProductDto;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private Category activeCategory;
    private Category inactiveCategory;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        User admin = userRepository.findByRole(Role.ADMIN).orElseThrow();

        Category cat1 = new Category();
        cat1.setCategoryName("Active Cat for Product Test");
        cat1.setStatus(1);
        cat1.setOwner(admin);
        activeCategory = categoryRepository.save(cat1);

        Category cat2 = new Category();
        cat2.setCategoryName("Inactive Cat for Product Test");
        cat2.setStatus(0);
        cat2.setOwner(admin);
        inactiveCategory = categoryRepository.save(cat2);
    }

    @Test
    @DisplayName("Public Catalog chỉ trả về sản phẩm hoạt động thuộc danh mục hoạt động của Admin")
    void publicProductCatalog_ShouldOnlyShowActiveAdminProducts() {
        ProductDto p1 = new ProductDto();
        p1.setProductName("Laptop Dell Active");
        p1.setUnitPrice(new BigDecimal("15000000"));
        p1.setQuantity(10);
        p1.setStatus(1);
        p1.setCategoryId(activeCategory.getCategoryId());
        productService.create(p1);

        ProductDto p2 = new ProductDto();
        p2.setProductName("Laptop HP Inactive");
        p2.setUnitPrice(new BigDecimal("12000000"));
        p2.setQuantity(5);
        p2.setStatus(0);
        p2.setCategoryId(activeCategory.getCategoryId());
        productService.create(p2);

        ProductDto p3 = new ProductDto();
        p3.setProductName("Phone Asus In Inactive Cat");
        p3.setUnitPrice(new BigDecimal("8000000"));
        p3.setQuantity(3);
        p3.setStatus(1);
        p3.setCategoryId(inactiveCategory.getCategoryId());
        productService.create(p3);

        Page<Product> publicPage = productService.searchPublic(null, null, PageRequest.of(0, 10));
        assertThat(publicPage.getTotalElements()).isEqualTo(1);
        assertThat(publicPage.getContent().get(0).getProductName()).isEqualTo("Laptop Dell Active");
    }

    @Test
    @DisplayName("Nhiều người dùng khác nhau xem public catalog đều nhận danh mục sản phẩm như nhau")
    void publicProductCatalog_DifferentUsers_SeeIdenticalCatalog() {
        ProductDto p = new ProductDto();
        p.setProductName("Identical View Product");
        p.setUnitPrice(new BigDecimal("5000000"));
        p.setQuantity(20);
        p.setStatus(1);
        p.setCategoryId(activeCategory.getCategoryId());
        productService.create(p);

        Page<Product> view1 = productService.searchPublic(null, null, PageRequest.of(0, 10));
        Page<Product> view2 = productService.searchPublic(null, null, PageRequest.of(0, 10));

        assertThat(view1.getContent()).hasSize(1);
        assertThat(view2.getContent()).hasSize(1);
        assertThat(view1.getContent().get(0).getProductId()).isEqualTo(view2.getContent().get(0).getProductId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin thực hiện đầy đủ chu trình CRUD sản phẩm thành công")
    void adminProductCrud_FullWorkflow_Success() {
        ProductDto form = new ProductDto();
        form.setProductName("Admin Managed Mouse");
        form.setUnitPrice(new BigDecimal("250000"));
        form.setQuantity(50);
        form.setDescription("Gaming mouse");
        form.setStatus(1);
        form.setCategoryId(activeCategory.getCategoryId());

        // Create
        Product created = productService.create(form);
        assertThat(created.getProductId()).isPositive();

        // Update
        form.setProductName("Admin Managed Mouse V2");
        form.setUnitPrice(new BigDecimal("300000"));
        Product updated = productService.update(created.getProductId(), form);
        assertThat(updated.getProductName()).isEqualTo("Admin Managed Mouse V2");
        assertThat(updated.getUnitPrice()).isEqualByComparingTo(new BigDecimal("300000"));

        // Delete
        productService.delete(created.getProductId());
        assertThat(productRepository.findById(created.getProductId())).isEmpty();
    }
}