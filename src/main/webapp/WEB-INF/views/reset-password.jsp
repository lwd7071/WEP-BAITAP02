<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi" class="notranslate">
<head>
    <title>Đặt lại mật khẩu | JPA Category</title>
    <%@ include file="partials/head.jspf" %>
</head>
<body class="auth-page">
<main class="auth-shell centered">
    <section class="auth-card">
        <div class="card-heading">
            <span class="brand-mark">J</span>
            <div>
                <h2>Đặt lại mật khẩu</h2>
                <p>Nhập mã OTP đã nhận và thiết lập mật khẩu mới</p>
            </div>
        </div>

        <c:if test="${not empty sessionScope.success}">
            <div class="alert success"><c:out value="${sessionScope.success}"/></div>
            <c:remove var="success" scope="session"/>
        </c:if>
        <c:if test="${not empty alert}">
            <div class="alert error"><c:out value="${alert}"/></div>
        </c:if>

        <form action="${pageContext.request.contextPath}/reset-password" method="post" class="form-stack">
            <label>Địa chỉ Email
                <input type="email" name="email" required readonly
                       value="${fn:escapeXml(email)}" class="input-readonly">
            </label>
            <label>Mã OTP (6 chữ số)
                <input type="text" name="otp" required maxlength="6" pattern="[0-9]{6}"
                       placeholder="Nhập 6 chữ số" autofocus
                       value="${fn:escapeXml(otp)}" style="font-size: 18px; letter-spacing: 4px; text-align: center;">
            </label>
            <label>Mật khẩu mới
                <input type="password" name="password" required minlength="4"
                       placeholder="Tối thiểu 4 ký tự">
            </label>
            <label>Xác nhận mật khẩu mới
                <input type="password" name="confirmPassword" required minlength="4"
                       placeholder="Nhập lại mật khẩu mới">
            </label>
            <button class="button primary full" type="submit">Đổi mật khẩu</button>
        </form>

        <p class="form-foot">Đã nhớ lại mật khẩu? <a href="${pageContext.request.contextPath}/login">Quay lại đăng nhập</a></p>
    </section>
</main>
</body>
</html>
