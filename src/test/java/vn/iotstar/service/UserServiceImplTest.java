package vn.iotstar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.iotstar.dao.IUserDao;
import vn.iotstar.entity.User;
import vn.iotstar.service.impl.UserServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private IUserDao userDao;
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userDao);
    }

    @Test
    void loginReturnsUserWhenPlainPasswordMatches() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("123456");
        when(userDao.findByUsername("admin")).thenReturn(user);

        assertSame(user, service.login("admin", "123456"));
        assertNull(service.login("admin", "wrong"));
    }

    @Test
    void registerCreatesDefaultMember() {
        assertTrue(service.register("member@example.com", "1234", "member", "Người dùng", "0909123456"));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).insert(captor.capture());
        assertEquals(3, captor.getValue().getRoleId());
        assertEquals("member", captor.getValue().getUsername());
    }

    @Test
    void registerReturnsFalseForDuplicateEmail() {
        when(userDao.existsByEmail("member@example.com")).thenReturn(true);

        assertFalse(service.register("member@example.com", "1234", "member", "Người dùng", null));

        verify(userDao, never()).insert(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void checkExistPhoneForUser_returnsFalse_whenPhoneBelongsToCurrentId() {
        // Phone của chính user thì không tính là trùng
        when(userDao.existsByPhoneAndNotId("0909123456", 1)).thenReturn(false);

        assertFalse(service.checkExistPhoneForUser("0909123456", 1));
    }

    @Test
    void checkExistPhoneForUser_returnsTrue_whenPhoneBelongsToAnotherId() {
        // Phone thuộc về user khác thì tính là trùng
        when(userDao.existsByPhoneAndNotId("0909123456", 1)).thenReturn(true);

        assertTrue(service.checkExistPhoneForUser("0909123456", 1));
    }

    @Test
    void updateProfile_throwsException_whenFullNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                service.updateProfile(1, "   ", "0909123456", null));
    }

    @Test
    void updateProfile_throwsException_whenPhoneAlreadyTakenByOtherUser() {
        when(userDao.existsByPhoneAndNotId("0909123456", 1)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                service.updateProfile(1, "Nguyen Van A", "0909123456", null));
    }

    @Test
    void updateProfile_success_updatesFieldsAndReturnsUpdatedUser() {
        User updated = new User();
        updated.setId(1);
        updated.setFullName("Nguyen Van A");
        updated.setPhone("0909123456");
        updated.setAvatar("avatar.png");

        when(userDao.existsByPhoneAndNotId("0909123456", 1)).thenReturn(false);
        when(userDao.updateProfile(1, "Nguyen Van A", "0909123456", "avatar.png")).thenReturn(updated);

        User result = service.updateProfile(1, "Nguyen Van A", "0909123456", "avatar.png");

        assertSame(updated, result);
        verify(userDao).updateProfile(1, "Nguyen Van A", "0909123456", "avatar.png");
    }

    @Test
    void checkExistPhoneForUser_returnsFalse_whenPhoneIsNull() {
        assertFalse(service.checkExistPhoneForUser(null, 1));
        verify(userDao, never()).existsByPhoneAndNotId(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void checkExistPhoneForUser_returnsFalse_whenPhoneIsBlank() {
        assertFalse(service.checkExistPhoneForUser("   ", 1));
        verify(userDao, never()).existsByPhoneAndNotId(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void updateProfile_allowsNullOrBlankPhone_withoutCheckingDuplicate() {
        User updated = new User();
        updated.setId(1);
        updated.setFullName("Nguyen Van A");
        updated.setPhone(null);

        when(userDao.updateProfile(1, "Nguyen Van A", null, null)).thenReturn(updated);

        User result = service.updateProfile(1, "Nguyen Van A", "   ", null);

        assertSame(updated, result);
        verify(userDao, never()).existsByPhoneAndNotId(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt());
        verify(userDao).updateProfile(1, "Nguyen Van A", null, null);
    }

    @Test
    void updateProfile_throwsException_whenFullNameIsTooShort() {
        assertThrows(IllegalArgumentException.class, () ->
                service.updateProfile(1, "A", "0909123456", null));
    }

    @Test
    void updateProfile_throwsException_whenPhoneIsNot10Digits() {
        // Nhập chữ
        assertThrows(IllegalArgumentException.class, () ->
                service.updateProfile(1, "Nguyen Van A", "aaa", null));

        // Thiếu số
        assertThrows(IllegalArgumentException.class, () ->
                service.updateProfile(1, "Nguyen Van A", "0909123", null));

        // Thừa số
        assertThrows(IllegalArgumentException.class, () ->
                service.updateProfile(1, "Nguyen Van A", "090912345678", null));

        // Không bắt đầu bằng 0
        assertThrows(IllegalArgumentException.class, () ->
                service.updateProfile(1, "Nguyen Van A", "1234567890", null));
    }

    @Test
    void login_throwsIllegalStateException_whenAccountIsUnactivated() {
        User user = new User();
        user.setUsername("unverified");
        user.setPassword("123456");
        user.setStatus(0);
        when(userDao.findByUsername("unverified")).thenReturn(user);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.login("unverified", "123456"));
        assertTrue(ex.getMessage().contains("chưa được kích hoạt"));
    }

    @Test
    void activateUser_activatesAccount_whenOtpMatches() {
        User user = new User();
        user.setId(10);
        user.setEmail("user@example.com");
        user.setStatus(0);
        user.setCode("123456");
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(5));

        when(userDao.findByEmail("user@example.com")).thenReturn(user);

        boolean result = service.activateUser("user@example.com", "123456");
        assertTrue(result);
        verify(userDao).updateStatusAndCode(10, 1, null);
    }

    @Test
    void activateUser_throwsException_whenOtpMismatch() {
        User user = new User();
        user.setId(10);
        user.setEmail("user@example.com");
        user.setStatus(0);
        user.setCode("123456");
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(5));

        when(userDao.findByEmail("user@example.com")).thenReturn(user);

        assertThrows(IllegalArgumentException.class, () ->
                service.activateUser("user@example.com", "999999"));
    }

    @Test
    void sendForgotPasswordOtp_and_resetPassword_success() {
        User user = new User();
        user.setId(20);
        user.setEmail("forgot@example.com");
        user.setCode("654321");
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(5));

        when(userDao.findByEmail("forgot@example.com")).thenReturn(user);

        boolean sent = service.sendForgotPasswordOtp("forgot@example.com");
        assertTrue(sent);
        verify(userDao).updateOtp(org.mockito.ArgumentMatchers.eq(20), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());

        boolean reset = service.resetPassword("forgot@example.com", "654321", "newPass123");
        assertTrue(reset);
        verify(userDao).updatePassword(20, "newPass123");
    }
}
