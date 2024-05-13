<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="hello" value="Hello World!"/>
${hello}

<c:set var="content">
    <c:choose>
        <c:when test="${true}">
            <p>Hello World</p>
        </c:when>
        <c:otherwise>
            <p>Hello, false World</p>
        </c:otherwise>
    </c:choose>
</c:set>
${content}