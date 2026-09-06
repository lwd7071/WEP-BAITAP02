<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!doctype html>
<html lang="vi" class="notranslate">
<head>
    <title><c:out value="${pageTitle}"/> | JPA Category</title>
</head>
<body>
<main class="page-shell">
    <section class="hero-panel">
        <div><div class="eyebrow">ĐĂNG NHẬP THÀNH CÔNG</div><h1><c:out value="${pageTitle}"/></h1><p><c:out value="${pageDescription}"/></p></div>
        <div class="role-badge">Role ${sessionScope.account.roleId}</div>
    </section>
    <section class="section-heading">
        <div><div class="eyebrow">SẢN PHẨM MỚI</div><h2>10 sản phẩm mới nhất</h2></div>
        <a class="button secondary inline" href="${pageContext.request.contextPath}/product">Xem tất cả</a>
    </section>
    <section class="product-grid">
        <c:forEach items="${latestProducts}" var="product">
            <c:choose>
                <c:when test="${fn:startsWith(product.images, 'http://') or fn:startsWith(product.images, 'https://')}"><c:set var="productImage" value="${product.images}"/></c:when>
                <c:when test="${not empty product.images}"><c:url var="productImage" value="/image"><c:param name="fname" value="${product.images}"/></c:url></c:when>
                <c:otherwise><c:set var="productImage" value="${pageContext.request.contextPath}/assets/default-category.svg"/></c:otherwise>
            </c:choose>
            <a class="product-card" href="${pageContext.request.contextPath}/product/detail?id=${product.productId}">
                <img src="${fn:escapeXml(productImage)}" alt="Ảnh ${fn:escapeXml(product.productName)}">
                <div class="product-card-body"><small><c:out value="${product.category.categoryName}"/></small><h3><c:out value="${product.productName}"/></h3><strong><fmt:formatNumber value="${product.unitPrice}" type="number" maxFractionDigits="0"/> ₫</strong><span>${product.quantity > 0 ? 'Còn hàng' : 'Hết hàng'}</span></div>
            </a>
        </c:forEach>
        <c:if test="${empty latestProducts}"><p class="empty-products">Chưa có sản phẩm đang hoạt động.</p></c:if>
    </section>
</main>
</body>
</html>
