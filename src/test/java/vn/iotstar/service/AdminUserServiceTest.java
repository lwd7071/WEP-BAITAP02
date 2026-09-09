package vn.iotstar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.UserAdminCreateForm;
import vn.iotstar.dto.UserAdminUpdateForm;
import vn.iotstar.entity.AuthProvider;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserStatus;
import vn.iotstar.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AdminUserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin tạo người dùng luôn có role USER, status ACTIVE và mã hóa password")
    void adminCreateUser_AlwaysAssignRoleUserAndStatusActive() {
        UserAdminCreateForm form = new UserAdminCreateForm();
        form.setUsername("created_by_admin");
        form.setEmail("created_by_admin@iotstar.vn");
        form.setFullName("User Created By Admin");
        form.setPassword("AdminSecret123");
        form.setPhone("0919998887");

        User created = userService.createByAdmin(form);

        assertThat(created.getId()).isPositive();
        assertThat(created.getRole()).isEqualTo(Role.USER);
        assertThat(created.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(created.getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(created.getOtpCode()).isNull();
        assertThat(passwordEncoder.matches("AdminSecret123", created.getPassword())).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Cố tình toggle status tài khoản ADMIN phải bị từ chối")
    void adminToggleStatus_OnAdminAccount_ShouldBeRejected() {
        User admin = userRepository.findByRole(Role.ADMIN).orElseThrow();

        assertThatThrownBy(() -> userService.toggleStatus(admin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("quản trị viên");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Cố tình xóa tài khoản ADMIN phải bị từ chối")
    void adminDeleteUser_OnAdminAccount_ShouldBeRejected() {
        User admin = userRepository.findByRole(Role.ADMIN).orElseThrow();

        assertThatThrownBy(() -> userService.delete(admin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("quản trị viên");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin tìm kiếm người dùng theo tham số q match bất kỳ trường nào")
    void adminSearchUsers_HttpParamQ_ShouldMatchAnyField() {
        UserAdminCreateForm u1 = new UserAdminCreateForm();
        u1.setUsername("search_u1");
        u1.setEmail("u1@iotstar.vn");
        u1.setFullName("Nguyen Quoc Anh");
        u1.setPassword("123456");
        u1.setPhone("0977112233");
        userService.createByAdmin(u1);

        Page<User> result = userService.search("Quoc Anh", null, null, PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("search_u1");
    }
}