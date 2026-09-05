package vn.iotstar.service.impl;

import vn.iotstar.dao.IUserDao;
import vn.iotstar.dao.impl.UserDao;
import vn.iotstar.entity.User;
import vn.iotstar.service.IUserService;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class UserServiceImpl implements IUserService {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE = Pattern.compile("^0\\d{9}$");
    private final IUserDao userDao;

    public UserServiceImpl() {
        this(new UserDao());
    }

    public UserServiceImpl(IUserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User login(String username, String password) {
        User user = findByUsername(username);
        if (user != null && password != null && password.equals(user.getPassword())) {
            if (user.getStatus() == 0) {
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
        vn.iotstar.util.EmailUtil.sendOtpEmail(user.getEmail(), user.getFullName(), otp, "Kích hoạt tài khoản");
        return true;
    }

    @Override
    public boolean activateUser(String email, String otp) {
        if (email == null || email.isBlank() || otp == null || otp.isBlank()) {
            return false;
        }
        User user = userDao.findByEmail(email.trim());
        if (user == null || user.getStatus() == 1) {
            return false;
        }
        if (user.getCode() == null || !user.getCode().equals(otp.trim())) {
            throw new IllegalArgumentException("Mã OTP không chính xác");
        }
        if (user.getOtpExpiry() != null && LocalDateTime.now().isAfter(user.getOtpExpiry())) {
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
        if (user == null || user.getStatus() == 1) {
            return false;
        }
        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        userDao.updateOtp(user.getId(), otp, expiry);
        vn.iotstar.util.EmailUtil.sendOtpEmail(user.getEmail(), user.getFullName(), otp, "Kích hoạt tài khoản");
        return true;
    }

    @Override
    public boolean sendForgotPasswordOtp(String emailOrUsername) {
        if (emailOrUsername == null || emailOrUsername.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập email hoặc tài khoản");
        }
        String target = emailOrUsername.trim();
        User user = userDao.findByEmail(target);
        if (user == null) {
            user = userDao.findByUsername(target);
        }
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản với thông tin đã cung cấp");
        }
        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        userDao.updateOtp(user.getId(), otp, expiry);
        vn.iotstar.util.EmailUtil.sendOtpEmail(user.getEmail(), user.getFullName(), otp, "Đặt lại mật khẩu");
        return true;
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
        if (user.getOtpExpiry() != null && LocalDateTime.now().isAfter(user.getOtpExpiry())) {
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
