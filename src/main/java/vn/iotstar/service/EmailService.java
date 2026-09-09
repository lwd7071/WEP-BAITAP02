package vn.iotstar.service;

public interface EmailService {
    boolean sendOtpEmail(String toEmail, String fullName, String otp, String subject);
}