package vn.iotstar.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.AuthProvider;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserStatus;
import vn.iotstar.repository.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PasswordMigrationRunnerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordMigrationRunner passwordMigrationRunner;

    @Test
    @DisplayName("Chuyển đổi mật khẩu plaintext thành BCrypt an toàn và chính xác")
    void plaintextPassword_ShouldBeConvertedToBcrypt() throws Exception {
        User legacyUser = new User();
        legacyUser.setUsername("legacy_plain_user");
        legacyUser.setEmail("plain@iotstar.vn");
        legacyUser.setFullName("Plain Password User");
        legacyUser.setPassword("plainText123");
        legacyUser.setRole(Role.USER);
        legacyUser.setStatus(UserStatus.ACTIVE);
        legacyUser.setProvider(AuthProvider.LOCAL);
        legacyUser.setCreatedDate(LocalDateTime.now());
        legacyUser = userRepository.saveAndFlush(legacyUser);

        passwordMigrationRunner.migratePasswords();

        User migratedUser = userRepository.findById(legacyUser.getId()).orElseThrow();
        assertThat(migratedUser.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches("plainText123", migratedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Mật khẩu đã được mã hóa BCrypt giữ nguyên, không bị hash 2 lần")
    void existingBcryptPassword_ShouldRemainUnchanged() throws Exception {
        String originalBcrypt = passwordEncoder.encode("AlreadyBcrypt123");

        User bcryptUser = new User();
        bcryptUser.setUsername("bcrypt_user");
        bcryptUser.setEmail("bcrypt@iotstar.vn");
        bcryptUser.setFullName("BCrypt User");
        bcryptUser.setPassword(originalBcrypt);
        bcryptUser.setRole(Role.USER);
        bcryptUser.setStatus(UserStatus.ACTIVE);
        bcryptUser.setProvider(AuthProvider.LOCAL);
        bcryptUser.setCreatedDate(LocalDateTime.now());
        bcryptUser = userRepository.saveAndFlush(bcryptUser);

        passwordMigrationRunner.migratePasswords();

        User checkedUser = userRepository.findById(bcryptUser.getId()).orElseThrow();
        assertThat(checkedUser.getPassword()).isEqualTo(originalBcrypt);
        assertThat(passwordEncoder.matches("AlreadyBcrypt123", checkedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Khi cờ migration tắt (disabled), runner không tự động chạy chuyển đổi")
    void whenMigrationDisabled_ShouldNotRun() {
        User unmigrated = new User();
        unmigrated.setUsername("unmigrated_user");
        unmigrated.setEmail("unmigrated@iotstar.vn");
        unmigrated.setFullName("Unmigrated User");
        unmigrated.setPassword("stayPlain123");
        unmigrated.setRole(Role.USER);
        unmigrated.setStatus(UserStatus.ACTIVE);
        unmigrated.setProvider(AuthProvider.LOCAL);
        unmigrated.setCreatedDate(LocalDateTime.now());
        unmigrated = userRepository.saveAndFlush(unmigrated);

        passwordMigrationRunner.setMigrationEnabled(false);
        passwordMigrationRunner.run();

        User checked = userRepository.findById(unmigrated.getId()).orElseThrow();
        assertThat(checked.getPassword()).isEqualTo("stayPlain123");
    }
}