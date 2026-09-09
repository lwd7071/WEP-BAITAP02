package vn.iotstar.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import vn.iotstar.dto.ProductDto;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.ProductService;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class PublicProductControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        User admin = userRepository.findByRole(Role.ADMIN).orElseThrow();
        Category cat = new Category();
        cat.setCategoryName("Public Controller Cat");
        cat.setStatus(1);
        cat.setOwner(admin);
        testCategory = categoryRepository.save(cat);
    }

    @Test
    @DisplayName("Khách chưa đăng nhập vào /products phải chuyển hướng về /login")
    void publicProductCatalog_Unauthenticated_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/login")));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Người dùng đã đăng nhập vào /products trả về view catalog và danh sách")
    void publicProductCatalog_Authenticated_ShouldReturnProducts() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-list"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Xem chi tiết sản phẩm bị khóa (inactive) phải trả về 404 Not Found")
    void productDetail_WhenInactive_ShouldReturn404() throws Exception {
        ProductDto p = new ProductDto();
        p.setProductName("Inactive Detail Product");
        p.setUnitPrice(new BigDecimal("100000"));
        p.setQuantity(10);
        p.setStatus(0);
        p.setCategoryId(testCategory.getCategoryId());
        Product created = productService.create(p);

        mockMvc.perform(get("/products/" + created.getProductId()))
                .andExpect(status().isNotFound());
    }
}