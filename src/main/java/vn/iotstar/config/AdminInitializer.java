package vn.iotstar.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.iotstar.entity.AuthProvider;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserStatus;
import vn.iotstar.repository.UserRepository;

import java.time.LocalDateTime;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.email:admin@iotstar.vn}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123456}")
    private String adminPassword;

    public AdminInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public void run(String... args) {
        long adminCount = userRepository.countByRole(Role.ADMIN);

        if (adminCount > 1) {
            String errorMsg = "Phát hiện nhiều hơn 1 tài khoản ADMIN trong dữ liệu (" + adminCount + " accounts)! Dừng ứng dụng để bảo vệ tính toàn vẹn dữ liệu.";
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        if (adminCount == 0) {
            log.info("Chưa có tài khoản ADMIN trong hệ thống. Đang tạo tài khoản ADMIN mặc định...");
            String encodedPassword = isBcrypt(adminPassword) ? adminPassword : passwordEncoder.encode(adminPassword);

            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setFullName("Hệ Thống Quản Trị");
            admin.setPassword(encodedPassword);
            admin.setRole(Role.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            admin.setProvider(AuthProvider.LOCAL);
            admin.setCreatedDate(LocalDateTime.now());
            admin.setUpdatedDate(LocalDateTime.now());

            userRepository.save(admin);
            log.info("Tạo tài khoản ADMIN thành công: username={}, email={}", adminUsername, adminEmail);
        } else {
            log.info("Đã có đúng 1 tài khoản ADMIN trong hệ thống. Khởi tạo hoàn tất.");
        }
    }

    private boolean isBcrypt(String password) {
        return password != null && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }
}
