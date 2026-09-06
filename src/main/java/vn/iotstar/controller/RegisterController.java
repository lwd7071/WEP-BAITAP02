package vn.iotstar.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.service.IUserService;
import vn.iotstar.service.impl.UserServiceImpl;

import java.io.IOException;

@WebServlet(urlPatterns = "/register")
public class RegisterController extends HttpServlet {
    private final IUserService userService;

    public RegisterController() {
        this(new UserServiceImpl());
    }

    public RegisterController(IUserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String fullName = request.getParameter("fullname");
        String phone = request.getParameter("phone");
        try {
            if (userService.checkExistEmail(email)) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }
            if (userService.checkExistUsername(username)) {
                throw new IllegalArgumentException("Tài khoản đã tồn tại");
            }
            if (userService.checkExistPhone(phone)) {
                throw new IllegalArgumentException("Số điện thoại đã tồn tại");
            }
            if (!userService.register(email, password, username, fullName, phone)) {
                throw new IllegalArgumentException("Thông tin đăng ký đã tồn tại");
            }
            request.getSession(true).setAttribute("success", "Đăng ký thành công! Mã OTP đã được gửi tới email của bạn để kích hoạt.");
            response.sendRedirect(request.getContextPath() + "/verify-otp?email=" + java.net.URLEncoder.encode(email.trim(), java.nio.charset.StandardCharsets.UTF_8));
        } catch (IllegalArgumentException exception) {
            request.setAttribute("alert", exception.getMessage());
            request.setAttribute("canResendOtp", exception.getMessage() != null
                    && exception.getMessage().contains("email OTP"));
            request.setAttribute("email", email);
            request.setAttribute("username", username);
            request.setAttribute("fullname", fullName);
            request.setAttribute("phone", phone);
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        }
    }
}
