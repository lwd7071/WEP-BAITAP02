package vn.iotstar.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/home", "/manager/home", "/admin/home"})
public class HomeController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/admin/")) {
            request.setAttribute("pageTitle", "Trang quản trị");
            request.setAttribute("pageDescription", "Quản lý danh mục và dữ liệu hệ thống bằng JPA.");
        } else if (path.startsWith("/manager/")) {
            request.setAttribute("pageTitle", "Trang quản lý");
            request.setAttribute("pageDescription", "Khu vực dành cho tài khoản quản lý.");
        } else {
            request.setAttribute("pageTitle", "Trang người dùng");
            request.setAttribute("pageDescription", "Đăng nhập Session và Cookie đã hoạt động.");
        }
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
