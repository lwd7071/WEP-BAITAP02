package vn.iotstar.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.AuthProvider;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserStatus;
import vn.iotstar.repository.UserRepository;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class OAuth2LoginTest {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("User mới đăng nhập bằng Google tự động tạo tài khoản role USER và status ACTIVE")
    void googleLogin_NewUser_ShouldCreateUserRoleWithStatusActive() {
        Map<String, Object> attributes = Map.of(
                "sub", "google_sub_001",
                "email", "fresh_google_user@gmail.com",
                "name", "Fresh Google User",
                "picture", "https://example.com/avatar.jpg"
        );

        User user = customOAuth2UserService.processOAuth2User(attributes);

        assertThat(user).isNotNull();
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(user.getProviderId()).isEqualTo("google_sub_001");
        assertThat(user.getEmail()).isEqualTo("fresh_google_user@gmail.com");

        User inDb = userRepository.findByEmailIgnoreCase("fresh_google_user@gmail.com").orElseThrow();
        assertThat(inDb.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("Email Google trùng với Admin Local bị từ chối tuyệt đối để chống cướp quyền")
    void googleLogin_ExistingAdminEmail_ShouldRejectAndNotGrantAdmin() {
        User admin = userRepository.findByRole(Role.ADMIN).orElseThrow();

        Map<String, Object> attributes = Map.of(
                "sub", "malicious_google_sub",
                "email", admin.getEmail(),
                "name", "Attacker Admin",
                "picture", "https://example.com/attacker.jpg"
        );

        assertThatThrownBy(() -> customOAuth2UserService.processOAuth2User(attributes))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }

    @Test
    @DisplayName("User Google có tài khoản bị BLOCKED sẽ bị từ chối đăng nhập")
    void googleLogin_BlockedUser_ShouldReject() {
        User blockedUser = new User();
        blockedUser.setUsername("blocked_oauth_user");
        blockedUser.setEmail("blocked_oauth@gmail.com");
        blockedUser.setFullName("Blocked User");
        blockedUser.setRole(Role.USER);
        blockedUser.setProvider(AuthProvider.GOOGLE);
        blockedUser.setProviderId("blocked_sub_999");
        blockedUser.setStatus(UserStatus.BLOCKED);
        userRepository.save(blockedUser);

        Map<String, Object> attributes = Map.of(
                "sub", "blocked_sub_999",
                "email", "blocked_oauth@gmail.com",
                "name", "Blocked User",
                "picture", "https://example.com/blocked.jpg"
        );

        assertThatThrownBy(() -> customOAuth2UserService.processOAuth2User(attributes))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }
}