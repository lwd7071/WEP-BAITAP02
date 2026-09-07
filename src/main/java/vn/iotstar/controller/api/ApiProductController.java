package vn.iotstar.controller.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.dto.ApiResponse;
import vn.iotstar.dto.ProductDto;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.service.IProductService;
import vn.iotstar.service.impl.ProductServiceImpl;
import vn.iotstar.util.AuthUtil;
import vn.iotstar.util.JsonUtil;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/api/products", "/api/products/*"})
public class ApiProductController extends HttpServlet {
    private final IProductService productService;

    public ApiProductController() {
        this(new ProductServiceImpl());
    }

    public ApiProductController(IProductService productService) {
        this.productService = productService;
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
            // Trường hợp: GET /api/products/{id}
            if (pathInfo != null && pathInfo.length() > 1) {
                int id = Integer.parseInt(pathInfo.substring(1));
                Product product = productService.findById(id, user.getId());
                if (product == null) {
                    JsonUtil.writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                            ApiResponse.error(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy sản phẩm với ID: " + id));
                    return;
                }
                JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                        ApiResponse.success(ProductDto.fromEntity(product)));
                return;
            }

            // Trường hợp: GET /api/products
            String latest = request.getParameter("latest");
            List<Product> products;
            if ("true".equalsIgnoreCase(latest)) {
                products = productService.findLatestActive(user.getId(), 10);
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
                products = productService.findAll(user.getId(), page, size);
            }

            List<ProductDto> dtoList = products.stream().map(ProductDto::fromEntity).toList();
            JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                    ApiResponse.success("Lấy danh sách sản phẩm thành công", dtoList));
        } catch (NumberFormatException e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, "ID sản phẩm không hợp lệ"));
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
            ProductDto inputDto = JsonUtil.fromJson(request, ProductDto.class);
            if (inputDto == null || inputDto.getProductName() == null || inputDto.getProductName().isBlank()) {
                JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                        ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, "Tên sản phẩm không được để trống"));
                return;
            }

            Product product = new Product();
            product.setProductName(inputDto.getProductName().trim());
            product.setUnitPrice(inputDto.getUnitPrice());
            product.setQuantity(inputDto.getQuantity());
            product.setDescription(inputDto.getDescription());
            product.setImages(inputDto.getImages());
            product.setStatus(inputDto.getStatus());

            productService.insert(product, inputDto.getCategoryId(), user.getId());

            JsonUtil.writeJson(response, HttpServletResponse.SC_CREATED,
                    ApiResponse.created("Tạo sản phẩm thành công", ProductDto.fromEntity(product)));
        } catch (IllegalArgumentException | IllegalStateException e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        } catch (Exception e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi tạo sản phẩm: " + e.getMessage()));
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
                    ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID sản phẩm"));
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            Product existing = productService.findById(id, user.getId());
            if (existing == null) {
                JsonUtil.writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        ApiResponse.error(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy sản phẩm để cập nhật"));
                return;
            }

            ProductDto inputDto = JsonUtil.fromJson(request, ProductDto.class);
            if (inputDto == null || inputDto.getProductName() == null || inputDto.getProductName().isBlank()) {
                JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                        ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, "Tên sản phẩm không được để trống"));
                return;
            }

            existing.setProductName(inputDto.getProductName().trim());
            existing.setUnitPrice(inputDto.getUnitPrice());
            existing.setQuantity(inputDto.getQuantity());
            existing.setDescription(inputDto.getDescription());
            if (inputDto.getImages() != null) {
                existing.setImages(inputDto.getImages());
            }
            existing.setStatus(inputDto.getStatus());

            int categoryId = inputDto.getCategoryId() > 0 ? inputDto.getCategoryId() : existing.getCategory().getCategoryId();
            productService.update(existing, categoryId, user.getId());

            JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                    ApiResponse.success("Cập nhật sản phẩm thành công", ProductDto.fromEntity(existing)));
        } catch (NumberFormatException e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, "ID sản phẩm không hợp lệ"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        } catch (Exception e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi cập nhật sản phẩm: " + e.getMessage()));
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
                    ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, "Thiếu ID sản phẩm cần xóa"));
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            productService.delete(id, user.getId());
            JsonUtil.writeJson(response, HttpServletResponse.SC_OK,
                    ApiResponse.success("Xóa sản phẩm thành công", null));
        } catch (NumberFormatException e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, "ID sản phẩm không hợp lệ"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    ApiResponse.error(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        } catch (Exception e) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi xóa sản phẩm: " + e.getMessage()));
        }
    }
}
