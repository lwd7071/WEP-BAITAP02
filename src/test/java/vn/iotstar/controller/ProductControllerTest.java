package vn.iotstar.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.service.IProductService;
import vn.iotstar.util.AppConstants;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {
    @Mock private IProductService productService;
    @Mock private ICategoryService categoryService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    private ProductController controller;

    @BeforeEach void setUp() { controller = new ProductController(productService, categoryService); }

    @Test
    void catalogUsesOneBasedUrlAndSixProductsPerPage() throws Exception {
        authenticate(10);
        when(request.getRequestURI()).thenReturn("/app/product");
        when(request.getContextPath()).thenReturn("/app");
        when(request.getParameter("page")).thenReturn("2");
        when(productService.countActive(10)).thenReturn(14);
        when(productService.findActive(10, 1, 6)).thenReturn(List.of());
        when(request.getRequestDispatcher("/WEB-INF/views/product-list.jsp")).thenReturn(dispatcher);
        controller.doGet(request, response);
        verify(productService).findActive(10, 1, 6);
        verify(request).setAttribute("page", 2);
        verify(request).setAttribute("totalPages", 3);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void pageBeyondLastPageUsesLastAvailablePage() throws Exception {
        authenticate(10);
        when(request.getRequestURI()).thenReturn("/app/product");
        when(request.getContextPath()).thenReturn("/app");
        when(request.getParameter("page")).thenReturn("99");
        when(productService.countActive(10)).thenReturn(7);
        when(productService.findActive(10, 1, 6)).thenReturn(List.of());
        when(request.getRequestDispatcher("/WEB-INF/views/product-list.jsp")).thenReturn(dispatcher);
        controller.doGet(request, response);
        verify(productService).findActive(10, 1, 6);
        verify(request).setAttribute("page", 2);
    }

    @Test
    void hiddenOrForeignProductDetailReturnsNotFound() throws Exception {
        authenticate(10);
        when(request.getRequestURI()).thenReturn("/app/product/detail");
        when(request.getContextPath()).thenReturn("/app");
        when(request.getParameter("id")).thenReturn("99");
        when(productService.findActiveById(99, 10)).thenReturn(null);
        controller.doGet(request, response);
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void authenticate(int ownerId) {
        User user = new User();
        user.setId(ownerId);
        user.setStatus(1);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AppConstants.SESSION_ACCOUNT)).thenReturn(user);
    }
}
