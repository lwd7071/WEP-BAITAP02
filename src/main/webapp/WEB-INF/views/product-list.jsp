<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!doctype html>
<html lang="vi">
<head>
    <title>Danh mục sản phẩm | Cửa hàng</title>
</head>
<body>
<main class="page-shell">
    <div class="page-heading">
        <div>
            <div class="eyebrow">CỬA HÀNG</div>
            <h1>Danh mục sản phẩm</h1>
            <p>Khám phá các sản phẩm chất lượng cao trong hệ thống.</p>
        </div>
    </div>

    <!-- Thanh tìm kiếm & lọc danh mục -->
    <form class="search-bar" method="get" action="${pageContext.request.contextPath}/products">
        <input name="q" value="${fn:escapeXml(q)}" placeholder="Tìm kiếm theo tên sản phẩm...">
        <select name="categoryId">
            <option value="">-- Tất cả danh mục --</option>
            <c:forEach items="${categories}" var="cat">
                <option value="${cat.categoryId}" ${categoryId == cat.categoryId ? 'selected' : ''}>
                    <c:out value="${cat.categoryName}"/>
                </option>
            </c:forEach>
        </select>
        <button class="button secondary" type="submit">Tìm kiếm</button>
        <c:if test="${not empty q or not empty categoryId}">
            <a href="${pageContext.request.contextPath}/products">Xóa lọc</a>
        </c:if>
    </form>

    <div class="catalog-layout">
        <section class="catalog-main" style="width: 100%;">
            <c:set var="productList" value="${products.content != null ? products.content : products}"/>
            <div class="product-grid">
                <c:forEach items="${productList}" var="product" varStatus="status">
                    <c:choose>
                        <c:when test="${fn:startsWith(product.images, 'http://') or fn:startsWith(product.images, 'https://')}">
                            <c:set var="productImage" value="${product.images}"/>
                        </c:when>
                        <c:when test="${not empty product.images}">
                            <c:url var="productImage" value="/image"><c:param name="fname" value="${product.images}"/></c:url>
                        </c:when>
                        <c:otherwise>
                            <c:set var="productImage" value="${pageContext.request.contextPath}/assets/default-category.svg"/>
                        </c:otherwise>
                    </c:choose>

                    <article class="product-card" style="--card-idx: ${status.index}">
                        <div class="product-thumb-wrap">
                            <a href="${pageContext.request.contextPath}/products/${product.productId}" style="display:block; width:100%; height:100%;">
                                <img src="${fn:escapeXml(productImage)}" alt="Ảnh ${fn:escapeXml(product.productName)}" loading="lazy">
                            </a>
                        </div>

                        <div class="product-card-body">
                            <div class="product-card-meta">
                                <small><c:out value="${product.category.categoryName}"/></small>
                                <span class="product-stock ${product.quantity > 0 ? 'in-stock' : 'out-stock'}">
                                    ${product.quantity > 0 ? 'Còn hàng' : 'Hết hàng'}
                                </span>
                            </div>
                            
                            <h3>
                                <a href="${pageContext.request.contextPath}/products/${product.productId}" style="color: inherit;">
                                    <c:out value="${product.productName}"/>
                                </a>
                            </h3>

                            <div class="product-card-price-row">
                                <span class="product-price">
                                    <fmt:formatNumber value="${product.unitPrice}" type="number" maxFractionDigits="0"/> ₫
                                </span>
                                <a class="button secondary inline" style="padding: 0.4rem 0.8rem; font-size: 0.8rem;" href="${pageContext.request.contextPath}/products/${product.productId}">
                                    Chi tiết
                                </a>
                            </div>
                        </div>
                    </article>
                </c:forEach>

                <c:if test="${empty productList}">
                    <p class="empty-products">Không tìm thấy sản phẩm nào phù hợp.</p>
                </c:if>
            </div>

            <!-- Phân trang -->
            <c:if test="${products.totalPages > 1}">
                <nav class="pagination">
                    <c:forEach begin="0" end="${products.totalPages - 1}" var="index">
                        <c:url var="pageUrl" value="/products">
                            <c:param name="page" value="${index}"/>
                            <c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if>
                            <c:if test="${not empty categoryId}"><c:param name="categoryId" value="${categoryId}"/></c:if>
                        </c:url>
                        <a class="${index == products.number ? 'current' : ''}" href="${pageUrl}">${index + 1}</a>
                    </c:forEach>
                </nav>
            </c:if>
        </section>
    </div>
</main>
</body>
</html>