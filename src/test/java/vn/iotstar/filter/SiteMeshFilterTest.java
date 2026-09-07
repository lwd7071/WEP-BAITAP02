package vn.iotstar.filter;

import jakarta.servlet.annotation.WebFilter;
import org.junit.jupiter.api.Test;
import org.sitemesh.builder.SiteMeshFilterBuilder;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SiteMeshFilterTest {

    @Test
    void siteMeshFilter_hasWebFilterAnnotationForRoot() {
        WebFilter annotation = SiteMeshFilter.class.getAnnotation(WebFilter.class);
        assertNotNull(annotation, "SiteMeshFilter must be annotated with @WebFilter");
        boolean matchesRoot = false;
        for (String pattern : annotation.value()) {
            if ("/*".equals(pattern)) matchesRoot = true;
        }
        for (String pattern : annotation.urlPatterns()) {
            if ("/*".equals(pattern)) matchesRoot = true;
        }
        assertTrue(matchesRoot, "SiteMeshFilter must map /*");
    }

    @Test
    void applyCustomConfiguration_configuresDecoratorsAndExcludes() throws Exception {
        SiteMeshFilter filter = new SiteMeshFilter();
        SiteMeshFilterBuilder builder = mock(SiteMeshFilterBuilder.class);

        Method method = SiteMeshFilter.class.getDeclaredMethod("applyCustomConfiguration", SiteMeshFilterBuilder.class);
        method.setAccessible(true);
        method.invoke(filter, builder);

        // Verify dispatch mode
        verify(builder).setDispatchMode(org.sitemesh.webapp.DispatchMode.INCLUDE);

        // Verify decorator paths
        verify(builder).addDecoratorPath("/admin/*", "/WEB-INF/decorators/admin.jsp");
        verify(builder).addDecoratorPath("/*", "/WEB-INF/decorators/web.jsp");

        // Verify excluded paths
        verify(builder).addExcludedPath("/login");
        verify(builder).addExcludedPath("/register");
        verify(builder).addExcludedPath("/verify-otp");
        verify(builder).addExcludedPath("/forgot-password");
        verify(builder).addExcludedPath("/reset-password");
        verify(builder).addExcludedPath("/assets/*");
        verify(builder).addExcludedPath("/image*");
    }
}
