package vn.iotstar.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import vn.iotstar.dto.RegisterRequestDto;
import vn.iotstar.entity.User;
import vn.iotstar.service.AuthService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@Transactional
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuthService authService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private vn.iotstar.service.EmailService emailService;

    private MockMvc mockMvc;

    @Test
    void invalidRegistrationShowsFieldErrors() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                .param("username", "ab").param("fullName", "")
                .param("email", "invalid").param("password", "1234"))
                .andExpect(view().name("register"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model()
                        .attributeHasFieldErrors("form", "username", "fullName", "email", "password"));
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /login trả về view login")
    void getLogin_ShouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("GET /register trả về view register")
    void getRegister_ShouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    @DisplayName("POST /register với dữ liệu hợp lệ redirect về /verify-otp")
    void postRegister_ValidData_ShouldRedirectToVerifyOtp() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "ctrl_reg_user")
                        .param("email", "ctrl_reg@iotstar.vn")
                        .param("fullName", "Controller Reg User")
                        .param("password", "secret123")
                        .param("phone", "0934567890"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/verify-otp?email=ctrl_reg%40iotstar.vn")));
    }

    @Test
    @DisplayName("GET /reset-password khi chưa có session redirect về /forgot-password")
    void getResetPassword_WithoutSession_ShouldRedirectToForgotPassword() throws Exception {
        mockMvc.perform(get("/reset-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/forgot-password"));
    }

    @Test
    @DisplayName("POST /forgot-password với username hợp lệ redirect về /reset-password và gán session")
    void postForgotPassword_ValidUser_ShouldSetSessionAndRedirect() throws Exception {
        RegisterRequestDto form = new RegisterRequestDto();
        form.setUsername("forgot_user");
        form.setEmail("forgot_user@iotstar.vn");
        form.setFullName("Forgot User");
        form.setPassword("password123");
        form.setPhone("0945678901");
        User registered = authService.register(form);
        authService.activate(registered.getEmail(), registered.getOtpCode());

        mockMvc.perform(post("/forgot-password")
                        .with(csrf())
                        .param("username", "forgot_user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/reset-password"))
                .andExpect(request().sessionAttribute("passwordResetUserId", org.hamcrest.Matchers.notNullValue()));
    }
}
