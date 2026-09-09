package vn.iotstar.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.AuthProvider;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserStatus;
import vn.iotstar.repository.UserRepository;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;

@Service
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        User user = processOAuth2User(oAuth2User.getAttributes());

        String roleName = (user.getRole() == Role.ADMIN) ? "ROLE_ADMIN" : "ROLE_USER";
        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority(roleName)),
                oAuth2User.getAttributes(),
                "email"
        );
    }

    public User processOAuth2User(Map<String, Object> attributes) {
        String sub = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_email", "Email từ tài khoản Google không hợp lệ", null));
        }
        email = email.trim().toLowerCase();

        // 1. Chống cướp quyền Admin tuyệt đối
        var existingByEmail = userRepository.findByEmailIgnoreCase(email);
        if (existingByEmail.isPresent()) {
            User existing = existingByEmail.get();
            if (existing.getRole() == Role.ADMIN) {
                log.warn("Cảnh báo bảo mật: Cố tình đăng nhập Google với email Admin: {}", email);
                throw new OAuth2AuthenticationException(new OAuth2Error(
                        "admin_oauth_forbidden",
                        "Tài khoản Admin không được phép đăng nhập bằng Google. Vui lòng sử dụng đăng nhập nội bộ",
                        null
                ));
            }
            if (existing.getProvider() == AuthProvider.LOCAL) {
                log.warn("Xung đột phương thức đăng nhập: Email {} đã đăng ký bằng tài khoản Local", email);
                throw new OAuth2AuthenticationException(new OAuth2Error(
                        "local_account_exists",
                        "Email này đã được đăng ký bằng tài khoản thường. Vui lòng đăng nhập bằng mật khẩu",
                        null
                ));
            }
        }

        // 2. Tìm theo (provider = GOOGLE, providerId = sub)
        var userBySub = userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, sub);
        if (userBySub.isPresent()) {
            User existingGoogleUser = userBySub.get();
            if (existingGoogleUser.getStatus() == UserStatus.BLOCKED) {
                throw new OAuth2AuthenticationException(new OAuth2Error("user_blocked", "Tài khoản của bạn đã bị khóa", null));
            }
            if (picture != null && !picture.isBlank()) {
                existingGoogleUser.setAvatar(picture);
            }
            return userRepository.save(existingGoogleUser);
        }

        // 3. Người dùng Google mới: Tạo user Role USER, Status ACTIVE, Provider GOOGLE
        String baseUsername = email.contains("@") ? email.substring(0, email.indexOf("@")) : "google_user";
        String uniqueUsername = generateUniqueUsername(baseUsername);

        User newUser = new User();
        newUser.setUsername(uniqueUsername);
        newUser.setEmail(email);
        newUser.setFullName(name != null && !name.isBlank() ? name.trim() : uniqueUsername);
        newUser.setAvatar(picture);
        newUser.setRole(Role.USER); // Bất biến: luôn luôn Role USER
        newUser.setStatus(UserStatus.ACTIVE);
        newUser.setProvider(AuthProvider.GOOGLE);
        newUser.setProviderId(sub);
        newUser.setPassword(null);

        return userRepository.save(newUser);
    }

    private String generateUniqueUsername(String baseUsername) {
        String cleanBase = baseUsername.replaceAll("[^a-zA-Z0-9_]", "");
        if (cleanBase.isBlank()) {
            cleanBase = "user";
        }
        String candidate = cleanBase;
        while (userRepository.existsByUsernameIgnoreCase(candidate)) {
            candidate = cleanBase + "_" + (secureRandom.nextInt(9000) + 1000);
        }
        return candidate;
    }
}