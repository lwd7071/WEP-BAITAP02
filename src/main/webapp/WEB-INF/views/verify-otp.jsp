<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi" class="notranslate">
<head>
    <title>Xác thực OTP | JPA Store</title>
    <%@ include file="partials/head.jspf" %>
</head>
<body class="auth-page">
<main class="auth-shell centered">
    <section class="auth-card">
        <div class="otp-icon-bubble">
            ✉
        </div>

        <div class="card-heading" style="justify-content: center; text-align: center; flex-direction: column; gap: 0.4rem; margin-bottom: 1.4rem;">
            <h2>Xác thực tài khoản</h2>
            <p>Nhập mã OTP 6 chữ số được gửi tới email của bạn</p>
        </div>

        <c:if test="${not empty sessionScope.success}">
            <div class="alert success"><c:out value="${sessionScope.success}"/></div>
            <c:remove var="success" scope="session"/>
        </c:if>
        <c:if test="${not empty success}">
            <div class="alert success"><c:out value="${success}"/></div>
        </c:if>
        <c:if test="${not empty message}">
            <div class="alert success" role="status"><c:out value="${message}"/></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert error" role="alert"><c:out value="${error}"/></div>
        </c:if>

        <form action="${pageContext.request.contextPath}/verify-otp" method="post" class="form-stack">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <input type="hidden" name="action" value="verify">
            <label>Email xác thực
                <input type="email" name="email" required readonly
                       value="${fn:escapeXml(email)}" class="input-readonly">
            </label>
            <label>Mã OTP (6 chữ số)
                <input type="text" name="otp" required maxlength="6" pattern="[0-9]{6}"
                       placeholder="· · · · · ·" autocomplete="one-time-code" autofocus
                       value="${fn:escapeXml(otp)}" style="font-size: 22px; letter-spacing: 8px; text-align: center; font-weight: 800;">
            </label>
            <button class="button primary full" type="submit">Xác nhận kích hoạt</button>
        </form>

        <form action="${pageContext.request.contextPath}/verify-otp/resend" method="post" style="margin-top: 10px;">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <input type="hidden" name="action" value="resend">
            <input type="hidden" name="email" value="${fn:escapeXml(email)}">
            <button class="button secondary full" type="submit">Gửi lại mã OTP mới</button>
        </form>

        <p class="form-foot">Đã kích hoạt tài khoản? <a href="${pageContext.request.contextPath}/login">Quay lại đăng nhập</a></p>
    </section>
</main>
<script src="${pageContext.request.contextPath}/assets/app.js" defer></script>
</body>
</html>
