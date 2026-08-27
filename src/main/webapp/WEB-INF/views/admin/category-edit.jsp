<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi">
<head><title>Sửa danh mục | JPA Category</title><%@ include file="../partials/head.jspf" %></head>
<body>
<%@ include file="../partials/topbar.jspf" %>
<main class="page-shell narrow">
    <a class="back-link" href="${pageContext.request.contextPath}/admin/categories">← Quay lại danh sách</a>
    <div class="page-heading"><div><div class="eyebrow">CATEGORY · UPDATE</div><h1>Sửa danh mục</h1><p>Mã danh mục #${category.categoryId}</p></div></div>
    <c:if test="${not empty error}"><div class="alert error"><c:out value="${error}"/></div></c:if>
    <form class="editor-card form-stack" method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/admin/category/update">
        <input type="hidden" name="id" value="${category.categoryId}">
        <label>Tên danh mục <input name="categoryName" maxlength="255" required value="${fn:escapeXml(category.categoryName)}"></label>
        <c:if test="${not empty category.images}">
            <c:choose><c:when test="${fn:startsWith(category.images, 'http')}"><c:set var="currentImage" value="${category.images}"/></c:when><c:otherwise><c:url var="currentImage" value="/image"><c:param name="fname" value="${category.images}"/></c:url></c:otherwise></c:choose>
            <div class="current-image"><img src="${fn:escapeXml(currentImage)}" alt="Ảnh hiện tại"><span>Ảnh hiện tại</span></div>
        </c:if>
        <label>Link ảnh mới <input type="url" name="images" maxlength="500" placeholder="Để trống nếu muốn giữ ảnh hiện tại"></label>
        <div class="separator"><span>hoặc</span></div>
        <label>Upload ảnh mới <input type="file" name="imageFile" accept=".jpg,.jpeg,.png,.gif,.webp,image/*"><small>Upload hoặc nhập link mới sẽ thay ảnh hiện tại.</small></label>
        <fieldset><legend>Trạng thái</legend><label class="radio"><input type="radio" name="status" value="1" ${category.status == 1 ? 'checked' : ''}> Hoạt động</label><label class="radio"><input type="radio" name="status" value="0" ${category.status == 0 ? 'checked' : ''}> Khóa</label></fieldset>
        <button class="button primary" type="submit">Cập nhật danh mục</button>
    </form>
</main>
</body>
</html>
