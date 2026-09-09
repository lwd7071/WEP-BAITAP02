<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!doctype html>
<html lang="vi" class="notranslate">
<head>
    <title><c:out value="${product.productName}"/> | JPA Store</title>
</head>
<body>
<main class="page-shell">
    <!-- Breadcrumb điều hướng -->
    <nav class="breadcrumb">
        <a href="${pageContext.request.contextPath}/">Trang chủ</a>
        <span>/</span>
        <a href="${pageContext.request.contextPath}/products">Sản phẩm</a>
        <span>/</span>
        <a href="${pageContext.request.contextPath}/products"><c:out value="${product.category.categoryName}"/></a>
        <span>/</span>
        <strong><c:out value="${product.productName}"/></strong>
    </nav>

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

    <!-- Layout Chi Tiết Sản Phẩm Chuẩn E-Commerce -->
    <article class="detail-layout">
        <!-- Khung ảnh sản phẩm (Cột trái) -->
        <div class="detail-gallery">
            <img src="${fn:escapeXml(productImage)}" alt="Ảnh ${fn:escapeXml(product.productName)}">
        </div>

        <!-- Thông tin & Nút mua (Cột phải) -->
        <div class="detail-info">
            <div class="eyebrow"><c:out value="${product.category.categoryName}"/></div>
            <h1 class="detail-title"><c:out value="${product.productName}"/></h1>

            <!-- Đánh giá sao -->
            <div class="detail-rating">
                <span class="star-icons">★★★★★</span>
                <strong>4.9</strong>
                <span>(128 đánh giá khách hàng)</span>
                <span>·</span>
                <span>Đã bán 350+</span>
            </div>

            <!-- Khung giá & Trạng thái -->
            <div class="detail-price-box">
                <div class="detail-main-price">
                    <fmt:formatNumber value="${product.unitPrice}" type="number" maxFractionDigits="0"/> ₫
                </div>
                <div class="detail-stock-chip">
                    ● ${product.quantity > 0 ? 'Còn hàng trong kho' : 'Tạm hết hàng'} (${product.quantity} sản phẩm)
                </div>
            </div>

            <!-- Chọn số lượng (Quantity Stepper: [-] 1 [+]) -->
            <div style="display: flex; align-items: center; gap: 1rem; margin-top: 0.5rem;">
                <span style="font-weight: 700; font-size: 0.9rem;">Số lượng:</span>
                <div class="quantity-stepper">
                    <button type="button" class="btn-qty btn-qty-minus" aria-label="Giảm số lượng">−</button>
                    <input type="number" class="qty-input" value="1" min="1" max="${product.quantity > 0 ? product.quantity : 1}" readonly>
                    <button type="button" class="btn-qty btn-qty-plus" aria-label="Tăng số lượng">+</button>
                </div>
            </div>

            <!-- Hàng nút CTA đôi -->
            <div class="detail-actions-row">
                <button type="button" class="button secondary" 
                        data-action="add-to-cart" 
                        data-product-name="${fn:escapeXml(product.productName)}">
                    🛒 Thêm vào giỏ hàng
                </button>
                <button type="button" class="button primary"
                        data-action="buy-now"
                        data-product-name="${fn:escapeXml(product.productName)}">
                    ⚡ Mua ngay
                </button>
                <button type="button" class="btn-icon wishlist-btn" data-action="wishlist" title="Yêu thích">
                    ♡
                </button>
            </div>

            <!-- Khối cam kết dịch vụ (Mini Policy Box) -->
            <div class="mini-policy-box">
                <div class="mini-policy-item">
                    <span class="mini-policy-icon">🚚</span>
                    <span>Freeship toàn quốc đơn từ 500k</span>
                </div>
                <div class="mini-policy-item">
                    <span class="mini-policy-icon">🛡️</span>
                    <span>Bảo hành chính hãng 12 tháng</span>
                </div>
                <div class="mini-policy-item">
                    <span class="mini-policy-icon">🔄</span>
                    <span>Đổi trả 1-1 trong vòng 7 ngày</span>
                </div>
            </div>

            <!-- Mô tả sản phẩm -->
            <div style="margin-top: 1rem;">
                <h3 style="font-size: 1.15rem; font-weight: 800; margin-bottom: 0.6rem;">Mô tả sản phẩm</h3>
                <p style="color: var(--muted); white-space: pre-line; line-height: 1.7; margin: 0;">
                    <c:out value="${empty product.description ? 'Sản phẩm chính hãng với tiêu chuẩn chất lượng cao nhất. Thiết kế hiện đại, bền đẹp theo thời gian.' : product.description}"/>
                </p>
            </div>
        </div>
    </article>
</main>
</body>
</html>
