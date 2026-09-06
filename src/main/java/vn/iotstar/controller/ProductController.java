package vn.iotstar.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.service.IProductService;
import vn.iotstar.service.impl.CategoryServiceImpl;
import vn.iotstar.service.impl.ProductServiceImpl;
import vn.iotstar.util.AppConstants;
import vn.iotstar.util.AuthUtil;
import vn.iotstar.util.UploadUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@MultipartConfig(maxFileSize = AppConstants.MAX_IMAGE_SIZE, maxRequestSize = AppConstants.MAX_IMAGE_SIZE + 1024 * 1024)
@WebServlet(urlPatterns = {"/products", "/product", "/product/add", "/product/insert",
        "/product/edit", "/product/update", "/product/delete", "/product/detail"})
public class ProductController extends HttpServlet {
    private static final int PAGE_SIZE = 6;
    private final IProductService productService;
    private final ICategoryService categoryService;

    public ProductController() { this(new ProductServiceImpl(), new CategoryServiceImpl()); }
    public ProductController(IProductService productService, ICategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = path(request);
        try {
            switch (path) {
                case "/products" -> showManagement(request, response);
                case "/product" -> showCatalog(request, response);
                case "/product/add" -> showAdd(request, response);
                case "/product/edit" -> showEdit(request, response);
                case "/product/delete" -> delete(request, response);
                case "/product/detail" -> showDetail(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash(request, "error", exception.getMessage());
            response.sendRedirect(request.getContextPath() + "/products");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = path(request);
        if ("/product/insert".equals(path)) insert(request, response);
        else if ("/product/update".equals(path)) update(request, response);
        else response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    private void showManagement(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int ownerId = requireCurrentUser(request).getId();
        int requestedPage = parsePage(request.getParameter("page"));
        int total = productService.count(ownerId);
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        int page = Math.min(requestedPage, totalPages);
        request.setAttribute("products", productService.findAll(ownerId, page - 1, PAGE_SIZE));
        setPaging(request, page, totalPages);
        moveFlash(request);
        forward(request, response, "/WEB-INF/views/admin/product-list.jsp");
    }

    private void showCatalog(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int ownerId = requireCurrentUser(request).getId();
        int requestedPage = parsePage(request.getParameter("page"));
        int total = productService.countActive(ownerId);
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        int page = Math.min(requestedPage, totalPages);
        request.setAttribute("products", productService.findActive(ownerId, page - 1, PAGE_SIZE));
        setPaging(request, page, totalPages);
        forward(request, response, "/WEB-INF/views/product-list.jsp");
    }

    private void showAdd(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int ownerId = requireCurrentUser(request).getId();
        request.setAttribute("categories", categoryService.findAll(ownerId));
        forward(request, response, "/WEB-INF/views/admin/product-add.jsp");
    }

    private void showEdit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int ownerId = requireCurrentUser(request).getId();
        Product product = productService.findById(parseId(request), ownerId);
        if (product == null) throw new IllegalArgumentException("Không tìm thấy sản phẩm hoặc bạn không có quyền sửa");
        request.setAttribute("product", product);
        request.setAttribute("categories", categoryService.findAll(ownerId));
        forward(request, response, "/WEB-INF/views/admin/product-edit.jsp");
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int productId;
        try {
            productId = parseId(request);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Product product = productService.findActiveById(productId, requireCurrentUser(request).getId());
        if (product == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        request.setAttribute("product", product);
        forward(request, response, "/WEB-INF/views/product-detail.jsp");
    }

    private void insert(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uploaded = null;
        try {
            int ownerId = requireCurrentUser(request).getId();
            uploaded = UploadUtil.saveImage(request.getPart("imageFile"));
            Product product = readProduct(request, chooseImage(uploaded, request.getParameter("images"), null));
            productService.insert(product, parseCategoryId(request), ownerId);
            flash(request, "success", "Đã thêm sản phẩm");
            response.sendRedirect(request.getContextPath() + "/products");
        } catch (IllegalArgumentException | IllegalStateException | ServletException exception) {
            UploadUtil.deleteLocal(uploaded);
            restoreForm(request, null, exception.getMessage());
            forward(request, response, "/WEB-INF/views/admin/product-add.jsp");
        }
    }

    private void update(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int ownerId = requireCurrentUser(request).getId();
        Product current = productService.findById(parseId(request), ownerId);
        if (current == null) throw new IllegalArgumentException("Không tìm thấy sản phẩm hoặc bạn không có quyền sửa");
        String oldImage = current.getImages();
        String uploaded = null;
        try {
            uploaded = UploadUtil.saveImage(request.getPart("imageFile"));
            Product product = readProduct(request, chooseImage(uploaded, request.getParameter("images"), oldImage));
            product.setProductId(current.getProductId());
            product.setCreatedDate(current.getCreatedDate());
            productService.update(product, parseCategoryId(request), ownerId);
            if (product.getImages() != null && !product.getImages().equals(oldImage)) UploadUtil.deleteLocal(oldImage);
            flash(request, "success", "Đã cập nhật sản phẩm");
            response.sendRedirect(request.getContextPath() + "/products");
        } catch (IllegalArgumentException | IllegalStateException | ServletException exception) {
            UploadUtil.deleteLocal(uploaded);
            current.setImages(oldImage);
            restoreForm(request, current, exception.getMessage());
            forward(request, response, "/WEB-INF/views/admin/product-edit.jsp");
        }
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int ownerId = requireCurrentUser(request).getId();
        Product product = productService.findById(parseId(request), ownerId);
        if (product == null) throw new IllegalArgumentException("Không tìm thấy sản phẩm hoặc bạn không có quyền xóa");
        productService.delete(product.getProductId(), ownerId);
        UploadUtil.deleteLocal(product.getImages());
        flash(request, "success", "Đã xóa sản phẩm");
        response.sendRedirect(request.getContextPath() + "/products");
    }

    private Product readProduct(HttpServletRequest request, String image) {
        Product product = new Product();
        product.setProductName(required(request.getParameter("productName"), "Tên sản phẩm không được để trống"));
        product.setUnitPrice(parsePrice(request.getParameter("unitPrice")));
        product.setQuantity(parseNonNegative(request.getParameter("quantity"), "Số lượng không hợp lệ"));
        product.setDescription(request.getParameter("description"));
        product.setImages(image);
        product.setStatus(parseStatus(request.getParameter("status")));
        return product;
    }

    private void restoreForm(HttpServletRequest request, Product product, String error) {
        request.setAttribute("error", error);
        request.setAttribute("product", product);
        request.setAttribute("productName", request.getParameter("productName"));
        request.setAttribute("unitPrice", request.getParameter("unitPrice"));
        request.setAttribute("quantity", request.getParameter("quantity"));
        request.setAttribute("description", request.getParameter("description"));
        request.setAttribute("images", request.getParameter("images"));
        request.setAttribute("status", request.getParameter("status"));
        request.setAttribute("selectedCategoryId", request.getParameter("categoryId"));
        User user = AuthUtil.currentUser(request);
        if (user != null) request.setAttribute("categories", categoryService.findAll(user.getId()));
    }

    private User requireCurrentUser(HttpServletRequest request) {
        User user = AuthUtil.currentUser(request);
        if (user == null || user.getStatus() != 1 || user.getId() <= 0)
            throw new IllegalStateException("Vui lòng đăng nhập bằng tài khoản đã kích hoạt");
        return user;
    }

    private String chooseImage(String uploaded, String imageUrl, String fallback) {
        if (uploaded != null) return uploaded;
        if (imageUrl != null && !imageUrl.isBlank()) {
            String value = imageUrl.trim();
            if (!UploadUtil.isRemoteUrl(value)) throw new IllegalArgumentException("Link ảnh phải bắt đầu bằng http:// hoặc https://");
            return value;
        }
        return fallback;
    }

    private BigDecimal parsePrice(String value) {
        try { BigDecimal result = new BigDecimal(value); if (result.signum() < 0) throw new NumberFormatException(); return result; }
        catch (Exception exception) { throw new IllegalArgumentException("Giá sản phẩm không hợp lệ"); }
    }
    private int parseCategoryId(HttpServletRequest request) { return parsePositive(request.getParameter("categoryId"), "Vui lòng chọn danh mục"); }
    private int parseId(HttpServletRequest request) { return parsePositive(request.getParameter("id"), "Mã sản phẩm không hợp lệ"); }
    private int parseStatus(String value) { int status = parseNonNegative(value, "Vui lòng chọn trạng thái sản phẩm"); if (status > 1) throw new IllegalArgumentException("Vui lòng chọn trạng thái sản phẩm"); return status; }
    private int parsePositive(String value, String message) { try { int result = Integer.parseInt(value); if (result <= 0) throw new NumberFormatException(); return result; } catch (Exception exception) { throw new IllegalArgumentException(message); } }
    private int parseNonNegative(String value, String message) { try { int result = Integer.parseInt(value); if (result < 0) throw new NumberFormatException(); return result; } catch (Exception exception) { throw new IllegalArgumentException(message); } }
    private int parsePage(String value) { try { int page = Integer.parseInt(value); return Math.max(1, page); } catch (Exception exception) { return 1; } }
    private String required(String value, String message) { if (value == null || value.isBlank()) throw new IllegalArgumentException(message); return value.trim(); }
    private void setPaging(HttpServletRequest request, int page, int totalPages) { request.setAttribute("page", page); request.setAttribute("totalPages", totalPages); }
    private String path(HttpServletRequest request) { return request.getRequestURI().substring(request.getContextPath().length()); }
    private void forward(HttpServletRequest request, HttpServletResponse response, String view) throws ServletException, IOException { request.getRequestDispatcher(view).forward(request, response); }
    private void flash(HttpServletRequest request, String key, String value) { request.getSession(true).setAttribute("flash_" + key, value); }
    private void moveFlash(HttpServletRequest request) { for (String key : List.of("success", "error")) { Object value = request.getSession().getAttribute("flash_" + key); if (value != null) { request.setAttribute(key, value); request.getSession().removeAttribute("flash_" + key); } } }
}
