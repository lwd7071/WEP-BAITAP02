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
import vn.iotstar.dto.UserAdminCreateForm;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class ApiUserControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

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
    @WithMockUser(roles = "USER")
    @DisplayName("User thường gọi GET /api/users phải nhận 403 Forbidden")
    void getUsers_AsUser_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin gọi GET /api/users thành công trả về danh sách có phân trang")
    void getUsers_AsAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin tạo người dùng qua REST API luôn có role USER, status ACTIVE")
    void createUser_AlwaysRoleUser_StatusActive() throws Exception {
        UserAdminCreateForm form = new UserAdminCreateForm();
        form.setUsername("api_created_user");
        form.setEmail("api_created@iotstar.vn");
        form.setFullName("Api Created User");
        form.setPassword("Secret123456");
        form.setPhone("0988776655");

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("api_created_user"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin cố tình toggle status của tài khoản ADMIN qua REST API phải bị từ chối")
    void toggleStatus_Admin_Rejected() throws Exception {
        User admin = userRepository.findByRole(Role.ADMIN).orElseThrow();

        mockMvc.perform(patch("/api/users/" + admin.getId() + "/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "BLOCKED"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin cố tình xóa tài khoản ADMIN qua REST API phải bị từ chối")
    void deleteUser_Admin_Rejected() throws Exception {
        User admin = userRepository.findByRole(Role.ADMIN).orElseThrow();

        mockMvc.perform(delete("/api/users/" + admin.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}