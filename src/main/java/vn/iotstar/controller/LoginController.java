package vn.iotstar.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.entity.User;
import vn.iotstar.service.IUserService;
import vn.iotstar.service.impl.UserServiceImpl;
import vn.iotstar.util.AppConstants;
import vn.iotstar.util.AuthUtil;

import java.io.IOException;

@WebServlet(urlPatterns = "/login")
public class LoginController extends HttpServlet {
    private final IUserService userService;

    public LoginController() {
        this(new UserServiceImpl());
    }

    public LoginController(IUserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User current = AuthUtil.currentUser(request);
        if (current != null) {
            response.sendRedirect(request.getContextPath() + "/waiting");
            return;
        }
        String remembered = AuthUtil.cookieValue(request, AppConstants.COOKIE_REMEMBER);
        if (remembered != null) {
            User user = userService.findByUsername(remembered);
            if (user != null && user.isActive()) {
                request.getSession(true).setAttribute(AppConstants.SESSION_ACCOUNT, user);
                response.sendRedirect(request.getContextPath() + "/waiting");
                return;
            }
            request.setAttribute("rememberedUsername", remembered);
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            forwardError(request, response, "Tài khoản và mật khẩu không được để trống", username, null);
            return;
        }
        try {
            User user = userService.login(username, password);
            if (user == null) {
                forwardError(request, response, "Tài khoản hoặc mật khẩu không đúng", username, null);
                return;
            }
            request.getSession(true).setAttribute(AppConstants.SESSION_ACCOUNT, user);
            if ("on".equals(request.getParameter("remember"))) {
                Cookie cookie = new Cookie(AppConstants.COOKIE_REMEMBER, user.getUsername());
                cookie.setMaxAge(AppConstants.COOKIE_MAX_AGE);
                cookie.setHttpOnly(true);
                cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
                response.addCookie(cookie);
            }
            Object target = request.getSession().getAttribute("redirectAfterLogin");
            request.getSession().removeAttribute("redirectAfterLogin");
            if (target instanceof String uri && uri.startsWith(request.getContextPath() + "/")) {
                response.sendRedirect(uri);
            } else {
                response.sendRedirect(request.getContextPath() + "/waiting");
            }
        } catch (IllegalStateException exception) {
            User unverified = userService.findByUsername(username.trim());
            String unverifiedEmail = unverified != null ? unverified.getEmail() : null;
            forwardError(request, response, exception.getMessage(), username, unverifiedEmail);
        }
    }

    private void forwardError(HttpServletRequest request, HttpServletResponse response,
                              String message, String username, String unverifiedEmail) throws ServletException, IOException {
        request.setAttribute("alert", message);
        request.setAttribute("rememberedUsername", username);
        request.setAttribute("unverifiedEmail", unverifiedEmail);
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }
}
