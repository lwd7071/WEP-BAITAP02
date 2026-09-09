package vn.iotstar.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @NotBlank
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(length = 255)
    private String password;

    @Column(length = 500)
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "otp_code", length = 10)
    private String otpCode;

    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;

    @Enumerated(EnumType.STRING)
    @Column(name = "otp_purpose", length = 50)
    private OtpPurpose otpPurpose;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    // Cột legacy hỗ trợ tương thích ngược trước khi dọn dẹp
    @Column(name = "role_id", insertable = false, updatable = false)
    private Integer roleId;

    @Column(name = "code", insertable = false, updatable = false, length = 50)
    private String code;

    public User() {
    }

    public User(String email, String username, String fullName, String password,
                String avatar, Role role, String phone, LocalDateTime createdDate) {
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.password = password;
        this.avatar = avatar;
        this.role = role != null ? role : Role.USER;
        this.phone = phone;
        this.createdDate = createdDate != null ? createdDate : LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.status = UserStatus.ACTIVE;
        this.provider = AuthProvider.LOCAL;
    }

    public User(String email, String username, String fullName, String password,
                String avatar, int roleId, String phone, LocalDateTime createdDate) {
        this(email, username, fullName, password, avatar, (roleId == 1 ? Role.ADMIN : Role.USER), phone, createdDate);
    }

    public User(String email, String username, String fullName, String password,
                String avatar, int roleId, String phone, LocalDateTime createdDate,
                int status, String code, LocalDateTime otpExpiry) {
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.password = password;
        this.avatar = avatar;
        this.role = (roleId == 1) ? Role.ADMIN : Role.USER;
        this.phone = phone;
        this.createdDate = createdDate != null ? createdDate : LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.status = (status == 1) ? UserStatus.ACTIVE : UserStatus.PENDING;
        this.provider = AuthProvider.LOCAL;
        this.otpCode = code;
        this.code = code;
        this.otpExpiry = otpExpiry;
    }

    @PrePersist
    void prePersist() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
        if (updatedDate == null) {
            updatedDate = LocalDateTime.now();
        }
        if (role == null) {
            role = Role.USER;
        }
        if (status == null) {
            status = UserStatus.PENDING;
        }
        if (provider == null) {
            provider = AuthProvider.LOCAL;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedDate = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public void setStatus(int legacyStatus) {
        this.status = (legacyStatus == 1) ? UserStatus.ACTIVE : UserStatus.PENDING;
    }

    public AuthProvider getProvider() { return provider; }
    public void setProvider(AuthProvider provider) { this.provider = provider; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getOtpCode() { return otpCode != null ? otpCode : code; }
    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
        this.code = otpCode;
    }

    public LocalDateTime getOtpExpiry() { return otpExpiry; }
    public void setOtpExpiry(LocalDateTime otpExpiry) { this.otpExpiry = otpExpiry; }

    public OtpPurpose getOtpPurpose() { return otpPurpose; }
    public void setOtpPurpose(OtpPurpose otpPurpose) { this.otpPurpose = otpPurpose; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    // Phương thức hỗ trợ tương thích mã cũ
    public int getRoleId() {
        if (role == Role.ADMIN) return 1;
        return 3;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
        this.role = (roleId == 1) ? Role.ADMIN : Role.USER;
    }

    public String getCode() { return getOtpCode(); }
    public void setCode(String code) { setOtpCode(code); }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
}
