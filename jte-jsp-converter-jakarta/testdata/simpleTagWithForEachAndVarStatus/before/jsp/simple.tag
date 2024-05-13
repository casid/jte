<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ attribute name="rows" required="true" type="java.util.List" %>

<div class="well content m-b-3">
    <c:forEach var="row" items="${rows}" varStatus="loop">
        <strong>${row.label}</strong>
        <strong>${loop.first}</strong>
        <strong>${loop.last}</strong>
        <strong>${loop.index}</strong>
    </c:forEach>
</div>