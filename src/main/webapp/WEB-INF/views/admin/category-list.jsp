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
        <div><div class="eyebrow">ADMIN · CATEGORY</div><h1>Quản lý danh mục</h1><p>CRUD bằng Jakarta Persistence API và SQL Server.</p></div>
        <a class="button primary inline" href="${pageContext.request.contextPath}/admin/category/add">+ Thêm danh mục</a>
    </div>
    <c:if test="${not empty success}"><div class="alert success"><c:out value="${success}"/></div></c:if>
    <c:if test="${not empty error}"><div class="alert error"><c:out value="${error}"/></div></c:if>
    <form class="search-bar" method="get" action="${pageContext.request.contextPath}/admin/categories">
        <input name="q" value="${fn:escapeXml(keyword)}" placeholder="Tìm theo tên danh mục...">
        <button class="button secondary" type="submit">Tìm kiếm</button>
        <c:if test="${not empty keyword}"><a href="${pageContext.request.contextPath}/admin/categories">Xóa lọc</a></c:if>
    </form>
    <section class="table-card">
        <table>
            <thead><tr><th>#</th><th>Ảnh</th><th>Tên danh mục</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
            <tbody>
            <c:forEach items="${categories}" var="category" varStatus="loop">
                <c:choose>
                    <c:when test="${fn:startsWith(category.images, 'http://') or fn:startsWith(category.images, 'https://')}"><c:set var="imageUrl" value="${category.images}"/></c:when>
                    <c:when test="${not empty category.images}"><c:url var="imageUrl" value="/image"><c:param name="fname" value="${category.images}"/></c:url></c:when>
                    <c:otherwise><c:set var="imageUrl" value="${pageContext.request.contextPath}/assets/default-category.svg"/></c:otherwise>
                </c:choose>
                <tr>
                    <td>${page * 6 + loop.index + 1}</td>
                    <td><img class="category-thumb" src="${fn:escapeXml(imageUrl)}" alt="Ảnh ${fn:escapeXml(category.categoryName)}"></td>
                    <td><strong><c:out value="${category.categoryName}"/></strong><small>ID: ${category.categoryId}</small></td>
                    <td><span class="status ${category.status == 1 ? 'active' : 'locked'}">${category.status == 1 ? 'Hoạt động' : 'Khóa'}</span></td>
                    <td class="actions">
                        <a href="${pageContext.request.contextPath}/admin/category/edit?id=${category.categoryId}">Sửa</a>
                        <a class="delete" onclick="return confirm('Xóa danh mục này?')" href="${pageContext.request.contextPath}/admin/category/delete?id=${category.categoryId}">Xóa</a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty categories}"><tr><td colspan="5" class="empty-state">Không tìm thấy danh mục phù hợp.</td></tr></c:if>
            </tbody>
        </table>
    </section>
    <nav class="pagination">
        <c:forEach begin="0" end="${totalPages - 1}" var="index">
            <c:url var="pageUrl" value="/admin/categories"><c:param name="page" value="${index}"/><c:if test="${not empty keyword}"><c:param name="q" value="${keyword}"/></c:if></c:url>
            <a class="${index == page ? 'current' : ''}" href="${pageUrl}">${index + 1}</a>
        </c:forEach>
    </nav>
</main>
</body>
</html>
