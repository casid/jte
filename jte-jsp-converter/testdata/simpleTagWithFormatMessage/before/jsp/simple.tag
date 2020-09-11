<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/fmt" prefix = "fmt" %>

<fmt:message key="hello.world"/>

<fmt:message key="hello.world" var="hello"/>
${hello}

<fmt:message key="some.params">
    <fmt:param value="${hello}"/>
    <fmt:param value="Param 2"/>
</fmt:message>

<fmt:message key="some.params" var="params">
    <fmt:param value="${hello}"/>
    <fmt:param value="Param 2"/>
</fmt:message>
${params}