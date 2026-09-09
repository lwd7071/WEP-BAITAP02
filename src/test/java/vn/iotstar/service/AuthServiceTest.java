package vn.iotstar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.RegisterRequestDto;
import vn.iotstar.entity.AuthProvider;
import vn.iotstar.entity.OtpPurpose;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserStatus;
import vn.iotstar.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Đăng ký tài khoản local luôn gán role USER, status PENDING và hash BCrypt password")
    void register_AlwaysAssignRoleUserAndStatusPending() {
        RegisterRequestDto form = new RegisterRequestDto();
        form.setUsername("newuser_test");
        form.setEmail("newuser_test@iotstar.vn");
        form.setFullName("New User Test");
        form.setPassword("plainPassword123");
        form.setPhone("0981112223");

        authService.register(form);

        User saved = userRepository.findByUsernameIgnoreCase("newuser_test").orElseThrow();
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(saved.getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(saved.getOtpCode()).isNotBlank();
        assertThat(saved.getOtpPurpose()).isEqualTo(OtpPurpose.ACTIVATION);
        assertThat(saved.getOtpExpiry()).isNotNull();
        assertThat(passwordEncoder.matches("plainPassword123", saved.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Kích hoạt tài khoản bằng đúng OTP chuyển status thành ACTIVE và giữ role USER")
    void activate_ValidOtp_ShouldActivateAccountAndPreserveRoleUser() {
        RegisterRequestDto form = new RegisterRequestDto();
        form.setUsername("activate_user");
        form.setEmail("activate_user@iotstar.vn");
        form.setFullName("Activate User");
        form.setPassword("pass123");
        form.setPhone("0983334445");
        authService.register(form);

        User pending = userRepository.findByUsernameIgnoreCase("activate_user").orElseThrow();
        String otp = pending.getOtpCode();

        authService.activate(pending.getEmail(), otp);

        User activated = userRepository.findByUsernameIgnoreCase("activate_user").orElseThrow();
        assertThat(activated.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(activated.getRole()).isEqualTo(Role.USER);
        assertThat(activated.getOtpCode()).isNull();
        assertThat(activated.getOtpPurpose()).isNull();
    }

    @Test
    @DisplayName("Kích hoạt tài khoản bằng sai mã OTP phải thất bại")
    void activate_InvalidOrExpiredOtp_ShouldFail() {
        RegisterRequestDto form = new RegisterRequestDto();
        form.setUsername("fail_otp_user");
        form.setEmail("fail_otp@iotstar.vn");
        form.setFullName("Fail User");
        form.setPassword("pass123");
        form.setPhone("0985556667");
        authService.register(form);

        assertThatThrownBy(() -> authService.activate("fail_otp@iotstar.vn", "999999"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Gửi lại OTP cấp mã mới nhưng vẫn giữ nguyên status PENDING")
    void resendOtp_ShouldGenerateNewOtpAndPreservePendingStatus() {
        RegisterRequestDto form = new RegisterRequestDto();
        form.setUsername("resend_user");
        form.setEmail("resend_user@iotstar.vn");
        form.setFullName("Resend User");
        form.setPassword("pass123");
        form.setPhone("0987778889");
        authService.register(form);

        User u1 = userRepository.findByUsernameIgnoreCase("resend_user").orElseThrow();
        String firstOtp = u1.getOtpCode();

        authService.resendActivationOtp(u1.getEmail());

        User u2 = userRepository.findByUsernameIgnoreCase("resend_user").orElseThrow();
        assertThat(u2.getOtpCode()).isNotBlank();
        assertThat(u2.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(u2.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("Yêu cầu quên mật khẩu tài khoản ACTIVE tạo OTP mục đích PASSWORD_RESET")
    void requestPasswordReset_ShouldGenerateResetOtp() {
        RegisterRequestDto form = new RegisterRequestDto();
        form.setUsername("reset_user");
        form.setEmail("reset_user@iotstar.vn");
        form.setFullName("Reset User");
        form.setPassword("pass123");
        form.setPhone("0989990001");
        authService.register(form);

        User user = userRepository.findByUsernameIgnoreCase("reset_user").orElseThrow();
        authService.activate(user.getEmail(), user.getOtpCode());

        Integer userId = authService.requestPasswordReset("reset_user");
        assertThat(userId).isNotNull();

        User resetPending = userRepository.findById(userId).orElseThrow();
        assertThat(resetPending.getOtpCode()).isNotBlank();
        assertThat(resetPending.getOtpPurpose()).isEqualTo(OtpPurpose.PASSWORD_RESET);
    }

    @Test
    @DisplayName("Đặt lại mật khẩu với OTP hợp lệ sẽ hash BCrypt mật khẩu mới và giữ role USER")
    void resetPassword_ValidOtp_ShouldUpdateBcryptPasswordAndPreserveRole() {
        RegisterRequestDto form = new RegisterRequestDto();
        form.setUsername("reset_pass_user");
        form.setEmail("reset_pass@iotstar.vn");
        form.setFullName("Reset Pass User");
        form.setPassword("oldPass123");
        form.setPhone("0981239876");
        authService.register(form);

        User user = userRepository.findByUsernameIgnoreCase("reset_pass_user").orElseThrow();
        authService.activate(user.getEmail(), user.getOtpCode());
        Integer userId = authService.requestPasswordReset("reset_pass_user");

        User withOtp = userRepository.findById(userId).orElseThrow();
        String resetOtp = withOtp.getOtpCode();

        authService.resetPassword(userId, resetOtp, "newSecurePassword456");

        User updated = userRepository.findById(userId).orElseThrow();
        assertThat(passwordEncoder.matches("newSecurePassword456", updated.getPassword())).isTrue();
        assertThat(updated.getRole()).isEqualTo(Role.USER);
        assertThat(updated.getOtpCode()).isNull();
        assertThat(updated.getOtpPurpose()).isNull();
    }
}