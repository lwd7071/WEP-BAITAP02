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
        if (!"/home".equals(path)) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        request.setAttribute("pageTitle", "Trang quản lý cá nhân");
        request.setAttribute("pageDescription", "Quản lý danh mục thuộc tài khoản đang đăng nhập.");
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
