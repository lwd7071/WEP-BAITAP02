package vn.iotstar.service;

import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import vn.iotstar.service.impl.EmailServiceImpl;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class EmailDeliveryTest {
    @Test
    void sendsActualMessageWithSenderAndOtp() {
        var sender = mock(JavaMailSender.class);
        var service = new EmailServiceImpl();
        ReflectionTestUtils.setField(service, "mailSender", sender);
        ReflectionTestUtils.setField(service, "from", "sender@example.com");
        service.sendOtpEmail("recipient@example.com", "Test", "123456", "Activation");
        var message = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(message.capture());
        assertThat(message.getValue().getFrom()).isEqualTo("sender@example.com");
        assertThat(message.getValue().getTo()).containsExactly("recipient@example.com");
        assertThat(message.getValue().getSubject()).isEqualTo("Activation");
        assertThat(message.getValue().getText()).contains("123456");
    }
    @Test
    void smtpFailureAndMissingSenderCannotReportSuccess() {
        var service = new EmailServiceImpl();
        assertThatThrownBy(() -> service.sendOtpEmail("a@b.com", "Test", "123456", "OTP"))
                .isInstanceOf(EmailDeliveryException.class);
        var sender = mock(JavaMailSender.class);
        ReflectionTestUtils.setField(service, "mailSender", sender);
        doThrow(new MailSendException("Unavailable")).when(sender).send(any(SimpleMailMessage.class));
        assertThatThrownBy(() -> service.sendOtpEmail("a@b.com", "Test", "123456", "OTP"))
                .isInstanceOf(EmailDeliveryException.class);
    }
}
