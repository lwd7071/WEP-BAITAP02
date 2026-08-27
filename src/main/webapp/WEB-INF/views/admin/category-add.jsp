<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi">
<head><title>Thêm danh mục | JPA Category</title><%@ include file="../partials/head.jspf" %></head>
<body>
<%@ include file="../partials/topbar.jspf" %>
<main class="page-shell narrow">
    <a class="back-link" href="${pageContext.request.contextPath}/admin/categories">← Quay lại danh sách</a>
    <div class="page-heading"><div><div class="eyebrow">CATEGORY · CREATE</div><h1>Thêm danh mục</h1><p>Ảnh có thể là URL công khai hoặc file tải lên.</p></div></div>
    <c:if test="${not empty error}"><div class="alert error"><c:out value="${error}"/></div></c:if>
    <form class="editor-card form-stack" method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/admin/category/insert">
        <label>Tên danh mục <input name="categoryName" maxlength="255" required value="${fn:escapeXml(categoryName)}" placeholder="Ví dụ: Điện thoại"></label>
        <label>Link ảnh <input type="url" name="images" maxlength="500" value="${fn:escapeXml(images)}" placeholder="https://example.com/image.jpg"></label>
        <div class="separator"><span>hoặc</span></div>
        <label>Upload ảnh <input type="file" name="imageFile" accept=".jpg,.jpeg,.png,.gif,.webp,image/*"><small>Tối đa 5 MB · JPG, PNG, GIF, WEBP</small></label>
        <fieldset><legend>Trạng thái</legend><label class="radio"><input type="radio" name="status" value="1" ${status != '0' ? 'checked' : ''}> Hoạt động</label><label class="radio"><input type="radio" name="status" value="0" ${status == '0' ? 'checked' : ''}> Khóa</label></fieldset>
        <button class="button primary" type="submit">Lưu danh mục</button>
    </form>
</main>
</body>
</html>
