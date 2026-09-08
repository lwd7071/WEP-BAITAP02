<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!doctype html>
<html lang="vi" class="notranslate">
<head>
    <title>Danh mục sản phẩm | JPA Store</title>
</head>
<body>
<main class="page-shell">
    <!-- Breadcrumb -->
    <nav class="breadcrumb">
        <a href="${pageContext.request.contextPath}/waiting">Trang chủ</a>
        <span>/</span>
        <strong>Sản phẩm</strong>
    </nav>

    <div class="catalog-layout">
        <!-- Sidebar bộ lọc (Cột trái) -->
        <aside class="catalog-sidebar">
            <div class="filter-group">
                <h4>Khoảng Giá</h4>
                <div class="filter-options">
                    <label class="filter-option"><input type="radio" name="price" checked> Tất cả mức giá</label>
                    <label class="filter-option"><input type="radio" name="price"> Dưới 200.000 ₫</label>
                    <label class="filter-option"><input type="radio" name="price"> 200.000 ₫ - 500.000 ₫</label>
                    <label class="filter-option"><input type="radio" name="price"> 500.000 ₫ - 1.000.000 ₫</label>
                    <label class="filter-option"><input type="radio" name="price"> Trên 1.000.000 ₫</label>
                </div>
            </div>

            <div class="filter-group">
                <h4>Tình Trạng Kho</h4>
                <div class="filter-options">
                    <label class="filter-option"><input type="checkbox" checked> Còn hàng sẵn</label>
                    <label class="filter-option"><input type="checkbox"> Đang có khuyến mãi</label>
                </div>
            </div>

            <button class="button secondary full" type="button" onclick="window.showToast && window.showToast('Đã áp dụng bộ lọc sản phẩm!', 'info');">
                Áp dụng bộ lọc
            </button>
        </aside>

        <!-- Lưới sản phẩm & Toolbar (Cột phải) -->
        <section class="catalog-main">
            <div class="catalog-toolbar">
                <div>
                    Hiển thị <strong>${empty products ? 0 : fn:length(products)}</strong> sản phẩm chọn lọc
                </div>
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                    <span>Sắp xếp:</span>
                    <select style="width: auto; padding: 0.4rem 0.8rem; font-size: 0.85rem;" onchange="window.showToast && window.showToast('Đã sắp xếp lại danh sách!', 'info');">
                        <option>Mới nhất</option>
                        <option>Giá tăng dần</option>
                        <option>Giá giảm dần</option>
                        <option>Phổ biến nhất</option>
                    </select>
                </div>
            </div>

            <div class="product-grid">
                <c:forEach items="${products}" var="product" varStatus="status">
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
                        <button type="button" class="card-wishlist-btn" data-action="wishlist" title="Yêu thích">♡</button>
                        
                        <div class="product-thumb-wrap">
                            <a href="${pageContext.request.contextPath}/product/detail?id=${product.productId}" style="display:block; width:100%; height:100%;">
                                <img src="${fn:escapeXml(productImage)}" alt="Ảnh ${fn:escapeXml(product.productName)}" loading="lazy">
                            </a>
                            <div class="product-overlay">
                                <button type="button" class="button primary quick-action-btn" 
                                        data-action="add-to-cart" 
                                        data-product-name="${fn:escapeXml(product.productName)}">
                                    + Thêm vào giỏ
                                </button>
                            </div>
                        </div>

                        <div class="product-card-body">
                            <div class="product-card-meta">
                                <small><c:out value="${product.category.categoryName}"/></small>
                                <span class="product-stock ${product.quantity > 0 ? 'in-stock' : 'out-stock'}">
                                    ${product.quantity > 0 ? 'Còn hàng' : 'Hết hàng'}
                                </span>
                            </div>
                            
                            <h3>
                                <a href="${pageContext.request.contextPath}/product/detail?id=${product.productId}" style="color: inherit;">
                                    <c:out value="${product.productName}"/>
                                </a>
                            </h3>

                            <div class="product-card-price-row">
                                <span class="product-price">
                                    <fmt:formatNumber value="${product.unitPrice}" type="number" maxFractionDigits="0"/> ₫
                                </span>
                                <a class="button secondary inline" style="padding: 0.4rem 0.8rem; font-size: 0.8rem;" href="${pageContext.request.contextPath}/product/detail?id=${product.productId}">
                                    Chi tiết
                                </a>
                            </div>
                        </div>
                    </article>
                </c:forEach>

                <c:if test="${empty products}">
                    <p class="empty-products">Không tìm thấy sản phẩm phù hợp.</p>
                </c:if>
            </div>

            <!-- Phân trang -->
            <c:if test="${totalPages > 1}">
                <nav class="pagination">
                    <c:forEach begin="1" end="${totalPages}" var="number">
                        <a class="${number == page ? 'current' : ''}" href="${pageContext.request.contextPath}/product?page=${number}">
                            ${number}
                        </a>
                    </c:forEach>
                </nav>
            </c:if>
        </section>
    </div>
</main>
</body>
</html>
