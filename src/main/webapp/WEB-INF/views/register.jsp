<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!doctype html>
<html lang="vi">
<head><title>Đăng ký | JPA Store</title>
<%@ include file="partials/head.jspf" %>
</head>
<body class="auth-page">
<main class="auth-shell centered">
<section class="auth-card register-card">
<div class="card-heading"><span class="brand-mark">J</span><div><h1>Đăng ký</h1><p>Tạo tài khoản để khám phá JPA Store</p></div></div>
<c:if test="${not empty error}"><div class="alert error" role="alert"><c:out value="${error}"/></div></c:if>
<form:form modelAttribute="form" method="post" servletRelativeAction="/register" cssClass="registration-form" id="registration-form">
<label>Họ và tên<form:input path="fullName" required="required" minlength="2" maxlength="50" autocomplete="name"/><form:errors path="fullName" cssClass="field-error"/></label>
<label>Tài khoản<form:input path="username" required="required" minlength="3" maxlength="50" autocomplete="username"/><form:errors path="username" cssClass="field-error"/></label>
<label>Email<form:input path="email" type="email" required="required" autocomplete="email"/><form:errors path="email" cssClass="field-error"/></label>
<label>Số điện thoại<form:input path="phone" type="tel" pattern="0[0-9]{9}" maxlength="10" autocomplete="tel"/><form:errors path="phone" cssClass="field-error"/></label>
<label class="register-full">Mật khẩu<form:password path="password" required="required" minlength="6" autocomplete="new-password" placeholder="Tối thiểu 6 ký tự"/><form:errors path="password" cssClass="field-error"/></label>
<button class="button primary register-full" type="submit">Tạo tài khoản</button>
</form:form>
<p class="form-foot">Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a></p>
</section>
</main>
<script src="${pageContext.request.contextPath}/assets/register.js" defer></script>
</body>
</html>
