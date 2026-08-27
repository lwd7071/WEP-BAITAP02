package vn.iotstar.service.impl;

import vn.iotstar.dao.IUserDao;
import vn.iotstar.dao.impl.UserDao;
import vn.iotstar.entity.User;
import vn.iotstar.service.IUserService;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class UserServiceImpl implements IUserService {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
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
        validateRegistration(email, password, username, fullName);
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

    private void validateRegistration(String email, String password, String username, String fullName) {
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
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
