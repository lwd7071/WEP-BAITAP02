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
        return user != null && password != null && password.equals(user.getPassword()) ? user : null;
    }

    @Override
    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public boolean register(String email, String password, String username, String fullName, String phone) {
        validateRegistration(email, password, username, fullName, phone);
        if (checkExistEmail(email) || checkExistUsername(username) || checkExistPhone(phone)) {
            return false;
        }
        User user = new User(email.trim(), username.trim(), fullName.trim(), password,
                null, 3, normalize(phone), LocalDateTime.now());
        userDao.insert(user);
        return true;
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
