<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi" class="notranslate">
<head>
    <title>Quên mật khẩu | JPA Category</title>
    <%@ include file="partials/head.jspf" %>
</head>
<body class="auth-page">
<main class="auth-shell centered">
    <section class="auth-card">
        <div class="card-heading">
            <span class="brand-mark">J</span>
            <div>
                <h2>Quên mật khẩu</h2>
                <p>Nhập email đã đăng ký để nhận mã OTP</p>
            </div>
        </div>

        <c:if test="${not empty alert}">
            <div class="alert error"><c:out value="${alert}"/></div>
        </c:if>

        <form action="${pageContext.request.contextPath}/forgot-password" method="post" class="form-stack">
            <label>Địa chỉ Email
                <input type="email" name="email" required autofocus
                       value="${fn:escapeXml(email)}" placeholder="name@example.com">
            </label>
            <button class="button primary full" type="submit">Gửi mã xác nhận OTP</button>
        </form>

        <p class="form-foot">Nhớ lại mật khẩu? <a href="${pageContext.request.contextPath}/login">Quay lại đăng nhập</a></p>
    </section>
</main>
</body>
</html>
