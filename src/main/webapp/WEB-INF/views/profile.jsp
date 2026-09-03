<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi">
<head>
    <title>Hồ sơ cá nhân | JPA Category</title>
    <%@ include file="partials/head.jspf" %>
</head>
<body>
<%@ include file="partials/topbar.jspf" %>
<main class="page-shell narrow">
    <div class="page-heading">
        <div>
            <div class="eyebrow">USER PROFILE</div>
            <h1>Hồ sơ cá nhân</h1>
            <p>Xem và chỉnh sửa thông tin tài khoản của bạn.</p>
        </div>
    </div>

    <c:if test="${not empty successMessage}">
        <div class="alert success"><c:out value="${successMessage}"/></div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert error"><c:out value="${errorMessage}"/></div>
    </c:if>

    <div class="profile-card">
        <div class="profile-avatar-section">
            <c:choose>
                <c:when test="${fn:startsWith(user.avatar, 'http://') or fn:startsWith(user.avatar, 'https://')}">
                    <c:set var="avatarSrc" value="${user.avatar}"/>
                </c:when>
                <c:when test="${not empty user.avatar}">
                    <c:url var="avatarSrc" value="/image"><c:param name="fname" value="${user.avatar}"/></c:url>
                </c:when>
                <c:otherwise>
                    <c:set var="avatarSrc" value="${pageContext.request.contextPath}/assets/default-category.svg"/>
                </c:otherwise>
            </c:choose>
            <img class="profile-avatar-preview" src="${fn:escapeXml(avatarSrc)}" alt="Avatar của ${fn:escapeXml(user.fullName)}">
            <div class="profile-summary">
                <h2><c:out value="${user.fullName}"/></h2>
                <span class="role-badge">
                    <c:choose>
                        <c:when test="${user.roleId == 1}">Quản trị viên (Admin)</c:when>
                        <c:when test="${user.roleId == 2}">Quản lý (Manager)</c:when>
                        <c:otherwise>Thành viên (Member)</c:otherwise>
                    </c:choose>
                </span>
            </div>
        </div>

        <form class="editor-card form-stack" method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/profile">
            <div class="field-grid">
                <label>Tên đăng nhập
                    <input type="text" value="${fn:escapeXml(user.username)}" readonly class="input-readonly">
                </label>
                <label>Email
                    <input type="email" value="${fn:escapeXml(user.email)}" readonly class="input-readonly">
                </label>
            </div>

            <label>Họ và tên
                <input type="text" name="fullName" required maxlength="255" value="${fn:escapeXml(user.fullName)}">
            </label>

            <label>Số điện thoại
                <input type="text" name="phone" maxlength="30" placeholder="Chưa có số điện thoại" value="${fn:escapeXml(user.phone)}">
            </label>

            <div class="avatar-inputs">
                <label>Tải lên ảnh mới
                    <input type="file" name="avatarFile" accept=".jpg,.jpeg,.png,.gif,.webp,image/*">
                    <small>Chấp nhận JPG, PNG, GIF, WEBP dưới 5MB. Ưu tiên cao hơn link online.</small>
                </label>
                <div class="separator"><span>hoặc</span></div>
                <label>Đường dẫn ảnh online (URL)
                    <input type="url" name="avatarUrl" maxlength="500" placeholder="https://example.com/avatar.jpg">
                    <small>Để trống cả 2 nếu muốn giữ nguyên ảnh đại diện hiện tại.</small>
                </label>
            </div>

            <button class="button primary" type="submit">Lưu thay đổi</button>
        </form>
    </div>
</main>
</body>
</html>
