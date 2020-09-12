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

<c:if test="${5 > 0}">
    <span class="text-md">
        <fmt:message key="nested">
            <fmt:param>
                <fmt:message key="common.x">
                    <fmt:param>${'Something'}</fmt:param>
                </fmt:message>
            </fmt:param>
        </fmt:message>
    </span>
</c:if>