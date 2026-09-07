package vn.iotstar.controller.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.dto.ApiResponse;
import vn.iotstar.dto.CategoryDto;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.User;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.service.impl.CategoryServiceImpl;
import vn.iotstar.util.AuthUtil;
import vn.iotstar.util.JsonUtil;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/api/categories", "/api/categories/*"})
public class ApiCategoryController extends HttpServlet {
    private final ICategoryService categoryService;

    public ApiCategoryController() {
        this(new CategoryServiceImpl());
    }

    public ApiCategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = AuthUtil.currentUser(request);
        if (user == null) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error(HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập"));
            return;
        }

        String pathInfo = request.getPathInfo();
        try {
            // Trường hợp: GET /api/categories/{id}
            if (pathInfo != null && pathInfo.length() > 1) {
                int id = Integer.parseInt(pathInfo.substring(1));
                Category category = categoryService.findById(id, user.getId());
                if (category == null) {
                    JsonUtil.writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                            ApiResponse.error(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy danh mục với ID: " + id));
                    return;
                }
                JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                        ApiResponse.success(CategoryDto.fromEntity(category)));
                return;
            }

            // Trường hợp: GET /api/categories (tìm kiếm & phân trang)
            String keyword = request.getParameter("q");
            List<Category> categories;
            if (keyword != null && !keyword.isBlank()) {
                categories = categoryService.searchByName(keyword.trim(), user.getId());
            } else {
                int page = 0;
                int size = 10;
                try {
                    if (request.getParameter("page") != null) {
                        page = Math.max(0, Integer.parseInt(request.getParameter("page")));
                    }
                    if (request.getParameter("size") != null) {
                        size = Math.max(1, Integer.parseInt(request.getParameter("size")));
                    }
                } catch (NumberFormatException ignored) {
                }
                categories = categoryService.findAll(user.getId(), page, size);
            }

            List<CategoryDto> dtoList = categories.stream().map(CategoryDto::fromEntity).toList();
            JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                    ApiResponse.success("Lấy danh sách danh mục thành công", dtoList));
        } catch (NumberFormatException e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, "ID danh mục không hợp lệ"));
        } catch (Exception e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = AuthUtil.currentUser(request);
        if (user == null) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error(HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập"));
            return;
        }

        try {
            CategoryDto inputDto = JsonUtil.fromJson(request, CategoryDto.class);
            if (inputDto == null || inputDto.getCategoryName() == null || inputDto.getCategoryName().isBlank()) {
                JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                        ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, "Tên danh mục không được để trống"));
                return;
            }

            Category category = new Category();
            category.setCategoryName(inputDto.getCategoryName().trim());
            category.setImages(inputDto.getImages());
            category.setStatus(inputDto.getStatus());

            categoryService.insert(category, user.getId());

            JsonUtil.writeJson(response, HttpServletResponse.SC_CREATED,
                    ApiResponse.created("Tạo danh mục thành công", CategoryDto.fromEntity(category)));
        } catch (IllegalArgumentException | IllegalStateException e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        } catch (Exception e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi tạo danh mục: " + e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = AuthUtil.currentUser(request);
        if (user == null) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error(HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập"));
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID danh mục cần cập nhật"));
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            Category existing = categoryService.findById(id, user.getId());
            if (existing == null) {
                JsonUtil.writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        ApiResponse.error(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy danh mục để cập nhật"));
                return;
            }

            CategoryDto inputDto = JsonUtil.fromJson(request, CategoryDto.class);
            if (inputDto == null || inputDto.getCategoryName() == null || inputDto.getCategoryName().isBlank()) {
                JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                        ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, "Tên danh mục không được để trống"));
                return;
            }

            existing.setCategoryName(inputDto.getCategoryName().trim());
            if (inputDto.getImages() != null) {
                existing.setImages(inputDto.getImages());
            }
            existing.setStatus(inputDto.getStatus());

            categoryService.update(existing, user.getId());

            JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                    ApiResponse.success("Cập nhật danh mục thành công", CategoryDto.fromEntity(existing)));
        } catch (NumberFormatException e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, "ID danh mục không hợp lệ"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        } catch (Exception e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi cập nhật danh mục: " + e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = AuthUtil.currentUser(request);
        if (user == null) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error(HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập"));
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID danh mục cần xóa"));
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            categoryService.delete(id, user.getId());
            JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                    ApiResponse.success("Xóa danh mục thành công", null));
        } catch (NumberFormatException e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, "ID danh mục không hợp lệ"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        } catch (Exception e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi xóa danh mục: " + e.getMessage()));
        }
    }
}
