package vn.iotstar.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.entity.User;
import vn.iotstar.service.IUserService;
import vn.iotstar.service.impl.UserServiceImpl;
import vn.iotstar.util.AppConstants;
import vn.iotstar.util.AuthUtil;

import java.io.IOException;

@WebFilter(urlPatterns = {"/home", "/manager/*", "/admin/*", "/profile", "/categories", "/category/*",
        "/products", "/product", "/product/*"})
public class AuthorizationFilter implements Filter {
    private final IUserService userService;

    public AuthorizationFilter() {
        this(new UserServiceImpl());
    }

    public AuthorizationFilter(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        User user = AuthUtil.currentUser(request);

        if (user == null || user.getStatus() != 1) {
            String remembered = AuthUtil.cookieValue(request, AppConstants.COOKIE_REMEMBER);
            user = userService.findByUsername(remembered);
            if (user != null && user.getStatus() == 1) {
                request.getSession(true).setAttribute(AppConstants.SESSION_ACCOUNT, user);
            } else {
                user = null;
            }
        }

        if (user == null) {
            request.getSession(true).setAttribute("redirectAfterLogin", request.getRequestURI());
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String path = request.getRequestURI().substring(request.getContextPath().length());
        if ((path.startsWith("/admin/") && user.getRoleId() != 1)
                || (path.startsWith("/manager/") && user.getRoleId() != 2)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này");
            return;
        }
        chain.doFilter(request, response);
    }
}
