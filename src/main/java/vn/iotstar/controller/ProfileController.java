package vn.iotstar.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.entity.User;
import vn.iotstar.service.IUserService;
import vn.iotstar.service.impl.UserServiceImpl;
import vn.iotstar.util.AuthUtil;

import java.io.IOException;

@WebServlet("/profile")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 10 * 1024 * 1024
)
public class ProfileController extends HttpServlet {
    private final IUserService userService;

    public ProfileController() {
        this(new UserServiceImpl());
    }

    public ProfileController(IUserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = AuthUtil.currentUser(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.setAttribute("user", currentUser);
        request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = AuthUtil.currentUser(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String avatarUrl = request.getParameter("avatarUrl");

        String uploadedFileName = null;
        try {
            jakarta.servlet.http.Part avatarPart = request.getPart("avatarFile");
            uploadedFileName = vn.iotstar.util.UploadUtil.saveImage(avatarPart);

            String avatar;
            if (uploadedFileName != null) {
                avatar = uploadedFileName;
            } else if (avatarUrl != null && !avatarUrl.isBlank()) {
                avatar = avatarUrl.trim();
            } else {
                avatar = currentUser.getAvatar();
            }

            User updatedUser = userService.updateProfile(currentUser.getId(), fullName, phone, avatar);
            // Đồng bộ lại session sau khi cập nhật thành công
            request.getSession().setAttribute(vn.iotstar.util.AppConstants.SESSION_ACCOUNT, updatedUser);

            // Dọn dẹp avatar local cũ nếu đã upload ảnh mới khác
            if (uploadedFileName != null && currentUser.getAvatar() != null && !currentUser.getAvatar().equals(uploadedFileName)) {
                vn.iotstar.util.UploadUtil.deleteLocal(currentUser.getAvatar());
            }

            request.setAttribute("successMessage", "Cập nhật hồ sơ thành công!");
            request.setAttribute("user", updatedUser);
            request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
        } catch (Exception exception) {
            // Rollback: Xóa file vừa upload nếu cập nhật thất bại
            if (uploadedFileName != null) {
                vn.iotstar.util.UploadUtil.deleteLocal(uploadedFileName);
            }
            request.setAttribute("errorMessage", exception.getMessage());
            request.setAttribute("user", currentUser);
            request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
        }
    }
}
