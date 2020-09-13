<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="my" prefix="my" %>

<%@ attribute name="greeting" required="true" type="java.lang.String" %>
<%@ attribute name="name" required="true" type="java.lang.String" %>

<h2>${greeting}
<my:simple-dependency/>
${not empty name ? ', ' + name : ''}!</h2>

