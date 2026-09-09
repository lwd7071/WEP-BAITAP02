<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi">
<head>
    <title>Cập nhật người dùng | Admin</title>
</head>
<body>
<main class="page-shell">
    <div class="page-heading">
        <div>
            <div class="eyebrow">QUẢN TRỊ VIÊN</div>
            <h1>Cập nhật người dùng: <c:out value="${user.username}"/></h1>
            <p>Cập nhật thông tin cơ bản của người dùng (không đổi vai trò và mật khẩu tại đây).</p>
        </div>
        <a class="button secondary inline" href="${pageContext.request.contextPath}/admin/users">Quay lại danh sách</a>
    </div>

    <c:if test="${not empty errorMessage}"><div class="alert error"><c:out value="${errorMessage}"/></div></c:if>

    <section class="form-card">
        <form method="post" action="${pageContext.request.contextPath}/admin/users/${user.id}/update">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

            <div class="form-group">
                <label for="username">Tên đăng nhập (Chỉ đọc)</label>
                <input id="username" value="${fn:escapeXml(user.username)}" disabled>
            </div>

            <div class="form-group">
                <label for="fullName">Họ và tên *</label>
                <input id="fullName" name="fullName" value="${fn:escapeXml(userForm.fullName)}" required>
            </div>

            <div class="form-group">
                <label for="email">Email *</label>
                <input id="email" type="email" name="email" value="${fn:escapeXml(userForm.email)}" required>
            </div>

            <div class="form-group">
                <label for="phone">Số điện thoại</label>
                <input id="phone" name="phone" value="${fn:escapeXml(userForm.phone)}">
            </div>

            <div class="form-group">
                <label for="avatar">Ảnh đại diện (URL hoặc tên file)</label>
                <input id="avatar" name="avatar" value="${fn:escapeXml(userForm.avatar)}">
            </div>

            <div class="form-actions">
                <button type="submit" class="button primary">Cập nhật thông tin</button>
                <a href="${pageContext.request.contextPath}/admin/users" class="button secondary">Hủy</a>
            </div>
        </form>
    </section>
</main>
</body>
</html>