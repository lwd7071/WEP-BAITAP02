<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!doctype html>
<html lang="vi" class="notranslate">
<head>
    <title>JPA Store | Cửa hàng mua sắm trực tuyến cao cấp</title>
</head>
<body>
<main class="page-shell">
    <!-- 1. Hero Banner chuẩn E-commerce -->
    <section class="hero-ecommerce">
        <div class="hero-content">
            <div class="hero-tag">
                <span class="pulse-dot"></span>
                <span>BỘ SƯU TẬP MỚI 2026</span>
            </div>
            <h1>Khám Phá Đẳng Cấp & Phong Cách Đỉnh Cao</h1>
            <p>Trải nghiệm mua sắm tiện lợi với hàng ngàn sản phẩm chọn lọc, chính sách bảo hành 12 tháng chính hãng và giao hàng hỏa tốc trong ngày.</p>
            <div class="hero-cta-group">
                <a class="button primary" href="${pageContext.request.contextPath}/product">Khám phá ngay</a>
                <c:if test="${not empty sessionScope.account}">
                    <a class="button secondary" href="${pageContext.request.contextPath}/categories">Danh mục của tôi</a>
                </c:if>
            </div>
        </div>

        <div class="hero-graphic">
            <div class="floating-glass-card">
                <span class="floating-card-badge">HOT DEAL THÁNG NÀY</span>
                <h3 class="floating-card-title">Ưu đãi giảm tới 30%</h3>
                <div class="floating-card-price">Chỉ từ 199.000 ₫</div>
                <p style="color: #e2e8f0; font-size: 0.85rem; margin: 0.5rem 0 0;">Áp dụng cho khách hàng mới khi mua sắm online hôm nay.</p>
            </div>
        </div>
    </section>

    <!-- 2. Trust Badges (4 Cam kết vàng) -->
    <section class="trust-badges-bar">
        <div class="trust-item">
            <div class="trust-icon">🚚</div>
            <div>
                <h4>Giao Hàng Siêu Tốc</h4>
                <p>Miễn phí đơn từ 500k</p>
            </div>
        </div>
        <div class="trust-item">
            <div class="trust-icon">🛡️</div>
            <div>
                <h4>100% Chính Hãng</h4>
                <p>Cam kết chất lượng cao</p>
            </div>
        </div>
        <div class="trust-item">
            <div class="trust-icon">🔄</div>
            <div>
                <h4>Đổi Trả Dễ Dàng</h4>
                <p>1 đổi 1 trong 7 ngày</p>
            </div>
        </div>
        <div class="trust-item">
            <div class="trust-icon">🎧</div>
            <div>
                <h4>Hỗ Trợ 24/7</h4>
                <p>Tư vấn nhiệt tình chu đáo</p>
            </div>
        </div>
    </section>

    <!-- 3. Category Strip (Chuyển đổi danh mục trực tiếp trên Home) -->
    <section class="category-strip">
        <div class="eyebrow">DANH MỤC NỔI BẬT</div>
        <div class="category-pills" id="home-category-tabs">
            <button type="button" class="category-pill active" data-category-id="all" data-category-name="Tất cả sản phẩm">
                ✨ Tất cả sản phẩm
            </button>
            <c:forEach items="${categories}" var="cat">
                <button type="button" class="category-pill" data-category-id="${cat.categoryId}" data-category-name="${fn:escapeXml(cat.categoryName)}">
                    📂 <c:out value="${cat.categoryName}"/>
                </button>
            </c:forEach>
        </div>
    </section>

    <!-- 4. Products Grid -->
    <section class="section-heading">
        <div>
            <div class="eyebrow" id="section-eyebrow">BỘ SƯU TẬP</div>
            <h2 id="section-title">Tất cả sản phẩm</h2>
        </div>
        <a id="view-all-link" class="button secondary inline" href="${pageContext.request.contextPath}/product">Xem tất cả →</a>
    </section>

    <section class="product-grid" id="home-product-grid">
        <c:forEach items="${latestProducts}" var="product" varStatus="status">
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

            <article class="product-card" data-category-id="${product.category.categoryId}" style="--card-idx: ${status.index}">
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

        <c:if test="${empty latestProducts}">
            <p class="empty-products">Hiện tại chưa có sản phẩm nào được đăng bán.</p>
        </c:if>
        <p id="empty-category-msg" class="empty-products" style="display: none; grid-column: 1 / -1;">
            Hiện tại chưa có sản phẩm nào thuộc danh mục này.
        </p>
    </section>
</main>
</body>
</html>
