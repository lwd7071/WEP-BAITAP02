package vn.iotstar.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.dto.ApiResponse;

import java.io.IOException;

/**
 * Tiện ích xử lý JSON và ghi response chuẩn cho REST API
 */
public final class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // Cấu hình ObjectMapper hỗ trợ Java 8 Date/Time và bỏ qua thuộc tính lạ
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private JsonUtil() {
    }

    public static ObjectMapper getMapper() {
        return OBJECT_MAPPER;
    }

    // Chuyển đối tượng sang chuỗi JSON
    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi chuyển đổi sang JSON: " + e.getMessage(), e);
        }
    }

    // Đọc JSON từ request body
    public static <T> T fromJson(HttpServletRequest request, Class<T> targetClass) throws IOException {
        return OBJECT_MAPPER.readValue(request.getInputStream(), targetClass);
    }

    // Ghi phản hồi ApiResponse chuẩn về client dưới dạng JSON
    public static void writeJson(HttpServletResponse response, int statusCode, ApiResponse<?> apiResponse)
            throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(apiResponse));
        response.getWriter().flush();
    }
}
