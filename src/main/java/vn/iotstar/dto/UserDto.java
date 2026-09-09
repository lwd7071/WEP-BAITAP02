package vn.iotstar.dto;

import vn.iotstar.entity.AuthProvider;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO an toàn cho người dùng, không chứa mật khẩu hay mã OTP
 */
public class UserDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private String email;
    private String fullName;
    private String avatar;
    private String phone;
    private Role role;
    private UserStatus status;
    private AuthProvider provider;
    private String providerId;
    private LocalDateTime createdDate;

    public UserDto() {
    }

    public static UserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setAvatar(user.getAvatar());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setProvider(user.getProvider());
        dto.setProviderId(user.getProviderId());
        dto.setCreatedDate(user.getCreatedDate());
        return dto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public void setProvider(AuthProvider provider) {
        this.provider = provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}