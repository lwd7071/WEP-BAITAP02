<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi">
<head>
    <title>Quản lý danh mục | JPA Category</title>
    <%@ include file="../partials/head.jspf" %>
</head>
<body>
<%@ include file="../partials/topbar.jspf" %>
<main class="page-shell">
    <div class="page-heading">
        <div><div class="eyebrow">CATEGORY ADMIN</div><h1>Quản lý danh mục</h1><p>Danh mục được quản lý tập trung bởi Admin.</p></div>
        <a class="button primary inline" href="${pageContext.request.contextPath}/admin/categories/new">+ Thêm danh mục</a>
    </div>
    <c:if test="${not empty message}"><div class="alert success"><c:out value="${message}"/></div></c:if>
    <c:if test="${not empty error}"><div class="alert error"><c:out value="${error}"/></div></c:if>
    <form class="search-bar" method="get" action="${pageContext.request.contextPath}/admin/categories">
        <input name="q" value="${fn:escapeXml(q)}" placeholder="Tìm theo tên danh mục...">
        <button class="button secondary" type="submit">Tìm kiếm</button>
        <c:if test="${not empty q}"><a href="${pageContext.request.contextPath}/admin/categories">Xóa lọc</a></c:if>
    </form>
    <section class="table-card">
        <table>
            <thead><tr><th>#</th><th>Ảnh</th><th>Tên danh mục</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
            <tbody>
            <c:forEach items="${categories.content}" var="category" varStatus="loop">
                <c:choose>
                    <c:when test="${fn:startsWith(category.images, 'http://') or fn:startsWith(category.images, 'https://')}"><c:set var="imageUrl" value="${category.images}"/></c:when>
                    <c:when test="${not empty category.images}"><c:url var="imageUrl" value="/image"><c:param name="fname" value="${category.images}"/></c:url></c:when>
                    <c:otherwise><c:set var="imageUrl" value="${pageContext.request.contextPath}/assets/default-category.svg"/></c:otherwise>
                </c:choose>
                <tr>
                    <td>${categories.number * categories.size + loop.index + 1}</td>
                    <td><img class="category-thumb" src="${fn:escapeXml(imageUrl)}" alt="Ảnh ${fn:escapeXml(category.categoryName)}"></td>
                    <td><strong><c:out value="${category.categoryName}"/></strong><small>ID: ${category.categoryId}</small></td>
                    <td><span class="status ${category.status == 1 ? 'active' : 'locked'}">${category.status == 1 ? 'Hoạt động' : 'Khóa'}</span></td>
                    <td class="actions">
                        <a href="${pageContext.request.contextPath}/admin/categories/${category.categoryId}/edit">Sửa</a>
                        <form method="post" action="${pageContext.request.contextPath}/admin/categories/${category.categoryId}/delete" style="display:inline;">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <button class="delete" type="submit" onclick="return confirm('Xóa danh mục này?')">Xóa</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty categories.content}"><tr><td colspan="5" class="empty-state">Không tìm thấy danh mục phù hợp.</td></tr></c:if>
            </tbody>
        </table>
    </section>
    <nav class="pagination">
        <c:forEach begin="0" end="${categories.totalPages - 1}" var="index">
            <c:url var="pageUrl" value="/admin/categories"><c:param name="page" value="${index}"/><c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if></c:url>
            <a class="${index == categories.number ? 'current' : ''}" href="${pageUrl}">${index + 1}</a>
        </c:forEach>
    </nav>
</main>
<%@ include file="../partials/footer.jspf" %>
<%@ include file="../partials/cart-drawer.jspf" %>
<script src="${pageContext.request.contextPath}/assets/app.js" defer></script>
</body>
</html>
