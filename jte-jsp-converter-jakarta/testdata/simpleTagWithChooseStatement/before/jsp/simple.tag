<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ attribute name="foodOption" required="true" type="example.FoodOption" %>
<%@ attribute name="hangover" required="false" type="java.lang.Boolean" %>

<h4>
<c:choose>
    <c:when test="${foodOption eq 'Pizza'}">
        <c:choose>
            <c:when test="${hangover}">
                Pizza is <b>a good choice to recover</b>!
                Prost!
            </c:when>
            <c:otherwise>
                Pizza is <b>super yummy</b>!
                That's for sure!
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:when test="${foodOption eq 'Salad'}">
        Salad is <i>super healthy</i>!
    </c:when>
    <c:otherwise>
        Enjoy your ${foodOption} :-)
    </c:otherwise>
</c:choose>

<%-- A one liner! --%>
<c:choose><c:when test="${hangover}">Hangover</c:when><c:otherwise>No hangover</c:otherwise></c:choose>
</h4>