<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="vi" class="notranslate">
<head>
    <title><sitemesh:write property="title"/></title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="google" content="notranslate">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/products.css">
    <sitemesh:write property="head"/>
</head>
<body>
    <%@ include file="/WEB-INF/views/partials/topbar.jspf" %>
    <sitemesh:write property="body"/>
    <%@ include file="/WEB-INF/views/partials/cart-drawer.jspf" %>
    <%@ include file="/WEB-INF/views/partials/footer.jspf" %>
    <script src="${pageContext.request.contextPath}/assets/app.js" defer></script>
</body>
</html>
