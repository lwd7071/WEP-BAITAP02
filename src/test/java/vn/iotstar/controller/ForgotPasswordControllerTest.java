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
import vn.iotstar.service.IUserService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordControllerTest {

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

    private ForgotPasswordController controller;

    @BeforeEach
    void setUp() {
        controller = new ForgotPasswordController(userService);
    }

    @Test
    void doGet_forwardsToForgotPasswordJsp() throws Exception {
        when(request.getServletPath()).thenReturn("/forgot-password");
        when(request.getContextPath()).thenReturn("/app");

        controller.doGet(request, response);

        verify(response).sendRedirect("/app/login");
    }

    @Test
    void doGet_forwardsToResetPasswordJsp() throws Exception {
        when(request.getServletPath()).thenReturn("/reset-password");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(vn.iotstar.util.AppConstants.PASSWORD_RESET_USER_ID)).thenReturn(20);
        when(session.getAttribute(vn.iotstar.util.AppConstants.PASSWORD_RESET_MASKED_EMAIL)).thenReturn("u***@example.com");
        when(request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("email", "u***@example.com");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doPost_forgotPassword_sendsOtpAndRedirects() throws Exception {
        when(request.getServletPath()).thenReturn("/forgot-password");
        when(request.getParameter("username")).thenReturn("member");
        when(userService.requestPasswordReset("member")).thenReturn("user@example.com");
        vn.iotstar.entity.User user = new vn.iotstar.entity.User();
        user.setId(20);
        user.setStatus(1);
        when(userService.findByUsername("member")).thenReturn(user);
        when(request.getSession(true)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/app");

        controller.doPost(request, response);

        verify(userService).requestPasswordReset("member");
        verify(response).sendRedirect("/app/reset-password");
    }

    @Test
    void doPost_resetPassword_updatesPasswordAndRedirectsToLogin() throws Exception {
        when(request.getServletPath()).thenReturn("/reset-password");
        when(request.getParameter("otp")).thenReturn("123456");
        when(request.getParameter("password")).thenReturn("newPassword123");
        when(request.getParameter("confirmPassword")).thenReturn("newPassword123");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(vn.iotstar.util.AppConstants.PASSWORD_RESET_USER_ID)).thenReturn(20);
        when(userService.resetPassword(20, "123456", "newPassword123")).thenReturn(true);
        when(request.getContextPath()).thenReturn("/app");

        controller.doPost(request, response);

        verify(userService).resetPassword(20, "123456", "newPassword123");
        verify(response).sendRedirect("/app/login");
    }
}
