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
class VerifyOtpControllerTest {

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

    private VerifyOtpController controller;

    @BeforeEach
    void setUp() {
        controller = new VerifyOtpController(userService);
    }

    @Test
    void doGet_forwardsToVerifyOtpJsp() throws Exception {
        when(request.getParameter("email")).thenReturn("test@example.com");
        when(request.getRequestDispatcher("/WEB-INF/views/verify-otp.jsp")).thenReturn(dispatcher);

        controller.doGet(request, response);

        verify(request).setAttribute("email", "test@example.com");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doPost_resendsOtp_whenActionIsResend() throws Exception {
        when(request.getParameter("action")).thenReturn("resend");
        when(request.getParameter("email")).thenReturn("test@example.com");
        when(request.getRequestDispatcher("/WEB-INF/views/verify-otp.jsp")).thenReturn(dispatcher);
        when(userService.resendRegistrationOtp("test@example.com")).thenReturn(true);

        controller.doPost(request, response);

        verify(userService).resendRegistrationOtp("test@example.com");
        verify(request).setAttribute("email", "test@example.com");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doPost_activatesAccount_whenOtpMatches() throws Exception {
        when(request.getParameter("action")).thenReturn("verify");
        when(request.getParameter("email")).thenReturn("test@example.com");
        when(request.getParameter("otp")).thenReturn("123456");
        when(userService.activateUser("test@example.com", "123456")).thenReturn(true);
        when(request.getSession(true)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/app");

        controller.doPost(request, response);

        verify(session).setAttribute(org.mockito.ArgumentMatchers.eq("success"), org.mockito.ArgumentMatchers.anyString());
        verify(response).sendRedirect("/app/login");
    }
}
