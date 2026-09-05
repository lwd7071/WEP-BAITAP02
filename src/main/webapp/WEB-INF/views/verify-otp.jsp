<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi" class="notranslate">
<head>
    <title>Xác thực OTP | JPA Category</title>
    <%@ include file="partials/head.jspf" %>
</head>
<body class="auth-page">
<main class="auth-shell centered">
    <section class="auth-card">
        <div class="card-heading">
            <span class="brand-mark">J</span>
            <div>
                <h2>Xác thực tài khoản</h2>
                <p>Nhập mã OTP 6 chữ số được gửi tới email</p>
            </div>
        </div>

        <c:if test="${not empty sessionScope.success}">
            <div class="alert success"><c:out value="${sessionScope.success}"/></div>
            <c:remove var="success" scope="session"/>
        </c:if>
        <c:if test="${not empty success}">
            <div class="alert success"><c:out value="${success}"/></div>
        </c:if>
        <c:if test="${not empty alert}">
            <div class="alert error"><c:out value="${alert}"/></div>
        </c:if>

        <form action="${pageContext.request.contextPath}/verify-otp" method="post" class="form-stack">
            <input type="hidden" name="action" value="verify">
            <label>Email xác thực
                <input type="email" name="email" required readonly
                       value="${fn:escapeXml(email)}" class="input-readonly">
            </label>
            <label>Mã OTP (6 chữ số)
                <input type="text" name="otp" required maxlength="6" pattern="[0-9]{6}"
                       placeholder="Ví dụ: 123456" autocomplete="one-time-code" autofocus
                       value="${fn:escapeXml(otp)}" style="font-size: 20px; letter-spacing: 6px; text-align: center;">
            </label>
            <button class="button primary full" type="submit">Xác nhận kích hoạt</button>
        </form>

        <form action="${pageContext.request.contextPath}/verify-otp" method="post" style="margin-top: 10px;">
            <input type="hidden" name="action" value="resend">
            <input type="hidden" name="email" value="${fn:escapeXml(email)}">
            <button class="button secondary full" type="submit">Gửi lại mã OTP mới</button>
        </form>

        <p class="form-foot">Đã xác thực xong? <a href="${pageContext.request.contextPath}/login">Quay lại đăng nhập</a></p>
    </section>
</main>
</body>
</html>
