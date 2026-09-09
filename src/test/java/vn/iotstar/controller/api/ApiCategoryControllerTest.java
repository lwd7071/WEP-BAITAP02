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
import vn.iotstar.dto.CategoryDto;
import vn.iotstar.entity.Category;
import vn.iotstar.service.CategoryService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class ApiCategoryControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin lấy danh sách danh mục qua REST API trả về 200 JSON")
    void getCategories_Authenticated_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin tạo danh mục qua POST /api/categories trả về 201 Created")
    void createCategory_Admin_ShouldReturn201() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setCategoryName("Tablet Api Test");
        dto.setStatus(1);

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categoryName").value("Tablet Api Test"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin xóa danh mục qua DELETE /api/categories/{id} trả về 200")
    void deleteCategory_Admin_ShouldReturn200() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setCategoryName("Delete Api Test");
        dto.setStatus(1);
        Category created = categoryService.create(dto);

        mockMvc.perform(delete("/api/categories/" + created.getCategoryId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}