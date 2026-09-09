package vn.iotstar.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.AuthProvider;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User u1 = new User();
        u1.setUsername("nguyenvana");
        u1.setEmail("vana@gmail.com");
        u1.setFullName("Nguyen Van A");
        u1.setPhone("0912345678");
        u1.setPassword("pass1");
        u1.setRole(Role.USER);
        u1.setStatus(UserStatus.ACTIVE);
        u1.setProvider(AuthProvider.LOCAL);
        entityManager.persist(u1);

        User u2 = new User();
        u2.setUsername("tranthib");
        u2.setEmail("thib@gmail.com");
        u2.setFullName("Tran Thi B");
        u2.setPhone("0987654321");
        u2.setPassword("pass2");
        u2.setRole(Role.USER);
        u2.setStatus(UserStatus.PENDING);
        u2.setProvider(AuthProvider.GOOGLE);
        entityManager.persist(u2);

        entityManager.flush();
    }

    @Test
    @DisplayName("Tìm kiếm user match không phân biệt hoa thường theo username, fullName, email hoặc phone")
    void searchUser_ShouldMatchUsernameNameEmailOrPhone() {
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate", "id"));

        Specification<User> specPhone = (root, query, cb) ->
                cb.equal(root.get("phone"), "0912345678");
        Page<User> resultPhone = userRepository.findAll(specPhone, pageable);
        assertThat(resultPhone.getContent()).hasSize(1);
        assertThat(resultPhone.getContent().get(0).getUsername()).isEqualTo("nguyenvana");

        Specification<User> specEmail = (root, query, cb) ->
                cb.like(cb.lower(root.get("email")), "%thib%");
        Page<User> resultEmail = userRepository.findAll(specEmail, pageable);
        assertThat(resultEmail.getContent()).hasSize(1);
        assertThat(resultEmail.getContent().get(0).getFullName()).isEqualTo("Tran Thi B");
    }

    @Test
    @DisplayName("Tìm kiếm danh sách người dùng cho Admin không bao gồm tài khoản Role ADMIN")
    void searchUser_ShouldNotReturnAdminAccount() {
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate", "id"));
        Specification<User> specOnlyUsers = (root, query, cb) ->
                cb.equal(root.get("role"), Role.USER);

        Page<User> result = userRepository.findAll(specOnlyUsers, pageable);
        assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result.getContent()).allMatch(u -> u.getRole() == Role.USER);
    }
}