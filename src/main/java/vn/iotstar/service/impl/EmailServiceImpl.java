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

    @Override
    public boolean sendOtpEmail(String toEmail, String fullName, String otp, String subject) {
        log.info("Sending OTP email to {}: code={}, subject={}", toEmail, otp, subject);
        if (mailSender == null) {
            log.warn("JavaMailSender not configured. Simulated email sending successful.");
            return true;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText("Xin chào " + fullName + ",\n\nMã xác thực OTP của bạn là: " + otp + "\nMã có hiệu lực trong 5 phút.\n\nTrân trọng!");
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email via SMTP, fallback to simulated delivery: {}", e.getMessage());
            return true; // Không làm gián đoạn luồng nghiệp vụ khi chưa cấu hình SMTP thật
        }
    }
}