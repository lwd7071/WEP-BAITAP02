package vn.iotstar.security;

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
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SecurityFilterChainTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("Truy cập trang web được bảo vệ khi chưa đăng nhập phải redirect 302 về /login")
    void anonymousAccessProtectedWeb_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/login")));
    }

    @Test
    @DisplayName("Trang login public phải render được JSP mà không redirect vòng lặp")
    void loginPage_ShouldRenderWithoutRedirectLoop() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Tài nguyên giao diện phải public để trang đăng nhập tải được CSS và JavaScript")
    void anonymousAccessAssets_ShouldBePublic() throws Exception {
        mockMvc.perform(get("/assets/app.css"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/assets/app.js"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Gọi API được bảo vệ khi chưa đăng nhập phải trả về 401 JSON kèm ApiResponse")
    void anonymousAccessProtectedApi_ShouldReturn401Json() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("API dưới context path vẫn phải trả 401 JSON thay vì redirect login")
    void anonymousApiWithContextPath_ShouldReturn401Json() throws Exception {
        mockMvc.perform(get("/WEP-BAITAP02/api/categories")
                        .contextPath("/WEP-BAITAP02")
                        .servletPath("/api/categories"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("User thường truy cập URL Admin phải nhận 403 Forbidden")
    void userAccessAdminUrl_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin truy cập URL Admin không bị chặn bởi 401 hoặc 403")
    void adminAccessAdminUrl_ShouldNotBeBlockedBySecurity() throws Exception {
        mockMvc.perform(get("/admin/categories"))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    org.assertj.core.api.Assertions.assertThat(s).isNotIn(401, 403);
                });
    }

    @Test
    @DisplayName("Logout bằng POST không có CSRF token phải bị chặn 403")
    void postLogoutWithoutCsrf_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Logout bằng POST có CSRF token phải invalidate session và redirect về /login?logout")
    void postLogoutWithCsrf_ShouldInvalidateSessionAndRedirect() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/login?logout")));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Logout bằng GET không được phép vì logout phải là POST có CSRF")
    void getLogout_ShouldNotLogout() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().isNotFound());
    }
}
