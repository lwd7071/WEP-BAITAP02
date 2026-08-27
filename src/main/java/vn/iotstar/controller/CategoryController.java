package vn.iotstar.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import vn.iotstar.entity.Category;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.service.impl.CategoryServiceImpl;
import vn.iotstar.util.AppConstants;
import vn.iotstar.util.UploadUtil;

import java.io.IOException;
import java.util.List;

@MultipartConfig(maxFileSize = AppConstants.MAX_IMAGE_SIZE, maxRequestSize = AppConstants.MAX_IMAGE_SIZE + 1024 * 1024)
@WebServlet(urlPatterns = {
        "/admin/categories",
        "/admin/category/add",
        "/admin/category/insert",
        "/admin/category/edit",
        "/admin/category/update",
        "/admin/category/delete"
})
public class CategoryController extends HttpServlet {
    private static final int PAGE_SIZE = 6;
    private final ICategoryService categoryService = new CategoryServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = path(request);
        try {
            switch (path) {
                case "/admin/categories" -> showList(request, response);
                case "/admin/category/add" -> forward(request, response, "/WEB-INF/views/admin/category-add.jsp");
                case "/admin/category/edit" -> showEdit(request, response);
                case "/admin/category/delete" -> delete(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flash(request, "error", exception.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/categories");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = path(request);
        if ("/admin/category/insert".equals(path)) {
            insert(request, response);
        } else if ("/admin/category/update".equals(path)) {
            update(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("q");
        int page = parseNonNegative(request.getParameter("page"), 0);
        List<Category> categories;
        int total;
        if (keyword != null && !keyword.isBlank()) {
            categories = categoryService.searchByName(keyword);
            total = categories.size();
            int from = Math.min(page * PAGE_SIZE, total);
            int to = Math.min(from + PAGE_SIZE, total);
            categories = categories.subList(from, to);
        } else {
            total = categoryService.count();
            categories = categoryService.findAll(page, PAGE_SIZE);
        }
        request.setAttribute("categories", categories);
        request.setAttribute("keyword", keyword == null ? "" : keyword.trim());
        request.setAttribute("page", page);
        request.setAttribute("totalPages", Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE)));
        moveFlash(request);
        forward(request, response, "/WEB-INF/views/admin/category-list.jsp");
    }

    private void showEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Category category = categoryService.findById(parseId(request));
        if (category == null) {
            throw new IllegalArgumentException("Không tìm thấy danh mục");
        }
        request.setAttribute("category", category);
        forward(request, response, "/WEB-INF/views/admin/category-edit.jsp");
    }

    private void insert(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String uploaded = null;
        try {
            uploaded = UploadUtil.saveImage(request.getPart("imageFile"));
            String image = chooseImage(uploaded, request.getParameter("images"), null);
            Category category = new Category(
                    required(request.getParameter("categoryName"), "Tên danh mục không được để trống"),
                    image,
                    parseStatus(request.getParameter("status")));
            categoryService.insert(category);
            flash(request, "success", "Đã thêm danh mục thành công");
            response.sendRedirect(request.getContextPath() + "/admin/categories");
        } catch (IllegalArgumentException | IllegalStateException | ServletException exception) {
            UploadUtil.deleteLocal(uploaded);
            request.setAttribute("error", exception.getMessage());
            request.setAttribute("categoryName", request.getParameter("categoryName"));
            request.setAttribute("images", request.getParameter("images"));
            request.setAttribute("status", request.getParameter("status"));
            forward(request, response, "/WEB-INF/views/admin/category-add.jsp");
        }
    }

    private void update(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Category category = categoryService.findById(parseId(request));
        if (category == null) {
            throw new IllegalArgumentException("Không tìm thấy danh mục");
        }
        String oldImage = category.getImages();
        String uploaded = null;
        try {
            Part imagePart = request.getPart("imageFile");
            uploaded = UploadUtil.saveImage(imagePart);
            String newImage = chooseImage(uploaded, request.getParameter("images"), oldImage);
            category.setCategoryName(required(request.getParameter("categoryName"),
                    "Tên danh mục không được để trống"));
            category.setStatus(parseStatus(request.getParameter("status")));
            category.setImages(newImage);
            categoryService.update(category);
            if (newImage != null && !newImage.equals(oldImage)) {
                UploadUtil.deleteLocal(oldImage);
            }
            flash(request, "success", "Đã cập nhật danh mục");
            response.sendRedirect(request.getContextPath() + "/admin/categories");
        } catch (IllegalArgumentException | IllegalStateException | ServletException exception) {
            UploadUtil.deleteLocal(uploaded);
            category.setImages(oldImage);
            request.setAttribute("category", category);
            request.setAttribute("error", exception.getMessage());
            forward(request, response, "/WEB-INF/views/admin/category-edit.jsp");
        }
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Category category = categoryService.findById(parseId(request));
        if (category == null) {
            throw new IllegalArgumentException("Không tìm thấy danh mục");
        }
        categoryService.delete(category.getCategoryId());
        UploadUtil.deleteLocal(category.getImages());
        flash(request, "success", "Đã xóa danh mục");
        response.sendRedirect(request.getContextPath() + "/admin/categories");
    }

    private String chooseImage(String uploaded, String imageUrl, String fallback) {
        if (uploaded != null) {
            return uploaded;
        }
        if (imageUrl != null && !imageUrl.isBlank()) {
            String value = imageUrl.trim();
            if (!UploadUtil.isRemoteUrl(value)) {
                throw new IllegalArgumentException("Link ảnh phải bắt đầu bằng http:// hoặc https://");
            }
            return value;
        }
        return fallback;
    }

    private int parseId(HttpServletRequest request) {
        return parsePositive(request.getParameter("id"), "Mã danh mục không hợp lệ");
    }

    private int parseStatus(String value) {
        int status = parseNonNegative(value, -1);
        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("Vui lòng chọn trạng thái danh mục");
        }
        return status;
    }

    private int parsePositive(String value, String message) {
        try {
            int result = Integer.parseInt(value);
            if (result <= 0) throw new NumberFormatException();
            return result;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(message);
        }
    }

    private int parseNonNegative(String value, int fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            int result = Integer.parseInt(value);
            return result < 0 ? fallback : result;
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
        return value.trim();
    }

    private String path(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        request.getRequestDispatcher(view).forward(request, response);
    }

    private void flash(HttpServletRequest request, String key, String value) {
        request.getSession(true).setAttribute("flash_" + key, value);
    }

    private void moveFlash(HttpServletRequest request) {
        for (String key : List.of("success", "error")) {
            Object value = request.getSession().getAttribute("flash_" + key);
            if (value != null) {
                request.setAttribute(key, value);
                request.getSession().removeAttribute("flash_" + key);
            }
        }
    }
}
