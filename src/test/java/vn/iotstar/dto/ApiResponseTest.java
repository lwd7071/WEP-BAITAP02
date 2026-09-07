package vn.iotstar.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import vn.iotstar.util.JsonUtil;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    private final ObjectMapper mapper = JsonUtil.getMapper();

    @Test
    void successResponse_hasAllRequiredFields() throws Exception {
        ApiResponse<String> response = ApiResponse.success("Dữ liệu test");

        String json = mapper.writeValueAsString(response);
        JsonNode node = mapper.readTree(json);

        assertTrue(node.has("success"));
        assertTrue(node.get("success").asBoolean());
        assertTrue(node.has("code"));
        assertEquals(200, node.get("code").asInt());
        assertTrue(node.has("message"));
        assertEquals("Thành công", node.get("message").asText());
        assertTrue(node.has("data"));
        assertEquals("Dữ liệu test", node.get("data").asText());
        assertTrue(node.has("timestamp"));
        assertTrue(node.get("timestamp").asLong() > 0);
    }

    @Test
    void createdResponse_returnsCode201() throws Exception {
        CategoryDto dto = new CategoryDto(1, "Điện thoại", "phone.jpg", 1, 10, "admin");
        ApiResponse<CategoryDto> response = ApiResponse.created("Tạo mới thành công", dto);

        String json = mapper.writeValueAsString(response);
        JsonNode node = mapper.readTree(json);

        assertTrue(node.get("success").asBoolean());
        assertEquals(201, node.get("code").asInt());
        assertEquals("Tạo mới thành công", node.get("message").asText());
        assertEquals("Điện thoại", node.get("data").get("categoryName").asText());
    }

    @Test
    void errorResponse_hasCorrectCodeAndNullData() throws Exception {
        ApiResponse<Void> response = ApiResponse.error(400, "Dữ liệu không hợp lệ");

        String json = mapper.writeValueAsString(response);
        JsonNode node = mapper.readTree(json);

        assertFalse(node.get("success").asBoolean());
        assertEquals(400, node.get("code").asInt());
        assertEquals("Dữ liệu không hợp lệ", node.get("message").asText());
        assertTrue(node.get("data").isNull());
    }
}
