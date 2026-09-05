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
        when(request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    void doGet_forwardsToResetPasswordJsp() throws Exception {
        when(request.getServletPath()).thenReturn("/reset-password");
        when(request.getParameter("email")).thenReturn("user@example.com");
        when(request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("email", "user@example.com");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doPost_forgotPassword_sendsOtpAndRedirects() throws Exception {
        when(request.getServletPath()).thenReturn("/forgot-password");
        when(request.getParameter("email")).thenReturn("user@example.com");
        when(request.getSession(true)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/app");

        controller.doPost(request, response);

        verify(userService).sendForgotPasswordOtp("user@example.com");
        verify(response).sendRedirect("/app/reset-password?email=user%40example.com");
    }

    @Test
    void doPost_resetPassword_updatesPasswordAndRedirectsToLogin() throws Exception {
        when(request.getServletPath()).thenReturn("/reset-password");
        when(request.getParameter("email")).thenReturn("user@example.com");
        when(request.getParameter("otp")).thenReturn("123456");
        when(request.getParameter("password")).thenReturn("newPassword123");
        when(request.getParameter("confirmPassword")).thenReturn("newPassword123");
        when(userService.resetPassword("user@example.com", "123456", "newPassword123")).thenReturn(true);
        when(request.getSession(true)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/app");

        controller.doPost(request, response);

        verify(userService).resetPassword("user@example.com", "123456", "newPassword123");
        verify(response).sendRedirect("/app/login");
    }
}
