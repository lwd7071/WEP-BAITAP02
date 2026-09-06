package vn.iotstar.dao;

import vn.iotstar.entity.User;

public interface IUserDao {
    void insert(User user);
    User findByUsername(String username);
    User findById(int id);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
    boolean existsByPhoneAndNotId(String phone, int id);
    User updateProfile(int id, String fullName, String phone, String avatar);
    User findByEmail(String email);
    void update(User user);
    void updateOtp(int id, String code, java.time.LocalDateTime expiry);
    void updateStatusAndCode(int id, int status, String code);
    void updatePassword(int id, String newPassword);
}
