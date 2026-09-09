package vn.iotstar.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.RegisterRequestDto;
import vn.iotstar.entity.AuthProvider;
import vn.iotstar.entity.OtpPurpose;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserStatus;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.EmailService;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    private String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    @Override
    public User register(RegisterRequestDto form) {
        if (form.getUsername() == null || form.getUsername().isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        }
        if (userRepository.existsByUsernameIgnoreCase(form.getUsername().trim())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trong hệ thống");
        }
        if (form.getEmail() == null || form.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (userRepository.existsByEmailIgnoreCase(form.getEmail().trim())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }
        if (form.getPhone() != null && !form.getPhone().isBlank()) {
            if (userRepository.existsByPhone(form.getPhone().trim())) {
                throw new IllegalArgumentException("Số điện thoại đã được sử dụng");
            }
        }

        User user = new User();
        user.setUsername(form.getUsername().trim());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setFullName(form.getFullName().trim());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setPhone(form.getPhone() != null && !form.getPhone().isBlank() ? form.getPhone().trim() : null);

        // Bất biến: luôn USER, LOCAL, PENDING
        user.setRole(Role.USER);
        user.setProvider(AuthProvider.LOCAL);
        user.setStatus(UserStatus.PENDING);

        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        user.setOtpPurpose(OtpPurpose.ACTIVATION);

        User saved = userRepository.save(user);
        emailService.sendOtpEmail(saved.getEmail(), saved.getFullName(), otp, "Mã kích hoạt tài khoản");
        return saved;
    }

    @Override
    public boolean activate(String email, String otp) {
        if (email == null || email.isBlank() || otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("Email và mã OTP không được để trống");
        }
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        if (user.getStatus() == UserStatus.ACTIVE) {
            return true;
        }
        if (user.getOtpPurpose() != OtpPurpose.ACTIVATION) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ cho mục đích kích hoạt");
        }
        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp.trim())) {
            throw new IllegalArgumentException("Mã OTP không chính xác");
        }
        if (user.getOtpExpiry() == null || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn, vui lòng yêu cầu mã mới");
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        user.setOtpPurpose(null);
        userRepository.save(user);
        return true;
    }

    @Override
    public boolean resendActivationOtp(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ có thể gửi lại mã kích hoạt cho tài khoản đang chờ kích hoạt");
        }

        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        user.setOtpPurpose(OtpPurpose.ACTIVATION);
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp, "Gửi lại mã kích hoạt tài khoản");
        return true;
    }

    @Override
    public Integer requestPasswordReset(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập");
        }
        User user = userRepository.findByUsernameIgnoreCase(username.trim())
                .or(() -> userRepository.findByEmailIgnoreCase(username.trim()))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Tài khoản chưa được kích hoạt hoặc đã bị khóa");
        }

        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        user.setOtpPurpose(OtpPurpose.PASSWORD_RESET);
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp, "Mã xác thực đặt lại mật khẩu");
        return user.getId();
    }

    @Override
    public boolean resetPassword(Integer userId, String otp, String newPassword) {
        if (userId == null || otp == null || otp.isBlank() || newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Thông tin đặt lại mật khẩu không hợp lệ");
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có tối thiểu 6 ký tự");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        if (user.getOtpPurpose() != OtpPurpose.PASSWORD_RESET) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ cho đặt lại mật khẩu");
        }
        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp.trim())) {
            throw new IllegalArgumentException("Mã OTP không chính xác");
        }
        if (user.getOtpExpiry() == null || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn, vui lòng gửi lại yêu cầu");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        user.setOtpPurpose(null);
        userRepository.save(user);
        return true;
    }
}