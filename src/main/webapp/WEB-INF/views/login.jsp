<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi">
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
        <c:if test="${not empty alert}"><div class="alert error"><c:out value="${alert}"/></div></c:if>
        <form action="${pageContext.request.contextPath}/login" method="post" class="form-stack">
            <label>Tài khoản
                <input name="username" autocomplete="username" required
                       value="${fn:escapeXml(rememberedUsername)}" placeholder="Nhập tên tài khoản">
            </label>
            <label>Mật khẩu
                <input type="password" name="password" autocomplete="current-password" required
                       placeholder="Nhập mật khẩu">
            </label>
            <label class="check-row"><input type="checkbox" name="remember"> Ghi nhớ đăng nhập trong 30 phút</label>
            <button class="button primary" type="submit">Đăng nhập</button>
        </form>
        <p class="form-foot">Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký ngay</a></p>
    </section>
</main>
</body>
</html>
