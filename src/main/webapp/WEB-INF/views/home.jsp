<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="vi">
<head>
    <title><c:out value="${pageTitle}"/> | JPA Category</title>
    <%@ include file="partials/head.jspf" %>
</head>
<body>
<%@ include file="partials/topbar.jspf" %>
<main class="page-shell">
    <section class="hero-panel">
        <div><div class="eyebrow">ĐĂNG NHẬP THÀNH CÔNG</div><h1><c:out value="${pageTitle}"/></h1><p><c:out value="${pageDescription}"/></p></div>
        <div class="role-badge">Role ${sessionScope.account.roleId}</div>
    </section>
    <section class="info-grid">
        <article><span>01</span><h3>Session</h3><p>Tài khoản hiện tại được lưu trong HttpSession.</p></article>
        <article><span>02</span><h3>Cookie</h3><p>Tùy chọn ghi nhớ hoạt động trong 30 phút.</p></article>
        <article><span>03</span><h3>JPA</h3><p>Dữ liệu được truy cập qua EntityManager và Hibernate.</p></article>
    </section>
    <c:if test="${sessionScope.account.roleId == 1}">
        <a class="button primary inline" href="${pageContext.request.contextPath}/admin/categories">Mở quản lý danh mục</a>
    </c:if>
</main>
</body>
</html>
