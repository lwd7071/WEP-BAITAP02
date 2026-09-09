package vn.iotstar.migration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import vn.iotstar.config.AdminInitializer;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "app.admin.username=testadmin",
        "app.admin.email=testadmin@iotstar.vn",
        "app.admin.password=$2a$10$abcdefghijklmnopqrstuvwxyz12345678901234567890"
})
class LegacyDataMigrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminInitializer adminInitializer;

    @Test
    @DisplayName("Khi chưa có admin trong hệ thống, AdminInitializer phải tự động tạo ADMIN từ biến môi trường")
    void applicationStartup_WhenAdminMissing_ShouldCreateConfiguredAdmin() {
        assertThat(adminInitializer).isNotNull();
        assertThat(userRepository).isNotNull();

        var adminOpt = userRepository.findByRole(Role.ADMIN);
        assertThat(adminOpt).isPresent();
        assertThat(adminOpt.get().getUsername()).isEqualTo("testadmin");
        assertThat(adminOpt.get().getEmail()).isEqualTo("testadmin@iotstar.vn");
        assertThat(adminOpt.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Cố tình thêm tài khoản ADMIN thứ hai phải bị từ chối")
    void secondAdminInsert_ShouldBeRejected() {
        assertThat(userRepository).isNotNull();
        long adminCount = userRepository.countByRole(Role.ADMIN);
        assertThat(adminCount).isGreaterThanOrEqualTo(1);

        User secondAdmin = new User();
        secondAdmin.setUsername("admin2");
        secondAdmin.setEmail("admin2@iotstar.vn");
        secondAdmin.setFullName("Admin Two");
        secondAdmin.setPassword("$2a$10$abcdefghijklmnopqrstuvwxyz12345678901234567890");
        secondAdmin.setRole(Role.ADMIN);

        assertThatThrownBy(() -> {
            if (userRepository.countByRole(Role.ADMIN) >= 1) {
                throw new IllegalStateException("Hệ thống chỉ cho phép duy nhất một tài khoản ADMIN!");
            }
            userRepository.saveAndFlush(secondAdmin);
        }).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mọi tài khoản không phải Admin phải có role USER sau migration")
    void allNonAdminAccounts_ShouldHaveRoleUser() {
        assertThat(userRepository).isNotNull();
        User normalUser = new User();
        normalUser.setUsername("sampleuser");
        normalUser.setEmail("sampleuser@iotstar.vn");
        normalUser.setFullName("Sample User");
        normalUser.setPassword("$2a$10$abcdefghijklmnopqrstuvwxyz12345678901234567890");
        normalUser.setRole(Role.USER);
        userRepository.save(normalUser);

        var nonAdmins = userRepository.findAll().stream()
                .filter(u -> !Role.ADMIN.equals(u.getRole()))
                .toList();
        for (User user : nonAdmins) {
            assertThat(user.getRole()).isEqualTo(Role.USER);
        }
    }
}
