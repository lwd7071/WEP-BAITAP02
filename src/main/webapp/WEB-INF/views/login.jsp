<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi" class="notranslate">
<head>
    <title>Đăng nhập | JPA Category</title>
    <%@ include file="partials/head.jspf" %>
</head>
<body class="auth-page">
<main class="auth-shell centered">
    <section class="auth-card">
        <div class="card-heading">
            <span class="brand-mark">J</span>
            <div><h2>Chào mừng trở lại</h2><p>Đăng nhập để tiếp tục</p></div>
        </div>
        <c:if test="${not empty sessionScope.success}">
            <div class="alert success"><c:out value="${sessionScope.success}"/></div>
            <c:remove var="success" scope="session"/>
        </c:if>
        <c:if test="${not empty alert}">
            <div class="alert error">
                <c:out value="${alert}"/>
                <c:if test="${not empty unverifiedEmail}">
                    <div style="margin-top: 8px;">
                        <a href="${pageContext.request.contextPath}/verify-otp?email=${fn:escapeXml(unverifiedEmail)}" style="color: #ffffff; text-decoration: underline; font-weight: 600;">
                            👉 Nhấn vào đây để kích hoạt tài khoản bằng mã OTP
                        </a>
                    </div>
                </c:if>
            </div>
        </c:if>
        <form action="${pageContext.request.contextPath}/login" method="post" class="form-stack">
            <label>Tài khoản
                <input name="username" autocomplete="username" required
                       value="${fn:escapeXml(rememberedUsername)}" placeholder="Nhập tên tài khoản">
            </label>
            <label>Mật khẩu
                <input type="password" name="password" autocomplete="current-password" required
                       placeholder="Nhập mật khẩu">
            </label>
            <div style="display: flex; justify-content: space-between; align-items: center; margin: 4px 0 10px 0;">
                <label class="check-row" style="margin: 0;"><input type="checkbox" name="remember"> Ghi nhớ 30 phút</label>
                <button type="submit" formaction="${pageContext.request.contextPath}/forgot-password" formmethod="post" formnovalidate
                        style="border: 0; background: none; padding: 0; font: inherit; font-size: 13px; color: #4338ca; cursor: pointer; font-weight: 500;">Quên mật khẩu?</button>
            </div>
            <button class="button primary" type="submit">Đăng nhập</button>
        </form>
        <p class="form-foot">Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký ngay</a></p>
    </section>
</main>
<script src="${pageContext.request.contextPath}/assets/app.js" defer></script>
</body>
</html>
