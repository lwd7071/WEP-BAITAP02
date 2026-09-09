package vn.iotstar.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vn.iotstar.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username:}")
    private String from;

    @Override
    public boolean sendOtpEmail(String toEmail, String fullName, String otp, String subject) {
        if (mailSender == null) {
            throw new vn.iotstar.service.EmailDeliveryException(null);
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText("Xin chào " + fullName + ",\n\nMã xác thực OTP của bạn là: " + otp + "\nMã có hiệu lực trong 5 phút.\n\nTrân trọng!");
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            log.error("SMTP delivery failed: {}", e.getClass().getSimpleName());
            throw new vn.iotstar.service.EmailDeliveryException(e);
        }
    }
}
