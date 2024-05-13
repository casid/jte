<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ attribute name="rows" required="true" type="java.util.List" %>

<div class="well content m-b-3">
    <c:forEach var="row" items="${rows}">
        <div class="row m-b-2">
            <div class="col-sm-12 col-md-8">
                <p class="m-y-1">
                    <strong>${row.label}</strong>
                </p>
            </div>
        </div>
    </c:forEach>
</div>