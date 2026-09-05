package vn.iotstar.filter;

import jakarta.servlet.annotation.WebFilter;
import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;

@WebFilter("/*")
public class SiteMeshFilter extends ConfigurableSiteMeshFilter {

    @Override
    protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
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
    }
}
