package vn.iotstar.filter;

import jakarta.servlet.annotation.WebFilter;
import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;
import org.sitemesh.webapp.DispatchMode;

@WebFilter("/*")
public class SiteMeshFilter extends ConfigurableSiteMeshFilter {

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, jakarta.servlet.FilterChain chain)
            throws java.io.IOException, jakarta.servlet.ServletException {
        super.doFilter(request, response, chain);
        try {
            response.getWriter().flush();
        } catch (Exception ignored) {
            try {
                response.getOutputStream().flush();
            } catch (Exception ignored2) {
            }
        }
    }

    @Override
    protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
        // Dùng DispatchMode.INCLUDE để tránh Tomcat 11 commit response sớm gây trắng trang
        builder.setDispatchMode(DispatchMode.INCLUDE);

        // Decorator riêng cho trang quản trị Admin
        builder.addDecoratorPath("/admin/*", "/WEB-INF/decorators/admin.jsp");

        // Decorator mặc định cho các trang người dùng
        builder.addDecoratorPath("/*", "/WEB-INF/decorators/web.jsp");

        // Loại trừ các trang xác thực (login, register, OTP) và tài nguyên tĩnh/ảnh
        builder.addExcludedPath("/login");
        builder.addExcludedPath("/register");
        builder.addExcludedPath("/verify-otp");
        builder.addExcludedPath("/forgot-password");
        builder.addExcludedPath("/reset-password");
        builder.addExcludedPath("/assets/*");
        builder.addExcludedPath("/image*");
        builder.addExcludedPath("/api/*");
    }
}
