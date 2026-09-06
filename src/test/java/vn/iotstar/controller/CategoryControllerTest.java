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
import vn.iotstar.entity.User;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.util.AppConstants;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {
    @Mock
    private ICategoryService categoryService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private RequestDispatcher dispatcher;

    private CategoryController controller;

    @BeforeEach
    void setUp() {
        controller = new CategoryController(categoryService);
    }

    @Test
    void listUsesAuthenticatedUserAsOwner() throws Exception {
        User user = new User();
        user.setId(10);
        user.setStatus(1);
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(AppConstants.SESSION_ACCOUNT)).thenReturn(user);
        when(request.getRequestURI()).thenReturn("/app/categories");
        when(request.getContextPath()).thenReturn("/app");
        when(request.getParameter("q")).thenReturn(null);
        when(request.getParameter("page")).thenReturn("0");
        when(categoryService.count(10)).thenReturn(0);
        when(categoryService.findAll(10, 0, 6)).thenReturn(java.util.List.of());
        when(request.getRequestDispatcher("/WEB-INF/views/admin/category-list.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(categoryService).count(10);
        verify(categoryService).findAll(10, 0, 6);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void editDoesNotExposeCategoryOwnedByAnotherUser() throws Exception {
        User user = new User();
        user.setId(10);
        user.setStatus(1);
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute(AppConstants.SESSION_ACCOUNT)).thenReturn(user);
        when(request.getRequestURI()).thenReturn("/app/category/edit");
        when(request.getContextPath()).thenReturn("/app");
        when(request.getParameter("id")).thenReturn("99");
        when(categoryService.findById(99, 10)).thenReturn(null);

        controller.doGet(request, response);

        verify(categoryService).findById(99, 10);
        verify(response).sendRedirect("/app/categories");
    }
}
