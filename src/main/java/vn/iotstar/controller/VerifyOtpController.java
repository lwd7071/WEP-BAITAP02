package vn.iotstar.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.service.IUserService;
import vn.iotstar.service.impl.UserServiceImpl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(urlPatterns = "/verify-otp")
public class VerifyOtpController extends HttpServlet {
    private final IUserService userService;

    public VerifyOtpController() {
        this(new UserServiceImpl());
    }

    public VerifyOtpController(IUserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        request.setAttribute("email", email);
        request.getRequestDispatcher("/WEB-INF/views/verify-otp.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String email = request.getParameter("email");
        String otp = request.getParameter("otp");

        if (email == null || email.isBlank()) {
            request.setAttribute("alert", "Thiếu thông tin email cần kích hoạt");
            request.getRequestDispatcher("/WEB-INF/views/verify-otp.jsp").forward(request, response);
            return;
        }

        email = email.trim();

        // Trường hợp người dùng nhấn nút "Gửi lại mã OTP"
        if ("resend".equalsIgnoreCase(action)) {
            try {
                boolean sent = userService.resendRegistrationOtp(email);
                if (sent) {
                    request.setAttribute("success", "Mã OTP mới đã được gửi tới email " + email);
                } else {
                    request.setAttribute("alert", "Không thể gửi lại OTP hoặc tài khoản đã được kích hoạt trước đó.");
                }
            } catch (Exception e) {
                request.setAttribute("alert", e.getMessage());
            }
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/views/verify-otp.jsp").forward(request, response);
            return;
        }

        // Trường hợp xác thực mã OTP
        try {
            if (otp == null || otp.isBlank()) {
                throw new IllegalArgumentException("Vui lòng nhập mã OTP gồm 6 chữ số");
            }
            boolean activated = userService.activateUser(email, otp);
            if (activated) {
                request.getSession(true).setAttribute("success", "Kích hoạt tài khoản thành công! Vui lòng đăng nhập.");
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            } else {
                throw new IllegalArgumentException("Không thể kích hoạt tài khoản hoặc tài khoản đã hoạt động.");
            }
        } catch (IllegalArgumentException exception) {
            request.setAttribute("alert", exception.getMessage());
            request.setAttribute("email", email);
            request.setAttribute("otp", otp);
            request.getRequestDispatcher("/WEB-INF/views/verify-otp.jsp").forward(request, response);
        }
    }
}
