package vn.iotstar.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.entity.User;
import vn.iotstar.service.IProductService;
import vn.iotstar.service.impl.ProductServiceImpl;
import vn.iotstar.util.AuthUtil;

import java.io.IOException;

@WebServlet(urlPatterns = {"/home", "/manager/home", "/admin/home"})
public class HomeController extends HttpServlet {
    private final IProductService productService;

    public HomeController() { this(new ProductServiceImpl()); }
    public HomeController(IProductService productService) { this.productService = productService; }

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
        User user = AuthUtil.currentUser(request);
        request.setAttribute("latestProducts", productService.findLatestActive(user.getId(), 10));
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
