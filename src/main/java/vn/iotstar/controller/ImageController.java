package vn.iotstar.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.util.UploadUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet(urlPatterns = "/image")
public class ImageController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Path file = UploadUtil.safeResolve(request.getParameter("fname"));
            if (!Files.isRegularFile(file)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String contentType = Files.probeContentType(file);
            response.setContentType(contentType == null ? "application/octet-stream" : contentType);
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setContentLengthLong(Files.size(file));
            try (var input = Files.newInputStream(file)) {
                input.transferTo(response.getOutputStream());
            }
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tên file không hợp lệ");
        }
    }
}
