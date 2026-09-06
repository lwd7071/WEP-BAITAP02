<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!doctype html><html lang="vi"><head><title>Sản phẩm</title></head><body>
<main class="page-shell">
    <section class="page-heading"><div><div class="eyebrow">PRODUCT CATALOG</div><h1>Sản phẩm</h1><p>Các sản phẩm đang hoạt động thuộc danh mục của bạn.</p></div></section>
    <section class="product-grid">
        <c:forEach items="${products}" var="product">
            <c:choose><c:when test="${fn:startsWith(product.images, 'http://') or fn:startsWith(product.images, 'https://')}"><c:set var="productImage" value="${product.images}"/></c:when><c:when test="${not empty product.images}"><c:url var="productImage" value="/image"><c:param name="fname" value="${product.images}"/></c:url></c:when><c:otherwise><c:set var="productImage" value="${pageContext.request.contextPath}/assets/default-category.svg"/></c:otherwise></c:choose>
            <a class="product-card" href="${pageContext.request.contextPath}/product/detail?id=${product.productId}"><img src="${fn:escapeXml(productImage)}" alt="Ảnh ${fn:escapeXml(product.productName)}"><div class="product-card-body"><small><c:out value="${product.category.categoryName}"/></small><h3><c:out value="${product.productName}"/></h3><strong><fmt:formatNumber value="${product.unitPrice}" type="number" maxFractionDigits="0"/> ₫</strong><span>${product.quantity > 0 ? 'Còn hàng' : 'Hết hàng'}</span></div></a>
        </c:forEach>
        <c:if test="${empty products}"><p class="empty-products">Chưa có sản phẩm đang hoạt động.</p></c:if>
    </section>
    <c:if test="${totalPages > 1}"><nav class="pagination"><c:forEach begin="1" end="${totalPages}" var="number"><a class="${number == page ? 'current' : ''}" href="${pageContext.request.contextPath}/product?page=${number}">${number}</a></c:forEach></nav></c:if>
</main></body></html>
