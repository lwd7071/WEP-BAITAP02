package vn.iotstar.util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EmailUtil {
    private static final Logger LOGGER = Logger.getLogger(EmailUtil.class.getName());

    private EmailUtil() {
    }

    public static boolean sendOtpEmail(String toEmail, String fullName, String otp, String purpose) {
        String subject = "[JPA Category] " + purpose + " - Mã OTP của bạn";
        String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px; background: #ffffff;">
                    <div style="text-align: center; margin-bottom: 20px;">
                        <h2 style="color: #4338ca; margin: 0;">JPA Category</h2>
                        <p style="color: #64748b; font-size: 14px; margin-top: 4px;">Xác thực tài khoản bảo mật</p>
                    </div>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Bạn vừa có yêu cầu <strong>%s</strong>. Dưới đây là mã OTP xác thực của bạn:</p>
                    <div style="text-align: center; margin: 24px 0;">
                        <span style="display: inline-block; font-size: 32px; font-weight: 800; letter-spacing: 8px; color: #4338ca; background: #eef2ff; padding: 12px 24px; border-radius: 8px; border: 1px dashed #818cf8;">
                            %s
                        </span>
                    </div>
                    <p style="color: #ef4444; font-size: 13px;">* Mã OTP có hiệu lực trong vòng <strong>5 phút</strong>. Tuyệt đối không chia sẻ mã này cho bất kỳ ai.</p>
                    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;">
                    <p style="font-size: 12px; color: #94a3b8; text-align: center;">Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>
                </div>
                """.formatted(
                fullName != null && !fullName.isBlank() ? fullName : "bạn",
                purpose,
                otp
        );
        return sendEmail(toEmail, subject, htmlContent);
    }

    public static boolean sendEmail(String toEmail, String subject, String htmlContent) {
        String host = getEnv("SMTP_HOST", "smtp.gmail.com");
        String port = getEnv("SMTP_PORT", "587");
        String rawUser = getEnv("SMTP_USER", getEnv("EMAIL_USER", null));
        String rawPass = getEnv("SMTP_PASSWORD", getEnv("EMAIL_PASSWORD", null));

        final String user = (rawUser != null) ? rawUser.trim() : null;
        final String pass = (rawPass != null) ? rawPass.trim().replace(" ", "") : null;

        // Không giả lập gửi mail trong môi trường chạy thật: nếu thiếu cấu hình,
        // service phải biết để báo lỗi và cho người dùng gửi lại OTP sau.
        if (user == null || pass == null || user.isBlank() || pass.isBlank()) {
            LOGGER.log(Level.INFO, """
                    \n[EMAIL NOT SENT] SMTP_USER/SMTP_PASSWORD chưa được cấu hình. Gửi tới: {0}
                    """, new Object[]{toEmail, subject, htmlContent});
            return false;
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user, "JPA Category"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=" + StandardCharsets.UTF_8.name());

            Transport.send(message);
            LOGGER.log(Level.INFO, "Đã gửi email thành công tới {0}", toEmail);
            return true;
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Lỗi khi gửi email tới " + toEmail, exception);
            return false;
        }
    }

    private static String getEnv(String name, String fallback) {
        String val = System.getenv(name);
        return val != null && !val.isBlank() ? val.trim() : fallback;
    }
}
