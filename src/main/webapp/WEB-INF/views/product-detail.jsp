<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!doctype html><html lang="vi"><head><title><c:out value="${product.productName}"/> | Sản phẩm</title></head><body>
<main class="page-shell">
    <a class="back-link" href="${pageContext.request.contextPath}/product">← Quay lại sản phẩm</a>
    <c:choose><c:when test="${fn:startsWith(product.images, 'http://') or fn:startsWith(product.images, 'https://')}"><c:set var="productImage" value="${product.images}"/></c:when><c:when test="${not empty product.images}"><c:url var="productImage" value="/image"><c:param name="fname" value="${product.images}"/></c:url></c:when><c:otherwise><c:set var="productImage" value="${pageContext.request.contextPath}/assets/default-category.svg"/></c:otherwise></c:choose>
    <article class="product-detail"><img src="${fn:escapeXml(productImage)}" alt="Ảnh ${fn:escapeXml(product.productName)}"><div><div class="eyebrow"><c:out value="${product.category.categoryName}"/></div><h1><c:out value="${product.productName}"/></h1><div class="detail-price"><fmt:formatNumber value="${product.unitPrice}" type="number" maxFractionDigits="0"/> ₫</div><span class="status ${product.quantity > 0 ? 'active' : 'locked'}">${product.quantity > 0 ? 'Còn hàng' : 'Hết hàng'} · ${product.quantity} sản phẩm</span><p class="detail-description"><c:out value="${empty product.description ? 'Sản phẩm chưa có mô tả.' : product.description}"/></p></div></article>
</main></body></html>
