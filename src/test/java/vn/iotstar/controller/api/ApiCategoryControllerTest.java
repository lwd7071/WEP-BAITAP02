package vn.iotstar.controller.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.User;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.util.AppConstants;
import vn.iotstar.util.JsonUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiCategoryControllerTest {

    @Mock
    private ICategoryService categoryService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private ApiCategoryController controller;
    private final ObjectMapper mapper = JsonUtil.getMapper();
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        controller = new ApiCategoryController(categoryService);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void doGet_whenUnauthenticated_returns401Json() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        controller.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        JsonNode node = mapper.readTree(stringWriter.toString());
        assertFalse(node.get("success").asBoolean());
        assertEquals(401, node.get("code").asInt());
        assertEquals("Vui lòng đăng nhập", node.get("message").asText());
    }

    @Test
    void doGet_listCategories_returns200WithCorrectFormat() throws Exception {
        User user = new User();
        user.setId(5);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AppConstants.SESSION_ACCOUNT)).thenReturn(user);
        when(request.getPathInfo()).thenReturn(null);
        when(request.getParameter("q")).thenReturn(null);
        when(request.getParameter("page")).thenReturn("0");
        when(request.getParameter("size")).thenReturn("10");

        Category cat = new Category("Laptop", "laptop.jpg", 1);
        cat.setOwner(user);
        when(categoryService.findAll(5, 0, 10)).thenReturn(List.of(cat));

        controller.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonNode node = mapper.readTree(stringWriter.toString());
        assertTrue(node.get("success").asBoolean());
        assertEquals(200, node.get("code").asInt());
        assertEquals("Lấy danh sách danh mục thành công", node.get("message").asText());
        assertTrue(node.get("data").isArray());
        assertEquals(1, node.get("data").size());
        assertEquals("Laptop", node.get("data").get(0).get("categoryName").asText());
    }

    @Test
    void doGet_findById_notFound_returns404Json() throws Exception {
        User user = new User();
        user.setId(5);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AppConstants.SESSION_ACCOUNT)).thenReturn(user);
        when(request.getPathInfo()).thenReturn("/999");
        when(categoryService.findById(999, 5)).thenReturn(null);

        controller.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        JsonNode node = mapper.readTree(stringWriter.toString());
        assertFalse(node.get("success").asBoolean());
        assertEquals(404, node.get("code").asInt());
    }

    @Test
    void doDelete_success_returns200Json() throws Exception {
        User user = new User();
        user.setId(5);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AppConstants.SESSION_ACCOUNT)).thenReturn(user);
        when(request.getPathInfo()).thenReturn("/12");

        controller.doDelete(request, response);

        verify(categoryService).delete(12, 5);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        JsonNode node = mapper.readTree(stringWriter.toString());
        assertTrue(node.get("success").asBoolean());
        assertEquals(200, node.get("code").asInt());
        assertEquals("Xóa danh mục thành công", node.get("message").asText());
    }
}
