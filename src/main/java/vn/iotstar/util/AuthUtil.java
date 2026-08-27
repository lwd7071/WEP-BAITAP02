package vn.iotstar.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import vn.iotstar.entity.User;

public final class AuthUtil {
    private AuthUtil() {
    }

    public static User currentUser(HttpServletRequest request) {
        Object value = request.getSession(false) == null
                ? null : request.getSession(false).getAttribute(AppConstants.SESSION_ACCOUNT);
        return value instanceof User user ? user : null;
    }

    public static String cookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static String homeFor(User user, String contextPath) {
        return switch (user.getRoleId()) {
            case 1 -> contextPath + "/admin/home";
            case 2 -> contextPath + "/manager/home";
            default -> contextPath + "/home";
        };
    }
}
