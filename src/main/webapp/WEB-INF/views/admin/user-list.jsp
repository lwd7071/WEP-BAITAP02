<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi">
<head>
    <title>Quản lý người dùng | Admin</title>
    <%@ include file="../partials/head.jspf" %>
</head>
<body>
<%@ include file="../partials/topbar.jspf" %>
<main class="page-shell">
    <div class="page-heading">
        <div>
            <div class="eyebrow">QUẢN TRỊ VIÊN</div>
            <h1>Quản lý người dùng</h1>
            <p>Xem danh sách, tìm kiếm, kích hoạt/khóa và quản lý tài khoản người dùng.</p>
        </div>
        <a class="button primary inline" href="${pageContext.request.contextPath}/admin/users/new">+ Thêm người dùng</a>
    </div>

    <c:if test="${not empty successMessage}"><div class="alert success"><c:out value="${successMessage}"/></div></c:if>
    <c:if test="${not empty errorMessage}"><div class="alert error"><c:out value="${errorMessage}"/></div></c:if>

    <form class="search-bar" method="get" action="${pageContext.request.contextPath}/admin/users">
        <input name="q" value="${fn:escapeXml(q)}" placeholder="Tìm theo username, tên, email, sđt...">
        <select name="status">
            <option value="">-- Tất cả trạng thái --</option>
            <option value="ACTIVE" ${status == 'ACTIVE' ? 'selected' : ''}>Hoạt động (ACTIVE)</option>
            <option value="BLOCKED" ${status == 'BLOCKED' ? 'selected' : ''}>Bị khóa (BLOCKED)</option>
        </select>
        <select name="provider">
            <option value="">-- Tất cả phương thức --</option>
            <option value="LOCAL" ${provider == 'LOCAL' ? 'selected' : ''}>Local</option>
            <option value="GOOGLE" ${provider == 'GOOGLE' ? 'selected' : ''}>Google</option>
        </select>
        <button class="button secondary" type="submit">Tìm kiếm</button>
        <c:if test="${not empty q or not empty status or not empty provider}">
            <a href="${pageContext.request.contextPath}/admin/users">Xóa lọc</a>
        </c:if>
    </form>

    <section class="table-card">
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Tài khoản</th>
                <th>Họ và tên</th>
                <th>Email</th>
                <th>Số điện thoại</th>
                <th>Phương thức</th>
                <th>Vai trò</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${users.content}" var="u">
                <tr>
                    <td>${u.id}</td>
                    <td><strong><c:out value="${u.username}"/></strong></td>
                    <td><c:out value="${u.fullName}"/></td>
                    <td><c:out value="${u.email}"/></td>
                    <td><c:out value="${u.phone}"/></td>
                    <td><span class="badge ${u.provider == 'GOOGLE' ? 'badge-google' : 'badge-local'}">${u.provider}</span></td>
                    <td><span class="badge ${u.role == 'ADMIN' ? 'badge-admin' : 'badge-user'}">${u.role}</span></td>
                    <td><span class="status ${u.status == 'ACTIVE' ? 'active' : 'locked'}">${u.status}</span></td>
                    <td class="actions">
                        <a href="${pageContext.request.contextPath}/admin/users/${u.id}/edit">Sửa</a>
                        <c:if test="${u.role != 'ADMIN'}">
                            <form method="post" action="${pageContext.request.contextPath}/admin/users/${u.id}/toggle-status" style="display:inline;">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <button type="submit" class="button-link" onclick="return confirm('Bạn có chắc muốn đổi trạng thái tài khoản này?');">
                                    ${u.status == 'ACTIVE' ? 'Khóa' : 'Mở khóa'}
                                </button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/admin/users/${u.id}/delete" style="display:inline;">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <button type="submit" class="button-link delete" onclick="return confirm('Bạn có chắc chắn muốn xóa tài khoản này?');">
                                    Xóa
                                </button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty users.content}">
                <tr><td colspan="9" class="empty-state">Không tìm thấy người dùng phù hợp.</td></tr>
            </c:if>
            </tbody>
        </table>
    </section>

    <c:if test="${users.totalPages > 1}">
        <nav class="pagination">
            <c:forEach begin="0" end="${users.totalPages - 1}" var="index">
                <c:url var="pageUrl" value="/admin/users">
                    <c:param name="page" value="${index}"/>
                    <c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if>
                    <c:if test="${not empty status}"><c:param name="status" value="${status}"/></c:if>
                    <c:if test="${not empty provider}"><c:param name="provider" value="${provider}"/></c:if>
                </c:url>
                <a class="${index == users.number ? 'current' : ''}" href="${pageUrl}">${index + 1}</a>
            </c:forEach>
        </nav>
    </c:if>
</main>
<%@ include file="../partials/footer.jspf" %>
<%@ include file="../partials/cart-drawer.jspf" %>
<script src="${pageContext.request.contextPath}/assets/app.js" defer></script>
</body>
</html>
