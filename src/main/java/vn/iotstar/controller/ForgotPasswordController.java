package vn.iotstar.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.iotstar.entity.User;
import vn.iotstar.service.IUserService;
import vn.iotstar.service.impl.UserServiceImpl;
import vn.iotstar.util.AppConstants;

import java.io.IOException;

@WebServlet(urlPatterns = {"/forgot-password", "/reset-password"})
public class ForgotPasswordController extends HttpServlet {
    private final IUserService userService;

    public ForgotPasswordController() { this(new UserServiceImpl()); }
    public ForgotPasswordController(IUserService userService) { this.userService = userService; }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/forgot-password".equals(path)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if ("/reset-password".equals(path)) {
            HttpSession session = request.getSession(false);
            if (session == null || !(session.getAttribute(AppConstants.PASSWORD_RESET_USER_ID) instanceof Number)) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            request.setAttribute("email", session.getAttribute(AppConstants.PASSWORD_RESET_MASKED_EMAIL));
            request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/forgot-password".equals(path)) handleForgotPassword(request, response);
        else if ("/reset-password".equals(path)) handleResetPassword(request, response);
        else response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    private void handleForgotPassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        try {
            String email = userService.requestPasswordReset(username);
            User user = userService.findByUsername(username == null ? null : username.trim());
            if (user == null) throw new IllegalArgumentException("Không tìm thấy tài khoản với tên đã nhập");
            HttpSession session = request.getSession(true);
            session.setAttribute(AppConstants.PASSWORD_RESET_USER_ID, user.getId());
            session.setAttribute(AppConstants.PASSWORD_RESET_MASKED_EMAIL, maskEmail(email));
            session.setAttribute("success", "Mã OTP đã được gửi tới " + maskEmail(email) + ".");
            response.sendRedirect(request.getContextPath() + "/reset-password");
        } catch (IllegalArgumentException exception) {
            request.setAttribute("alert", exception.getMessage());
            request.setAttribute("rememberedUsername", username);
            User user = username == null ? null : userService.findByUsername(username.trim());
            if (user != null && user.getStatus() == vn.iotstar.entity.UserStatus.PENDING) request.setAttribute("unverifiedEmail", user.getEmail());
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Object userIdValue = session == null ? null : session.getAttribute(AppConstants.PASSWORD_RESET_USER_ID);
        String otp = request.getParameter("otp");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        try {
            if (!(userIdValue instanceof Number)) throw new IllegalArgumentException("Phiên đặt lại mật khẩu đã hết hạn. Vui lòng thử lại.");
            if (otp == null || !otp.matches("\\d{6}")) throw new IllegalArgumentException("Vui lòng nhập mã OTP gồm 6 chữ số");
            if (password == null || password.length() < 4) throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 4 ký tự");
            if (!password.equals(confirmPassword)) throw new IllegalArgumentException("Xác nhận mật khẩu mới không trùng khớp");
            userService.resetPassword(((Number) userIdValue).intValue(), otp.trim(), password);
            session.removeAttribute(AppConstants.PASSWORD_RESET_USER_ID);
            session.removeAttribute(AppConstants.PASSWORD_RESET_MASKED_EMAIL);
            session.setAttribute("success", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập với mật khẩu mới.");
            response.sendRedirect(request.getContextPath() + "/login");
        } catch (IllegalArgumentException exception) {
            request.setAttribute("alert", exception.getMessage());
            request.setAttribute("otp", otp);
            request.setAttribute("email", session == null ? null : session.getAttribute(AppConstants.PASSWORD_RESET_MASKED_EMAIL));
            request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(request, response);
        }
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "địa chỉ email đã đăng ký";
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(at);
        return email.charAt(0) + "***" + email.substring(at);
    }
}
