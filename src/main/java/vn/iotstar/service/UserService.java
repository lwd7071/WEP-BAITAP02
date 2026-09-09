package vn.iotstar.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.iotstar.dto.UserAdminCreateForm;
import vn.iotstar.dto.UserAdminUpdateForm;
import vn.iotstar.dto.UserDto;
import vn.iotstar.entity.AuthProvider;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserStatus;

import java.util.Optional;

public interface UserService {

    Page<User> search(String keyword, UserStatus status, AuthProvider provider, Pageable pageable);

    User findById(int id);

    Optional<User> findOptionalById(int id);

    User createByAdmin(UserAdminCreateForm form);

    User updateByAdmin(int id, UserAdminUpdateForm form);

    void toggleStatus(int id);

    void updateStatus(int id, UserStatus status);

    void delete(int id);

    UserDto toDto(User user);
}