<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ attribute name="greeting" required="true" type="java.lang.String" %>
<%@ attribute name="name" required="true" type="java.lang.String" %>

<h2>${greeting}
<my:simple-dependency1/>
${not empty name ? ', ' + name : ''}!<my:simple-dependency1/></h2>
<my:simple-dependency2/>

