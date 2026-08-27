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
}
