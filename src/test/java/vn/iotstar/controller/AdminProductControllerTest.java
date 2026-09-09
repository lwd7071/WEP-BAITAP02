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
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.UserRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class AdminProductControllerTest {

    @Autowired
    private WebApplicationContext context;

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
        cat.setCategoryName("Admin Controller Cat");
        cat.setStatus(1);
        cat.setOwner(admin);
        testCategory = categoryRepository.save(cat);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("User thường cố tình truy cập /admin/products phải trả về 403 Forbidden")
    void userAttemptToAccessAdminProducts_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin truy cập /admin/products trả về view admin và danh sách phân trang")
    void adminAccessAdminProducts_ShouldReturnView() throws Exception {
        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-list"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin tạo sản phẩm mới thành công redirect về /admin/products")
    void adminCreateProduct_Success() throws Exception {
        mockMvc.perform(post("/admin/products")
                        .with(csrf())
                        .param("productName", "New Admin Headset")
                        .param("unitPrice", "500000")
                        .param("quantity", "15")
                        .param("categoryId", String.valueOf(testCategory.getCategoryId()))
                        .param("status", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}