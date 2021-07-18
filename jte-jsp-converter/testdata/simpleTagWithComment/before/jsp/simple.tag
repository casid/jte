<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:choose>
    <%-- comment1 --%>
    <c:when test="${someCondition}">
        some text
    </c:when>
    <%-- comment2 --%>
    <c:when test="${anotherCondition}">
        <c:choose>
            <%-- comment3 --%>
            <c:when test="${someCondition}">
                some text
            </c:when>
            <%-- comment4 --%>
            <c:when test="${anotherCondition}">
                some other text
            </c:when>
        </c:choose>
    </c:when>
</c:choose>