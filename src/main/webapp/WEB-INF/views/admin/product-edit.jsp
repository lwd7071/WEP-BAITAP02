<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi">
<head>
    <title>Cập nhật sản phẩm | Admin</title>
</head>
<body>
<main class="page-shell">
    <div class="page-heading">
        <div>
            <div class="eyebrow">QUẢN TRỊ VIÊN</div>
            <h1>Cập nhật sản phẩm</h1>
            <p>Chỉnh sửa thông tin chi tiết sản phẩm.</p>
        </div>
        <a class="button secondary inline" href="${pageContext.request.contextPath}/admin/products">Quay lại</a>
    </div>

    <c:if test="${not empty errorMessage}"><div class="alert error"><c:out value="${errorMessage}"/></div></c:if>

    <section class="form-card">
        <form method="post" action="${pageContext.request.contextPath}/admin/products/${product.productId}/update">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

            <div class="form-group">
                <label for="productName">Tên sản phẩm *</label>
                <input id="productName" name="productName" value="${fn:escapeXml(productForm.productName)}" required>
            </div>

            <div class="form-group">
                <label for="categoryId">Danh mục *</label>
                <select id="categoryId" name="categoryId" required>
                    <option value="">-- Chọn danh mục --</option>
                    <c:forEach items="${categories}" var="cat">
                        <option value="${cat.categoryId}" ${productForm.categoryId == cat.categoryId ? 'selected' : ''}>
                            <c:out value="${cat.categoryName}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label for="unitPrice">Đơn giá (VNĐ) *</label>
                <input id="unitPrice" type="number" step="1000" min="0" name="unitPrice" value="${productForm.unitPrice}" required>
            </div>

            <div class="form-group">
                <label for="quantity">Số lượng tồn kho *</label>
                <input id="quantity" type="number" min="0" name="quantity" value="${productForm.quantity}" required>
            </div>

            <div class="form-group">
                <label for="images">Ảnh sản phẩm (URL hoặc tên file)</label>
                <input id="images" name="images" value="${fn:escapeXml(productForm.images)}">
            </div>

            <div class="form-group">
                <label for="status">Trạng thái kinh doanh</label>
                <select id="status" name="status">
                    <option value="1" ${productForm.status == 1 ? 'selected' : ''}>Hoạt động</option>
                    <option value="0" ${productForm.status == 0 ? 'selected' : ''}>Khóa</option>
                </select>
            </div>

            <div class="form-group">
                <label for="description">Mô tả sản phẩm</label>
                <textarea id="description" name="description" rows="4"><c:out value="${productForm.description}"/></textarea>
            </div>

            <div class="form-actions">
                <button type="submit" class="button primary">Cập nhật sản phẩm</button>
                <a href="${pageContext.request.contextPath}/admin/products" class="button secondary">Hủy</a>
            </div>
        </form>
    </section>
</main>
</body>
</html>