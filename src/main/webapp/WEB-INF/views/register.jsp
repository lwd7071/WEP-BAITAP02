<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi" class="notranslate">
<head>
    <title>Đăng ký | JPA Category</title>
    <%@ include file="partials/head.jspf" %>
</head>
<body class="auth-page">
<main class="auth-shell compact">
    <section class="auth-intro">
        <div class="eyebrow">TẠO TÀI KHOẢN</div>
        <h1>Bắt đầu với<br>Jakarta Web.</h1>
        <p>Tài khoản mới được gán vai trò người dùng. Admin và Manager được tạo bằng script seed.</p>
    </section>
    <section class="auth-card wide">
        <div class="card-heading"><span class="brand-mark">J</span><div><h2>Đăng ký</h2><p>Điền đầy đủ thông tin bên dưới</p></div></div>
        <c:if test="${not empty alert}"><div class="alert error"><c:out value="${alert}"/></div></c:if>
        <form action="${pageContext.request.contextPath}/register" method="post" class="form-grid">
            <label>Họ và tên<input name="fullname" required minlength="2" maxlength="50" value="${fn:escapeXml(fullname)}" placeholder="Nguyễn Văn A"></label>
            <label>Tài khoản<input name="username" required minlength="3" maxlength="50" value="${fn:escapeXml(username)}" placeholder="nguyenvana"></label>
            <label>Email<input type="email" name="email" required value="${fn:escapeXml(email)}" placeholder="name@example.com"></label>
            <label>Số điện thoại<input type="tel" name="phone" pattern="0[0-9]{9}" maxlength="10" value="${fn:escapeXml(phone)}" placeholder="0xxxxxxxxx (10 chữ số)" title="Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0"></label>
            <label class="full">Mật khẩu<input type="password" name="password" required minlength="4" autocomplete="new-password" placeholder="Tối thiểu 4 ký tự"></label>
            <button class="button primary full" type="submit">Tạo tài khoản</button>
        </form>
        <p class="form-foot">Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Quay lại đăng nhập</a></p>
    </section>
</main>
</body>
</html>
