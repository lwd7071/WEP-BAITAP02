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
        assertTrue(service.register("member@example.com", "1234", "member", "Người dùng", "0909"));
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
}
