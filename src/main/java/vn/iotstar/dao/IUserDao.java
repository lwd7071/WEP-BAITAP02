package vn.iotstar.dao;

import vn.iotstar.entity.User;

public interface IUserDao {
    void insert(User user);
    User findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
}
