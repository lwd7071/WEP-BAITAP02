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
    private final vn.iotstar.service.ICategoryService categoryService;

    public HomeController() {
        this(new ProductServiceImpl(), new vn.iotstar.service.impl.CategoryServiceImpl());
    }

    public HomeController(IProductService productService, vn.iotstar.service.ICategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!"/home".equals(path)) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        request.setAttribute("pageTitle", "Trang chủ mua sắm");
        request.setAttribute("pageDescription", "Trải nghiệm mua sắm trực tuyến cao cấp.");
        User user = AuthUtil.currentUser(request);
        int ownerId = user != null ? user.getId() : 0;
        if (ownerId > 0) {
            request.setAttribute("categories", categoryService.findAll(ownerId));
            request.setAttribute("latestProducts", productService.findLatestActive(ownerId, 24));
        }
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
