<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ attribute name="rows" required="true" type="java.util.List" %>

<div class="well content m-b-3">
    <c:forEach var="row" items="${rows}" varStatus="loop">
        <strong>${row.label}</strong>
        <strong>${loop.first}</strong>
        <strong>${loop.last}</strong>
        <strong>${loop.index}</strong>
    </c:forEach>
</div>