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
import vn.iotstar.service.IUserService;
import vn.iotstar.util.AppConstants;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private IUserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    private ProfileController controller;

    @BeforeEach
    void setUp() {
        controller = new ProfileController(userService);
    }

    @Test
    void doGet_redirectsToLogin_whenNotAuthenticated() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/app");

        controller.doGet(request, response);

        verify(response).sendRedirect("/app/login");
    }

    @Test
    void doGet_forwardsToProfileView_whenAuthenticated() throws Exception {
        User user = new User();
        user.setId(1);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AppConstants.SESSION_ACCOUNT)).thenReturn(user);
        when(request.getRequestDispatcher("/WEB-INF/views/profile.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("user", user);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doPost_redirectsToLogin_whenNotAuthenticated() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/app");

        controller.doPost(request, response);

        verify(response).sendRedirect("/app/login");
    }

    @Test
    void doPost_updatesProfileAndSyncsSession_onSuccess() throws Exception {
        User currentUser = new User();
        currentUser.setId(1);
        currentUser.setAvatar("old_avatar.png");

        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setFullName("Nguyen Van B");
        updatedUser.setPhone("0988888888");
        updatedUser.setAvatar("old_avatar.png");

        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(AppConstants.SESSION_ACCOUNT)).thenReturn(currentUser);
        when(request.getParameter("fullName")).thenReturn("Nguyen Van B");
        when(request.getParameter("phone")).thenReturn("0988888888");
        when(request.getParameter("avatarUrl")).thenReturn(null);
        when(request.getPart("avatarFile")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/views/profile.jsp")).thenReturn(dispatcher);

        when(userService.updateProfile(1, "Nguyen Van B", "0988888888", "old_avatar.png"))
                .thenReturn(updatedUser);

        controller.doPost(request, response);

        verify(session).setAttribute(AppConstants.SESSION_ACCOUNT, updatedUser);
        verify(request).setAttribute("user", updatedUser);
        verify(request).setAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doPost_handlesExceptionAndForwardsWithError_whenServiceFails() throws Exception {
        User currentUser = new User();
        currentUser.setId(1);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AppConstants.SESSION_ACCOUNT)).thenReturn(currentUser);
        when(request.getParameter("fullName")).thenReturn("");
        when(request.getParameter("phone")).thenReturn("0988888888");
        when(request.getParameter("avatarUrl")).thenReturn(null);
        when(request.getPart("avatarFile")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/views/profile.jsp")).thenReturn(dispatcher);

        when(userService.updateProfile(1, "", "0988888888", null))
                .thenThrow(new IllegalArgumentException("Họ tên không được để trống"));

        controller.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Họ tên không được để trống");
        verify(request).setAttribute("user", currentUser);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doPost_rollsBackUploadedFile_whenServiceThrowsException() throws Exception {
        User currentUser = new User();
        currentUser.setId(1);

        jakarta.servlet.http.Part part = org.mockito.Mockito.mock(jakarta.servlet.http.Part.class);
        when(part.getSize()).thenReturn(100L);
        when(part.getSubmittedFileName()).thenReturn("test-avatar.png");
        when(part.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(new byte[]{1, 2, 3, 4}));

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AppConstants.SESSION_ACCOUNT)).thenReturn(currentUser);
        when(request.getParameter("fullName")).thenReturn("Nguyen Van C");
        when(request.getParameter("phone")).thenReturn("0911223344");
        when(request.getParameter("avatarUrl")).thenReturn(null);
        when(request.getPart("avatarFile")).thenReturn(part);
        when(request.getRequestDispatcher("/WEB-INF/views/profile.jsp")).thenReturn(dispatcher);

        // Giả lập Service ném lỗi sau khi file đã được ghi xuống đĩa
        when(userService.updateProfile(org.mockito.ArgumentMatchers.eq(1),
                org.mockito.ArgumentMatchers.eq("Nguyen Van C"),
                org.mockito.ArgumentMatchers.eq("0911223344"),
                org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new IllegalArgumentException("Lỗi cập nhật CSDL"));

        controller.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Lỗi cập nhật CSDL");
        verify(request).setAttribute("user", currentUser);
        verify(dispatcher).forward(request, response);
    }
}
