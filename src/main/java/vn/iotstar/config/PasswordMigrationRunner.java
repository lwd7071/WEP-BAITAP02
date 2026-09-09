package vn.iotstar.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;

import java.util.List;

@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordMigrationRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.migration.password.enabled:false}")
    private boolean migrationEnabled;

    public PasswordMigrationRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!migrationEnabled) {
            LOGGER.info("Password migration is disabled (app.migration.password.enabled=false). Skipping.");
            return;
        }
        migratePasswords();
    }

    @Transactional
    public int migratePasswords() {
        LOGGER.info("Bắt đầu quá trình quét và chuyển đổi mật khẩu plaintext sang BCrypt...");
        List<User> users = userRepository.findAll();
        int convertedCount = 0;

        for (User user : users) {
            String pwd = user.getPassword();
            if (pwd == null || pwd.isBlank()) {
                continue;
            }

            // Kiểm tra xem mật khẩu đã được mã hóa BCrypt chuẩn chưa ($2a$, $2b$, $2y$)
            boolean isAlreadyBcrypt = pwd.startsWith("$2a$") || pwd.startsWith("$2b$") || pwd.startsWith("$2y$");
            if (!isAlreadyBcrypt) {
                user.setPassword(passwordEncoder.encode(pwd));
                userRepository.save(user);
                convertedCount++;
                LOGGER.info("Đã chuyển đổi mật khẩu sang BCrypt cho tài khoản: {}", user.getUsername());
            }
        }

        LOGGER.info("Hoàn tất chuyển đổi mật khẩu sang BCrypt. Tổng số tài khoản đã chuyển đổi: {}", convertedCount);
        return convertedCount;
    }

    public boolean isMigrationEnabled() {
        return migrationEnabled;
    }

    public void setMigrationEnabled(boolean migrationEnabled) {
        this.migrationEnabled = migrationEnabled;
    }
}