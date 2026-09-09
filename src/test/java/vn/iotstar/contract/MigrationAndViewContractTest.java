package vn.iotstar.contract;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationAndViewContractTest {

    private static String read(String path) throws Exception {
        return Files.readString(Path.of("src/main", path), StandardCharsets.UTF_8);
    }

    @Test
    void productionPropertiesMustNotContainSecretFallbacks() throws Exception {
        String properties = read("resources/application.properties");

        assertThat(properties).contains("spring.config.import=optional:file:.env[.properties]");
        assertThat(properties).doesNotContain("DB_PASSWORD:123456");
        assertThat(properties).doesNotContain("APP_ADMIN_PASSWORD:Admin@123456");
        assertThat(properties).doesNotContain("databaseName=WEP_BAITAP02");
    }

    @Test
    void localEnvMustBeIgnoredAndExampleMustContainAllRuntimeKeys() throws Exception {
        String gitignore = Files.readString(Path.of(".gitignore"), StandardCharsets.UTF_8);
        String example = Files.readString(Path.of(".env.example"), StandardCharsets.UTF_8);

        assertThat(gitignore.lines()).contains(".env");
        assertThat(example).contains(
                "DB_URL=", "DB_USER=", "DB_PASSWORD=", "UPLOAD_DIR=",
                "APP_ADMIN_USERNAME=", "APP_ADMIN_EMAIL=", "APP_ADMIN_PASSWORD=",
                "GOOGLE_CLIENT_ID=", "GOOGLE_CLIENT_SECRET=",
                "SMTP_HOST=", "SMTP_PORT=", "SMTP_USER=", "SMTP_PASSWORD=",
                "PASSWORD_MIGRATION_ENABLED=");
    }

    @Test
    void categoryJspMustUseSpringAdminRoutesAndPostCsrfForms() throws Exception {
        String list = read("webapp/WEB-INF/views/admin/category-list.jsp");
        String add = read("webapp/WEB-INF/views/admin/category-add.jsp");
        String edit = read("webapp/WEB-INF/views/admin/category-edit.jsp");

        assertThat(list).contains("/admin/categories/new", "/admin/categories/${category.categoryId}/edit",
                "/admin/categories/${category.categoryId}/delete", "${_csrf.token}");
        assertThat(add).contains("/admin/categories", "${_csrf.token}");
        assertThat(edit).contains("/admin/categories/${category.categoryId}/update", "${_csrf.token}");
        assertThat(list).doesNotContain("/category/", "href=\"${pageContext.request.contextPath}/categories\"");
    }

    @Test
    void migrationMustRejectInvalidLegacyStatusAndProtectGoogleProviderId() throws Exception {
        String sql = Files.readString(Path.of("sql/05-spring-boot-4-role-auth-migration.sql"), StandardCharsets.UTF_8);

        assertThat(sql).contains("status NOT IN (0, 1)");
        assertThat(sql).contains("uk_users_google_provider_id");
        assertThat(sql).doesNotContain("ELSE N''PENDING''");
    }

    @Test
    void applicationViewsMustIncludeTheCompleteSharedUiShell() throws Exception {
        List<String> rootViews = List.of(
                "home.jsp", "product-list.jsp", "product-detail.jsp", "profile.jsp");
        List<String> adminViews = List.of(
                "category-list.jsp", "category-add.jsp", "category-edit.jsp",
                "product-list.jsp", "product-add.jsp", "product-edit.jsp",
                "user-list.jsp", "user-add.jsp", "user-edit.jsp");

        for (String view : rootViews) {
            assertCompleteUiShell(read("webapp/WEB-INF/views/" + view), "partials/");
        }
        for (String view : adminViews) {
            assertCompleteUiShell(read("webapp/WEB-INF/views/admin/" + view), "../partials/");
        }

        assertThat(read("webapp/WEB-INF/views/partials/head.jspf"))
                .contains("/assets/app.css", "/assets/products.css");
        for (String partial : List.of("head.jspf", "topbar.jspf", "footer.jspf", "cart-drawer.jspf")) {
            assertThat(read("webapp/WEB-INF/views/partials/" + partial))
                    .contains("pageEncoding=\"UTF-8\"");
        }
    }

    private static void assertCompleteUiShell(String jsp, String partialPrefix) {
        assertThat(jsp).contains(
                "include file=\"" + partialPrefix + "head.jspf\"",
                "include file=\"" + partialPrefix + "topbar.jspf\"",
                "include file=\"" + partialPrefix + "footer.jspf\"",
                "include file=\"" + partialPrefix + "cart-drawer.jspf\"",
                "/assets/app.js");
    }

    @Test
    void productCatalogWithoutSidebarMustUseTheFullWidthGrid() throws Exception {
        String productList = read("webapp/WEB-INF/views/product-list.jsp");
        String productCss = read("webapp/assets/products.css");

        assertThat(productList).contains("class=\"catalog-layout full-width\"");
        assertThat(productCss).contains(
                ".catalog-layout.full-width",
                "grid-template-columns: minmax(0, 1fr)");
    }
}
