package vn.iotstar.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class ApiProductControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Category activeCategory;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        User admin = userRepository.findByRole(Role.ADMIN).orElseThrow();
        Category cat = new Category();
        cat.setCategoryName("Api Product Cat");
        cat.setStatus(1);
        cat.setOwner(admin);
        activeCategory = categoryRepository.save(cat);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("User gọi GET /api/products chỉ nhận các sản phẩm active")
    void userGetProducts_OnlyActiveProducts() throws Exception {
        ProductDto p1 = new ProductDto();
        p1.setProductName("Active API Item");
        p1.setUnitPrice(new BigDecimal("100000"));
        p1.setQuantity(5);
        p1.setStatus(1);
        p1.setCategoryId(activeCategory.getCategoryId());
        productService.create(p1);

        ProductDto p2 = new ProductDto();
        p2.setProductName("Inactive API Item");
        p2.setUnitPrice(new BigDecimal("200000"));
        p2.setQuantity(2);
        p2.setStatus(0);
        p2.setCategoryId(activeCategory.getCategoryId());
        productService.create(p2);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].productName").value("Active API Item"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("User cố tình POST /api/products phải nhận 403 Forbidden")
    void userMutateProduct_Forbidden() throws Exception {
        ProductDto p = new ProductDto();
        p.setProductName("Hack Product");
        p.setUnitPrice(new BigDecimal("999"));
        p.setQuantity(1);
        p.setStatus(1);
        p.setCategoryId(activeCategory.getCategoryId());

        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin tạo sản phẩm qua REST API trả về 201 Created")
    void adminCreateProduct_Success() throws Exception {
        ProductDto p = new ProductDto();
        p.setProductName("REST Admin Product");
        p.setUnitPrice(new BigDecimal("450000"));
        p.setQuantity(10);
        p.setStatus(1);
        p.setCategoryId(activeCategory.getCategoryId());

        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("REST Admin Product"));
    }
}