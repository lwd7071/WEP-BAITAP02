<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!doctype html>
<html lang="vi">
<head>
    <title>Quản lý sản phẩm | Admin</title>
    <%@ include file="../partials/head.jspf" %>
</head>
<body>
<%@ include file="../partials/topbar.jspf" %>
<main class="page-shell">
    <div class="page-heading">
        <div>
            <div class="eyebrow">QUẢN TRỊ VIÊN</div>
            <h1>Quản lý sản phẩm</h1>
            <p>Quản lý toàn bộ sản phẩm trong hệ thống cửa hàng.</p>
        </div>
        <a class="button primary inline" href="${pageContext.request.contextPath}/admin/products/new">+ Thêm sản phẩm</a>
    </div>

    <c:if test="${not empty successMessage}"><div class="alert success"><c:out value="${successMessage}"/></div></c:if>
    <c:if test="${not empty errorMessage}"><div class="alert error"><c:out value="${errorMessage}"/></div></c:if>

    <form class="search-bar" method="get" action="${pageContext.request.contextPath}/admin/products">
        <input name="q" value="${fn:escapeXml(q)}" placeholder="Tìm theo tên hoặc mô tả...">
        <select name="categoryId">
            <option value="">-- Tất cả danh mục --</option>
            <c:forEach items="${categories}" var="cat">
                <option value="${cat.categoryId}" ${categoryId == cat.categoryId ? 'selected' : ''}>
                    <c:out value="${cat.categoryName}"/>
                </option>
            </c:forEach>
        </select>
        <select name="status">
            <option value="">-- Tất cả trạng thái --</option>
            <option value="1" ${status == 1 ? 'selected' : ''}>Hoạt động</option>
            <option value="0" ${status == 0 ? 'selected' : ''}>Khóa</option>
        </select>
        <button class="button secondary" type="submit">Tìm kiếm</button>
        <c:if test="${not empty q or not empty categoryId or not empty status}">
            <a href="${pageContext.request.contextPath}/admin/products">Xóa lọc</a>
        </c:if>
    </form>

    <section class="table-card">
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Ảnh</th>
                <th>Tên sản phẩm</th>
                <th>Danh mục</th>
                <th>Đơn giá</th>
                <th>Số lượng</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${products.content}" var="p">
                <c:choose>
                    <c:when test="${fn:startsWith(p.images, 'http://') or fn:startsWith(p.images, 'https://')}">
                        <c:set var="imgUrl" value="${p.images}"/>
                    </c:when>
                    <c:when test="${not empty p.images}">
                        <c:url var="imgUrl" value="/image"><c:param name="fname" value="${p.images}"/></c:url>
                    </c:when>
                    <c:otherwise>
                        <c:set var="imgUrl" value="${pageContext.request.contextPath}/assets/default-category.svg"/>
                    </c:otherwise>
                </c:choose>
                <tr>
                    <td>${p.productId}</td>
                    <td><img class="category-thumb" src="${fn:escapeXml(imgUrl)}" alt="Ảnh"></td>
                    <td><strong><c:out value="${p.productName}"/></strong></td>
                    <td><c:out value="${p.category.categoryName}"/></td>
                    <td><fmt:formatNumber value="${p.unitPrice}" type="number" maxFractionDigits="0"/> ₫</td>
                    <td>${p.quantity}</td>
                    <td><span class="status ${p.status == 1 ? 'active' : 'locked'}">${p.status == 1 ? 'Hoạt động' : 'Khóa'}</span></td>
                    <td class="actions">
                        <a href="${pageContext.request.contextPath}/admin/products/${p.productId}/edit">Sửa</a>
                        <form method="post" action="${pageContext.request.contextPath}/admin/products/${p.productId}/delete" style="display:inline;">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <button type="submit" class="button-link delete" onclick="return confirm('Bạn có chắc chắn muốn xóa sản phẩm này?');">
                                Xóa
                            </button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty products.content}">
                <tr><td colspan="8" class="empty-state">Không có sản phẩm nào phù hợp.</td></tr>
            </c:if>
            </tbody>
        </table>
    </section>

    <c:if test="${products.totalPages > 1}">
        <nav class="pagination">
            <c:forEach begin="0" end="${products.totalPages - 1}" var="index">
                <c:url var="pageUrl" value="/admin/products">
                    <c:param name="page" value="${index}"/>
                    <c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if>
                    <c:if test="${not empty categoryId}"><c:param name="categoryId" value="${categoryId}"/></c:if>
                    <c:if test="${not empty status}"><c:param name="status" value="${status}"/></c:if>
                </c:url>
                <a class="${index == products.number ? 'current' : ''}" href="${pageUrl}">${index + 1}</a>
            </c:forEach>
        </nav>
    </c:if>
</main>
<%@ include file="../partials/footer.jspf" %>
<%@ include file="../partials/cart-drawer.jspf" %>
<script src="${pageContext.request.contextPath}/assets/app.js" defer></script>
</body>
</html>
