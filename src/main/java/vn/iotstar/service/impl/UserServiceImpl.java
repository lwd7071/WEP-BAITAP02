package vn.iotstar.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dao.IUserDao;
import vn.iotstar.dao.impl.UserDao;
import vn.iotstar.dto.UserAdminCreateForm;
import vn.iotstar.dto.UserAdminUpdateForm;
import vn.iotstar.dto.UserDto;
import vn.iotstar.entity.AuthProvider;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserStatus;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.IUserService;
import vn.iotstar.service.UserService;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserServiceImpl implements UserService, IUserService {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE = Pattern.compile("^0\\d{9}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IUserDao userDao;

    public UserServiceImpl() {
        this(new UserDao());
    }

    public UserServiceImpl(IUserDao userDao) {
        this.userRepository = null;
        this.passwordEncoder = null;
        this.userDao = userDao;
    }

    @org.springframework.beans.factory.annotation.Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDao = new UserDao();
    }

    // ==========================================
    // Spring Boot UserService Implementation
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public Page<User> search(String keyword, UserStatus status, AuthProvider provider, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate usernameMatch = cb.like(cb.lower(root.get("username")), pattern);
                Predicate fullNameMatch = cb.like(cb.lower(root.get("fullName")), pattern);
                Predicate emailMatch = cb.like(cb.lower(root.get("email")), pattern);
                Predicate phoneMatch = cb.like(cb.lower(root.get("phone")), pattern);
                predicates.add(cb.or(usernameMatch, fullNameMatch, emailMatch, phoneMatch));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (provider != null) {
                predicates.add(cb.equal(root.get("provider"), provider));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable sortedPageable = pageable;
        if (pageable.getSort().isUnsorted()) {
            sortedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdDate", "id")
            );
        }

        return userRepository.findAll(spec, sortedPageable);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findOptionalById(int id) {
        return userRepository.findById(id);
    }

    @Override
    public User createByAdmin(UserAdminCreateForm form) {
        if (userRepository.existsByUsernameIgnoreCase(form.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại: " + form.getUsername());
        }
        if (userRepository.existsByEmailIgnoreCase(form.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại: " + form.getEmail());
        }
        if (form.getPhone() != null && !form.getPhone().isBlank() && userRepository.existsByPhone(form.getPhone().trim())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại: " + form.getPhone());
        }

        User user = new User();
        user.setUsername(form.getUsername().trim());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setFullName(form.getFullName().trim());
        user.setPhone(form.getPhone() != null && !form.getPhone().isBlank() ? form.getPhone().trim() : null);
        user.setAvatar(form.getAvatar());
        user.setPassword(passwordEncoder.encode(form.getPassword()));

        // Bất biến: tài khoản do admin tạo luôn là role USER và trạng thái ACTIVE
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setProvider(AuthProvider.LOCAL);
        user.setProviderId(null);
        user.setOtpCode(null);
        user.setCreatedDate(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public User updateByAdmin(int id, UserAdminUpdateForm form) {
        User user = findById(id);

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(form.getEmail(), id)) {
            throw new IllegalArgumentException("Email đã tồn tại: " + form.getEmail());
        }
        if (form.getPhone() != null && !form.getPhone().isBlank() && userRepository.existsByPhoneAndIdNot(form.getPhone().trim(), id)) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại: " + form.getPhone());
        }

        user.setFullName(form.getFullName().trim());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setPhone(form.getPhone() != null && !form.getPhone().isBlank() ? form.getPhone().trim() : null);
        if (form.getAvatar() != null && !form.getAvatar().isBlank()) {
            user.setAvatar(form.getAvatar());
        }

        return userRepository.save(user);
    }

    @Override
    public void toggleStatus(int id) {
        User user = findById(id);
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Không thể khóa hoặc mở khóa tài khoản quản trị viên");
        }
        user.setStatus(user.getStatus() == UserStatus.ACTIVE ? UserStatus.BLOCKED : UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Override
    public void updateStatus(int id, UserStatus status) {
        User user = findById(id);
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Không thể thay đổi trạng thái tài khoản quản trị viên");
        }
        user.setStatus(status);
        userRepository.save(user);
    }

    @Override
    public void delete(int id) {
        User user = findById(id);
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Không thể xóa tài khoản quản trị viên");
        }
        userRepository.delete(user);
    }

    @Override
    public UserDto toDto(User user) {
        return UserDto.fromEntity(user);
    }

    // ==========================================
    // Legacy IUserService Implementation
    // ==========================================

    @Override
    public User login(String username, String password) {
        User user = findByUsername(username);
        if (user != null && password != null && password.equals(user.getPassword())) {
            if (!user.isActive()) {
                throw new IllegalStateException("Tài khoản chưa được kích hoạt. Vui lòng xác thực OTP qua email.");
            }
            return user;
        }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return email == null || email.isBlank() ? null : userDao.findByEmail(email.trim());
    }

    @Override
    public boolean register(String email, String password, String username, String fullName, String phone) {
        validateRegistration(email, password, username, fullName, phone);
        if (checkExistEmail(email) || checkExistUsername(username) || checkExistPhone(phone)) {
            return false;
        }
        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        User user = new User(email.trim(), username.trim(), fullName.trim(), password,
                null, 3, normalize(phone), LocalDateTime.now(), 0, otp, expiry);
        userDao.insert(user);
        if (!vn.iotstar.util.EmailUtil.sendOtpEmail(user.getEmail(), user.getFullName(), otp, "Kích hoạt tài khoản")) {
            throw new IllegalArgumentException("Không thể gửi email OTP. Vui lòng kiểm tra cấu hình SMTP hoặc thử gửi lại.");
        }
        return true;
    }

    @Override
    public boolean activateUser(String email, String otp) {
        if (email == null || email.isBlank() || otp == null || otp.isBlank()) {
            return false;
        }
        User user = userDao.findByEmail(email.trim());
        if (user == null || user.isActive()) {
            return false;
        }
        if (user.getCode() == null || !user.getCode().equals(otp.trim())) {
            throw new IllegalArgumentException("Mã OTP không chính xác");
        }
        if (user.getOtpExpiry() == null || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn, vui lòng yêu cầu mã mới");
        }
        userDao.updateStatusAndCode(user.getId(), 1, null);
        return true;
    }

    @Override
    public boolean resendRegistrationOtp(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        User user = userDao.findByEmail(email.trim());
        if (user == null || user.isActive()) {
            return false;
        }
        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        userDao.updateOtp(user.getId(), otp, expiry);
        if (!vn.iotstar.util.EmailUtil.sendOtpEmail(user.getEmail(), user.getFullName(), otp, "Kích hoạt tài khoản")) {
            throw new IllegalArgumentException("Không thể gửi email OTP. Vui lòng kiểm tra cấu hình SMTP hoặc thử lại.");
        }
        return true;
    }

    @Override
    public boolean sendForgotPasswordOtp(String emailOrUsername) {
        if (emailOrUsername == null || emailOrUsername.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập email hoặc tài khoản");
        }
        User user = userDao.findByEmail(emailOrUsername.trim());
        if (user == null) {
            user = userDao.findByUsername(emailOrUsername.trim());
        }
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản với thông tin đã cung cấp");
        }
        if (!user.isActive()) {
            throw new IllegalArgumentException("Tài khoản chưa được kích hoạt. Vui lòng xác thực OTP qua email trước.");
        }
        issuePasswordResetOtp(user);
        return true;
    }

    @Override
    public String requestPasswordReset(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tên tài khoản trước khi chọn quên mật khẩu");
        }
        User user = userDao.findByUsername(username.trim());
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản với tên đã nhập");
        }
        if (!user.isActive()) {
            throw new IllegalArgumentException("Tài khoản chưa được kích hoạt. Vui lòng xác thực OTP qua email trước.");
        }
        issuePasswordResetOtp(user);
        return user.getEmail();
    }

    private void issuePasswordResetOtp(User user) {
        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        userDao.updateOtp(user.getId(), otp, expiry);
        if (!vn.iotstar.util.EmailUtil.sendOtpEmail(user.getEmail(), user.getFullName(), otp, "Đặt lại mật khẩu")) {
            throw new IllegalArgumentException("Không thể gửi email OTP. Vui lòng kiểm tra cấu hình SMTP hoặc thử lại.");
        }
    }

    @Override
    public boolean resetPassword(String email, String otp, String newPassword) {
        if (email == null || email.isBlank() || otp == null || otp.isBlank() || newPassword == null) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin");
        }
        if (newPassword.length() < 4) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 4 ký tự");
        }
        User user = userDao.findByEmail(email.trim());
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản với email này");
        }
        if (user.getCode() == null || !user.getCode().equals(otp.trim())) {
            throw new IllegalArgumentException("Mã OTP không chính xác");
        }
        if (user.getOtpExpiry() == null || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn, vui lòng gửi lại mã mới");
        }
        userDao.updatePassword(user.getId(), newPassword);
        return true;
    }

    @Override
    public boolean resetPassword(int userId, String otp, String newPassword) {
        if (otp == null || otp.isBlank() || newPassword == null) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin");
        }
        if (newPassword.length() < 4) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 4 ký tự");
        }
        User user = userDao.findById(userId);
        if (user == null || !user.isActive()) {
            throw new IllegalArgumentException("Tài khoản không hợp lệ để đặt lại mật khẩu");
        }
        if (user.getCode() == null || !user.getCode().equals(otp.trim())) {
            throw new IllegalArgumentException("Mã OTP không chính xác");
        }
        if (user.getOtpExpiry() == null || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn, vui lòng gửi lại mã mới");
        }
        userDao.updatePassword(user.getId(), newPassword);
        return true;
    }

    private String generateOtp() {
        return String.format("%06d", new java.security.SecureRandom().nextInt(1000000));
    }

    @Override
    public boolean checkExistEmail(String email) {
        return userDao.existsByEmail(email);
    }

    @Override
    public boolean checkExistUsername(String username) {
        return userDao.existsByUsername(username);
    }

    @Override
    public boolean checkExistPhone(String phone) {
        return userDao.existsByPhone(phone);
    }

    @Override
    public boolean checkExistPhoneForUser(String phone, int userId) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        return userDao.existsByPhoneAndNotId(phone.trim(), userId);
    }

    @Override
    public User updateProfile(int userId, String fullName, String phone, String avatar) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        String trimmedFullName = fullName.trim();
        if (trimmedFullName.length() < 2 || trimmedFullName.length() > 50) {
            throw new IllegalArgumentException("Họ tên phải từ 2 đến 50 ký tự");
        }
        String normalizedPhone = normalize(phone);
        if (normalizedPhone != null) {
            if (!PHONE.matcher(normalizedPhone).matches()) {
                throw new IllegalArgumentException("Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0");
            }
            if (checkExistPhoneForUser(normalizedPhone, userId)) {
                throw new IllegalArgumentException("Số điện thoại đã được sử dụng");
            }
        }
        return userDao.updateProfile(userId, trimmedFullName, normalizedPhone, normalize(avatar));
    }

    private void validateRegistration(String email, String password, String username, String fullName, String phone) {
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Tài khoản phải có ít nhất 3 ký tự");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 4 ký tự");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        String trimmedFullName = fullName.trim();
        if (trimmedFullName.length() < 2 || trimmedFullName.length() > 50) {
            throw new IllegalArgumentException("Họ tên phải từ 2 đến 50 ký tự");
        }
        if (phone != null && !phone.isBlank() && !PHONE.matcher(phone.trim()).matches()) {
            throw new IllegalArgumentException("Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}