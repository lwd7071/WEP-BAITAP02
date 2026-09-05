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

@WebServlet(urlPatterns = {"/forgot-password", "/reset-password"})
public class ForgotPasswordController extends HttpServlet {
    private final IUserService userService;

    public ForgotPasswordController() {
        this(new UserServiceImpl());
    }

    public ForgotPasswordController(IUserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/forgot-password".equals(path)) {
            request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
        } else if ("/reset-password".equals(path)) {
            String email = request.getParameter("email");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/forgot-password".equals(path)) {
            handleForgotPassword(request, response);
        } else if ("/reset-password".equals(path)) {
            handleResetPassword(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void handleForgotPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        try {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Vui lòng nhập địa chỉ email");
            }
            userService.sendForgotPasswordOtp(email.trim());
            request.getSession(true).setAttribute("success", "Mã OTP đặt lại mật khẩu đã được gửi tới email " + email.trim());
            response.sendRedirect(request.getContextPath() + "/reset-password?email=" + URLEncoder.encode(email.trim(), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException exception) {
            request.setAttribute("alert", exception.getMessage());
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
        }
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String otp = request.getParameter("otp");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        try {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Thiếu thông tin email");
            }
            if (otp == null || otp.isBlank()) {
                throw new IllegalArgumentException("Vui lòng nhập mã OTP gồm 6 chữ số");
            }
            if (password == null || password.length() < 4) {
                throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 4 ký tự");
            }
            if (!password.equals(confirmPassword)) {
                throw new IllegalArgumentException("Xác nhận mật khẩu mới không trùng khớp");
            }

            boolean success = userService.resetPassword(email.trim(), otp.trim(), password);
            if (success) {
                request.getSession(true).setAttribute("success", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập với mật khẩu mới.");
                response.sendRedirect(request.getContextPath() + "/login");
            } else {
                throw new IllegalArgumentException("Không thể đặt lại mật khẩu. Vui lòng thử lại.");
            }
        } catch (IllegalArgumentException exception) {
            request.setAttribute("alert", exception.getMessage());
            request.setAttribute("email", email);
            request.setAttribute("otp", otp);
            request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(request, response);
        }
    }
}
