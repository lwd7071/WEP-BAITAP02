package vn.iotstar.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.dto.RegisterRequestDto;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class RegistrationRollbackTest {
    @Autowired AuthService authService;
    @Autowired UserRepository users;
    @MockitoBean EmailService emailService;

    @Test
    void mailFailureRollsBackRealServiceTransaction() {
        var form = new RegisterRequestDto();
        form.setUsername("rollback_mail_user");
        form.setEmail("rollback-mail@example.com");
        form.setFullName("Mail Test");
        form.setPassword("secret123");
        doThrow(new EmailDeliveryException(null)).when(emailService)
                .sendOtpEmail(anyString(), anyString(), anyString(), anyString());
        assertThatThrownBy(() -> authService.register(form)).isInstanceOf(EmailDeliveryException.class);
        assertThat(users.findByUsernameIgnoreCase(form.getUsername())).isEmpty();
        assertThat(users.findByEmailIgnoreCase(form.getEmail())).isEmpty();
    }
}
