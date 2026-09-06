package vn.iotstar.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.annotation.WebFilter;
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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationFilterTest {

    @Mock
    private IUserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private FilterChain chain;

    private AuthorizationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AuthorizationFilter(userService);
    }

    @Test
    void webFilterAnnotation_containsProfileUrl() {
        WebFilter annotation = AuthorizationFilter.class.getAnnotation(WebFilter.class);
        String[] patterns = annotation.urlPatterns();
        boolean containsProfile = Arrays.asList(patterns).contains("/profile");
        assertTrue(containsProfile, "AuthorizationFilter must include /profile in its urlPatterns");
        assertTrue(Arrays.asList(patterns).contains("/categories"));
        assertTrue(Arrays.asList(patterns).contains("/category/*"));
    }

    @Test
    void doFilter_redirectsToLogin_whenAccessingProfileUnauthenticated() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getCookies()).thenReturn(null);
        when(request.getSession(true)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/app");
        when(request.getRequestURI()).thenReturn("/app/profile");

        filter.doFilter(request, response, chain);

        verify(session).setAttribute("redirectAfterLogin", "/app/profile");
        verify(response).sendRedirect("/app/login");
    }

    @Test
    void doFilter_continuesChain_whenAuthenticatedMemberAccessesProfile() throws Exception {
        User member = new User();
        member.setId(1);
        member.setRoleId(3);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AppConstants.SESSION_ACCOUNT)).thenReturn(member);
        when(request.getContextPath()).thenReturn("/app");
        when(request.getRequestURI()).thenReturn("/app/profile");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
