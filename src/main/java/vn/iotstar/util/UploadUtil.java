package vn.iotstar.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class UploadUtil {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private UploadUtil() {
    }

    public static Path uploadDirectory() throws IOException {
        String configured = System.getenv("UPLOAD_DIR");
        Path directory = configured == null || configured.isBlank()
                ? Paths.get("D:", "BAITAP02", "uploads")
                : Paths.get(configured.trim());
        directory = directory.toAbsolutePath().normalize();
        Files.createDirectories(directory);
        return directory;
    }

    public static String saveImage(Part part) throws IOException, ServletException {
        if (part == null || part.getSize() == 0) {
            return null;
        }
        if (part.getSize() > AppConstants.MAX_IMAGE_SIZE) {
            throw new ServletException("Ảnh không được vượt quá 5 MB");
        }
        String submitted = Paths.get(part.getSubmittedFileName()).getFileName().toString();
        int dot = submitted.lastIndexOf('.');
        if (dot < 1 || dot == submitted.length() - 1) {
            throw new ServletException("Tên ảnh không có phần mở rộng hợp lệ");
        }
        String extension = submitted.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ServletException("Chỉ chấp nhận JPG, PNG, GIF hoặc WEBP");
        }
        String fileName = UUID.randomUUID() + "." + extension;
        Path target = safeResolve(fileName);
        try (var input = part.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return fileName;
    }

    public static Path safeResolve(String fileName) throws IOException {
        if (fileName == null || fileName.isBlank() || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }
        Path directory = uploadDirectory();
        Path resolved = directory.resolve(fileName).normalize();
        if (!resolved.startsWith(directory)) {
            throw new IllegalArgumentException("Đường dẫn file không hợp lệ");
        }
        return resolved;
    }

    public static boolean isRemoteUrl(String value) {
        if (value == null) {
            return false;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.startsWith("https://") || lower.startsWith("http://");
    }

    public static void deleteLocal(String fileName) {
        if (fileName == null || fileName.isBlank() || isRemoteUrl(fileName)) {
            return;
        }
        try {
            Files.deleteIfExists(safeResolve(fileName));
        } catch (IOException | IllegalArgumentException ignored) {
            // A database update must not fail just because an old optional image cannot be removed.
        }
    }
}
