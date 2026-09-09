package vn.iotstar.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.UserAdminCreateForm;
import vn.iotstar.dto.UserAdminUpdateForm;
import vn.iotstar.dto.UserDto;
import vn.iotstar.entity.AuthProvider;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserStatus;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.UserService;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
}